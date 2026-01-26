// src/main/kotlin/com/tesis/gamificacion/service/ExploracionCapasService.kt
package com.tesis.gamificacion.service

import com.tesis.gamificacion.model.entities.*
import com.tesis.gamificacion.model.enums.*
import com.tesis.gamificacion.model.request.*
import com.tesis.gamificacion.model.responses.*
import com.tesis.gamificacion.repository.*
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

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
    private val capaDescubrimientoRepository: CapaDescubrimientoRepository,
    private val capaTemporalRepository: CapaTemporalRepository
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Obtener todas las capas de un punto específico con su progreso
     */
    fun obtenerCapasPunto(puntoId: Long, partidaId: Long, usuarioId: Long): List<CapaPuntoDTO> {
        logger.info("🔍 obtenerCapasPunto - Punto: $puntoId, Partida: $partidaId, Usuario: $usuarioId")

        val punto = puntoInteresRepository.findById(puntoId)
            .orElseThrow {
                logger.error("❌ Punto $puntoId no encontrado")
                IllegalArgumentException("Punto no encontrado")
            }

        logger.info("✅ Punto encontrado: ${punto.nombre}")

        val progreso = progresoExploracionRepository.findByPartidaIdAndUsuarioId(partidaId, usuarioId)
            .orElseThrow {
                logger.error("❌ Progreso NO encontrado para Partida: $partidaId, Usuario: $usuarioId")
                throw IllegalArgumentException("Progreso no encontrado para usuario $usuarioId")
            }

        logger.info("✅ Progreso encontrado ID: ${progreso.id}")

        val capasGlobales = capaDescubrimientoRepository.findByProgreso(progreso)
        logger.info("📋 Capas globales encontradas: ${capasGlobales.size}")
        capasGlobales.forEach {
            logger.info("   - ${it.nivel.nombre}: desbloqueada=${it.desbloqueada}")
        }

        val resultado = NivelCapa.entries.map { nivelCapa ->
            construirCapaPuntoDTO(punto, nivelCapa, progreso, capasGlobales)
        }

        logger.info("✅ Devolviendo ${resultado.size} capas")

        return resultado
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
        println("🔍 DEBUG: Buscando misión - Punto: ${punto.id}/${punto.nombre}, Nivel: ${nivelCapa.name}")

        return try {
            val capaTemporal = capaTemporalRepository.findByPuntoInteresAndNivel(punto, nivelCapa)

            if (capaTemporal == null) {
                println("❌ No existe capa temporal para punto ${punto.id} nivel ${nivelCapa.name}")
                return null
            }

            println("✅ Capa temporal encontrada ID: ${capaTemporal.id}")

            val mision = capaTemporal.mision

            if (mision == null) {
                println("❌ La capa ID ${capaTemporal.id} NO tiene misión asociada")
                return null
            }

            println("✅ Misión encontrada: ${mision.titulo} (ID: ${mision.id})")

            MisionDTO(
                id = mision.id!!,
                titulo = mision.titulo,
                descripcion = mision.descripcionCorta,
                progreso = calcularProgresoMision(mision, partidaId),
                estado = EstadoMision.DISPONIBLE,
                tipo = TipoMision.TIEMPO_EXPLORACION,
                objetivo = 19,
                recompensaPuntos = 20,
                fechaLimite = LocalDateTime.now().plusDays(1)
            )
        } catch (e: Exception) {
            println("⚠️ Error obteniendo misión: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    private fun calcularProgresoMision(mision: Mision, partidaId: Long): Int {
        // Por ahora devolver 0, después puedes calcular según objetivos
        return 0
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
        logger.info("📍 Iniciando descubrirCapaPunto. Request: $request")

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

            // BUSCAR DESCUBRIMIENTO EXISTENTE
            val descubrimientoExistente = puntoDescubrimientoRepository
                .findByProgresoAndPuntoInteresAndNivelDescubrimiento(
                    progreso,
                    punto,
                    request.nivelCapa
                )

            logger.info("🔍 Descubrimiento existente: ${descubrimientoExistente?.id} (null = nuevo)")

            val narrativaNueva = descubrimientoExistente == null

            // LÓGICA DE DESCUBRIMIENTO
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

                // GENERAR NARRATIVA CON IA
                try {
                    logger.info("🤖 Solicitando narrativa a IA Service...")
                    val narrativa = narrativaIAService.generarNarrativaDescubrimiento(
                        nombrePunto = punto.nombre,
                        categoria = punto.categoria.name,
                        nivel = request.nivelCapa.name,
                        descripcionBase = punto.descripcion
                    ) ?: generarNarrativaFallback(punto, request.nivelCapa)

                    nuevoDescubrimiento.narrativa = narrativa
                    logger.info("✅ Narrativa guardada: ${narrativa.take(50)}...")

                } catch (e: Exception) {
                    logger.error("❌ Error generando narrativa IA: ${e.message}", e)
                    nuevoDescubrimiento.narrativa = generarNarrativaFallback(punto, request.nivelCapa)
                }

                // GUARDAR CON PROTECCIÓN CONTRA DUPLICADOS
                try {
                    puntoDescubrimientoRepository.save(nuevoDescubrimiento)
                    logger.info("✅ Descubrimiento guardado con ID: ${nuevoDescubrimiento.id}")
                } catch (e: DataIntegrityViolationException) {
                    logger.warn("⚠️ Race condition detectado, ignorando error de duplicado")
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
            logger.error("❌ ERROR CRÍTICO en descubrirCapaPunto: ${e.message}", e)
            throw e
        }
    }

    private fun generarNarrativaFallback(punto: PuntoInteres, nivel: NivelCapa): String {
        return "Has descubierto ${punto.nombre} en ${nivel.nombre}. ${punto.descripcion}"
    }
}