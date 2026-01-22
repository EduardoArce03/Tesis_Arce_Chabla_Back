// src/main/kotlin/com/tesis/gamificacion/service/ExploracionCapasService.kt
package com.tesis.gamificacion.service

import com.tesis.gamificacion.model.entities.*
import com.tesis.gamificacion.model.enums.*
import com.tesis.gamificacion.model.request.*
import com.tesis.gamificacion.model.responses.*
import com.tesis.gamificacion.repository.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Servicio extendido para manejar el sistema de capas por punto
 */
@Service
@Transactional
class ExploracionCapasService(
    private val puntoInteresRepository: PuntoInteresRepository,
    private val progresoExploracionRepository: ProgresoExploracionRepository,
    private val puntoDescubrimientoRepository: PuntoDescubrimientoRepository,
    private val fotografiaObjetivoRepository: FotografiaObjetivoRepository,
    private val fotografiaCapturadaRepository: FotografiaCapturadaRepository,
    private val dialogoHistorialRepository: DialogoHistorialRepository,
    private val misionService: MisionService,
    private val narrativaIAService: NarrativaIAService,
    private val capaDescubrimientoRepository: CapaDescubrimientoRepository
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Obtener todas las capas de un punto específico con su progreso
     */
    fun obtenerCapasPunto(puntoId: Long, partidaId: Long): List<CapaPuntoDTO> {
        val punto = puntoInteresRepository.findById(puntoId)
            .orElseThrow { IllegalArgumentException("Punto no encontrado") }

        val progreso = progresoExploracionRepository.findByPartidaId(partidaId)
            ?: throw IllegalArgumentException("Progreso no encontrado")

        // Obtener capas globales desbloqueadas
        val capasGlobales = capaDescubrimientoRepository.findByProgreso(progreso)

        // Crear DTO para cada capa temporal
        return NivelCapa.entries.map { nivelCapa ->
            construirCapaPuntoDTO(punto, nivelCapa, progreso, capasGlobales)
        }
    }

    /**
     * Construir DTO de una capa específica de un punto
     */
    private fun construirCapaPuntoDTO(
        punto: PuntoInteres,
        nivelCapa: NivelCapa,
        progreso: ProgresoExploracion,
        capasGlobales: List<CapaDescubrimiento>
    ): CapaPuntoDTO {
        // Verificar si la capa global está desbloqueada
        val capaGlobal = capasGlobales.find { it.nivel == nivelCapa }
        val desbloqueada = capaGlobal?.desbloqueada ?: false

        // Buscar descubrimiento específico de esta capa + punto
        val descubrimiento = punto.descubrimientos.find {
            it.progreso.id == progreso.id &&
                    esDescubrimientoDeNivel(it, nivelCapa)
        }

        // Fotografías de esta capa
        val fotografias = obtenerFotografiasCapa(punto, nivelCapa, progreso)

        // Diálogos
        val dialogosRealizados = contarDialogosCapa(punto, nivelCapa, progreso)

        // Misión asociada
        val mision = obtenerMisionCapa(punto, nivelCapa, progreso.partidaId)
        val misionCompletada = mision?.estado == EstadoMision.COMPLETADA

        // Calcular nivel de descubrimiento (BRONCE, PLATA, ORO)
        val nivelDescubrimiento = calcularNivelDescubrimientoCapa(
            descubrimiento = descubrimiento,
            punto = punto,
            nivelCapa = nivelCapa,
            progreso = progreso,
            fotosCompletadas = fotografias.count { it.completada },
            fotosRequeridas = fotografias.size,
            dialogosRealizados = dialogosRealizados,
            misionCompletada = misionCompletada
        )

        // Calcular porcentaje de completitud
        val porcentaje = calcularPorcentajeCompletitudCapa(
            narrativaLeida = descubrimiento != null,
            fotografias = fotografias,
            dialogos = dialogosRealizados,
            mision = mision
        )

        return CapaPuntoDTO(
            nivelCapa = nivelCapa,
            nombre = nivelCapa.nombre,
            descripcion = nivelCapa.descripcion,
            desbloqueada = desbloqueada,
            nivelDescubrimiento = nivelDescubrimiento,
            porcentajeCompletitud = porcentaje,

            narrativaLeida = descubrimiento != null,
            narrativaTexto = descubrimiento?.narrativa,

            fotografiasRequeridas = fotografias.size,
            fotografiasCompletadas = fotografias.count { it.completada },
            fotografiasPendientes = fotografias,

            dialogosRealizados = dialogosRealizados,
            tieneDialogoDisponible = desbloqueada,

            misionAsociada = mision,
            misionCompletada = misionCompletada,

            puntosGanados = 20,
            recompensaFinal = if (nivelDescubrimiento == NivelDescubrimiento.ORO) {
                RecompensaDTO(
                    tipo = "ORO_CAPA",
                    cantidad = 500,
                    descripcion = "Exploración completa de ${nivelCapa.nombre}"
                )
            } else null
        )
    }

    /**
     * Verificar si un descubrimiento pertenece a un nivel específico
     */
    private fun esDescubrimientoDeNivel(
        descubrimiento: PuntoDescubrimiento,
        nivelCapa: NivelCapa
    ): Boolean {
        // ⚠️ IMPORTANTE: Asume que PuntoDescubrimiento.nivelDescubrimiento es de tipo NivelCapa
        // Si tu entidad todavía usa NivelDescubrimiento, necesitas cambiarla primero
        return descubrimiento.nivelDescubrimiento == nivelCapa
    }

    /**
     * Calcular nivel de descubrimiento (BRONCE, PLATA, ORO) basado en completitud
     */
    private fun calcularNivelDescubrimientoCapa(
        descubrimiento: PuntoDescubrimiento?,
        punto: PuntoInteres,
        nivelCapa: NivelCapa,
        progreso: ProgresoExploracion,
        fotosCompletadas: Int,
        fotosRequeridas: Int,
        dialogosRealizados: Int,
        misionCompletada: Boolean
    ): NivelDescubrimiento {
        if (descubrimiento == null) {
            return NivelDescubrimiento.NO_VISITADO
        }

        val narrativaLeida = true // Si hay descubrimiento, la narrativa se leyó
        val todasFotosCompletadas = fotosRequeridas == 0 || fotosCompletadas == fotosRequeridas
        val dialogoRealizado = dialogosRealizados > 0

        // Lógica de niveles:
        // ORO: Narrativa + Todas las fotos + Diálogo + Misión (si hay misión)
        // PLATA: Narrativa + (Todas las fotos O Diálogo)
        // BRONCE: Solo narrativa
        return when {
            narrativaLeida && todasFotosCompletadas && dialogoRealizado && misionCompletada
                -> NivelDescubrimiento.ORO

            narrativaLeida && (todasFotosCompletadas || dialogoRealizado)
                -> NivelDescubrimiento.PLATA

            narrativaLeida
                -> NivelDescubrimiento.BRONCE

            else
                -> NivelDescubrimiento.NO_VISITADO
        }
    }

    /**
     * Obtener fotografías de una capa específica
     */
    private fun obtenerFotografiasCapa(
        punto: PuntoInteres,
        nivelCapa: NivelCapa,
        progreso: ProgresoExploracion
    ): List<FotografiaObjetivoSimpleDTO> {
        val objetivos = fotografiaObjetivoRepository.findByPuntoInteresId(punto.id!!)
            .filter { it.nivelRequerido == nivelCapa }

        return objetivos.map { objetivo ->
            val capturada = fotografiaCapturadaRepository.existsByProgresoAndObjetivo(
                progreso,
                objetivo
            )

            FotografiaObjetivoSimpleDTO(
                id = objetivo.id!!,
                descripcion = objetivo.descripcion,
                rareza = objetivo.rareza.name,
                completada = capturada
            )
        }
    }

    /**
     * Contar diálogos realizados en una capa
     */
    private fun contarDialogosCapa(
        punto: PuntoInteres,
        nivelCapa: NivelCapa,
        progreso: ProgresoExploracion
    ): Int {
        val capa = capaDescubrimientoRepository.findByProgresoAndNivel(progreso, nivelCapa)
            ?: return 0

        return dialogoHistorialRepository.findByProgresoAndCapaOrderByFechaDesc(progreso, capa)
            .count { it.puntoInteresRelacionado?.id == punto.id }
    }

    /**
     * Obtener misión asociada a una capa de un punto
     */
    private fun obtenerMisionCapa(
        punto: PuntoInteres,
        nivelCapa: NivelCapa,
        partidaId: Long
    ): MisionDTO? {
        return try {
            // Buscar misiones que involucren este punto y nivel
            val misiones = misionService.obtenerMisionesDisponibles(partidaId)

            // Buscar misión que mencione el punto y la capa
            misiones.find { mision ->
                mision.descripcion.contains(punto.nombre, ignoreCase = true) &&
                        mision.descripcion.contains(nivelCapa.nombre, ignoreCase = true)
            }
        } catch (e: Exception) {
            // Si no hay misiones o hay error, devolver null
            null
        }
    }

    /**
     * Calcular porcentaje de completitud de una capa
     */
    private fun calcularPorcentajeCompletitudCapa(
        narrativaLeida: Boolean,
        fotografias: List<FotografiaObjetivoSimpleDTO>,
        dialogos: Int,
        mision: MisionDTO?
    ): Double {
        var completadas = 0.0
        var total = 0.0

        // Narrativa (25%)
        total += 25.0
        if (narrativaLeida) completadas += 25.0

        // Fotografías (35%)
        if (fotografias.isNotEmpty()) {
            total += 35.0
            val porcentajeFotos = (fotografias.count { it.completada }.toDouble() / fotografias.size) * 35.0
            completadas += porcentajeFotos
        }

        // Diálogo (20%)
        total += 20.0
        if (dialogos > 0) completadas += 20.0

        // Misión (20%)
        if (mision != null) {
            total += 20.0
            if (mision.estado == EstadoMision.COMPLETADA) {
                completadas += 20.0
            }
        }

        return if (total > 0) (completadas / total) * 100.0 else 0.0
    }

    /**
     * Descubrir/Entrar a una capa específica de un punto
     */
    fun descubrirCapaPunto(request: DescubrirCapaPuntoRequest): DescubrirCapaPuntoResponse {
        logger.info("📍 Iniciando descubrirCapaPunto. Request: $request") // Log de entrada

        try {
            // BUSQUEDA DE PUNTO
            val punto = puntoInteresRepository.findById(request.puntoId)
                .orElseThrow {
                    logger.warn("⚠️ Punto ID ${request.puntoId} no encontrado")
                    IllegalArgumentException("Punto no encontrado")
                }

            // BUSQUEDA DE PROGRESO
            val progreso = progresoExploracionRepository.findByPartidaId(request.partidaId)
                ?: run {
                    logger.warn("⚠️ Progreso para partida ${request.partidaId} no encontrado")
                    throw IllegalArgumentException("Progreso no encontrado")
                }

            // BUSQUEDA DE CAPA GLOBAL
            val capaGlobal = capaDescubrimientoRepository.findByProgresoAndNivel(
                progreso,
                request.nivelCapa
            ) ?: run {
                logger.warn("⚠️ Capa global no encontrada: ${request.nivelCapa} en progreso ${progreso.id}")
                throw IllegalArgumentException("Capa no encontrada")
            }

            // VERIFICACION DE BLOQUEO
            if (!capaGlobal.desbloqueada) {
                logger.info("🔒 Capa bloqueada. Retornando estado bloqueado.")
                return DescubrirCapaPuntoResponse(
                    exito = false,
                    capa = construirCapaPuntoDTO(punto, request.nivelCapa, progreso, listOf(capaGlobal)),
                    narrativaNueva = false,
                    mensaje = "Esta capa temporal aún no está desbloqueada"
                )
            }

            // LOGICA DE DESCUBRIMIENTO
            val descubrimientoExistente = punto.descubrimientos.find {
                it.progreso.id == progreso.id && esDescubrimientoDeNivel(it, request.nivelCapa)
            }

            val narrativaNueva = descubrimientoExistente == null

            if (descubrimientoExistente == null) {
                logger.info("✨ Primer descubrimiento detectado para punto ${punto.id} en capa ${request.nivelCapa}")

                val nuevoDescubrimiento = PuntoDescubrimiento(
                    puntoInteres = punto,
                    progreso = progreso,
                    nivelDescubrimiento = request.nivelCapa,
                    visitas = 1,
                    quizCompletado = false,
                    usuarioId = progreso.usuarioId,
                )
                punto.descubrimientos.add(nuevoDescubrimiento)
                puntoInteresRepository.save(punto) // Guardado BD

                // ⚠️ PUNTO CRÍTICO: LLAMADA EXTERNA A IA
                try {
                    logger.info("🤖 Solicitando narrativa a IA Service...")
                    narrativaIAService.generarNarrativaDescubrimiento(
                        nombrePunto = punto.nombre,
                        categoria = punto.categoria.name,
                        nivel = request.nivelCapa.name,
                        descripcionBase = punto.descripcion
                    )
                    logger.info("✅ Narrativa IA generada correctamente")
                } catch (e: Exception) {
                    // Importante: No detenemos el juego si falla la IA, pero lo logueamos
                    logger.error("❌ Error generando narrativa IA (no crítico): ${e.message}", e)
                }

            } else {
                logger.info("🔄 Re-visitando punto. Incrementando contador.")
                descubrimientoExistente.visitas++
                puntoDescubrimientoRepository.save(descubrimientoExistente)
            }

            // CONSTRUIR RESPUESTA
            val capasGlobales = capaDescubrimientoRepository.findByProgreso(progreso)
            val capaDTO = construirCapaPuntoDTO(punto, request.nivelCapa, progreso, capasGlobales)

            logger.info("✅ descubrirCapaPunto finalizado con éxito")
            return DescubrirCapaPuntoResponse(
                exito = true,
                capa = capaDTO,
                narrativaNueva = narrativaNueva,
                mensaje = if (narrativaNueva) "Primera exploración de esta capa" else "Continuando exploración"
            )

        } catch (e: Exception) {
            // 🚨 AQUÍ ATRAPAS CUALQUIER ERROR QUE NO TE ESTABA SALIENDO
            logger.error("❌ ERROR CRÍTICO en descubrirCapaPunto: ${e.message}", e)
            throw e // Re-lanzamos la excepción para que el Controller la maneje, pero ya quedó registrada en log
        }
    }
}