package com.tesis.gamificacion.service

import com.tesis.gamificacion.model.data.*
import com.tesis.gamificacion.model.entities.*
import com.tesis.gamificacion.model.enums.*
import com.tesis.gamificacion.repository.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestTemplate
import java.net.URL
import java.time.LocalDateTime
import java.util.Base64

@Service
@Transactional
class ExploracionService(
    private val partidaRepository: PartidaRepository,
    private val progresoCapaRepository: ProgresoCapaRepository,
    private val fotografiaCapturadaRepository: FotografiaCapturadaRepository,
    private val dialogoHistorialRepository: DialogoHistorialRepository,
    private val dialogoIAService: DialogoIAService,
    private val narrativaIAService: NarrativaIAService,
    private val restTemplate: RestTemplate,
    private val fotografiaIAService: FotografiaIAService
) {

    private val log = LoggerFactory.getLogger(ExploracionService::class.java)

    // ==================== INICIALIZACIÓN ====================

    fun iniciarPartida(jugadorId: Long): PartidaDTO {
        log.info("🎮 Iniciando partida para jugador $jugadorId")

        val partida = Partida(
            jugadorId = jugadorId.toString(),
            nivel = NivelDificultad.FACIL,
            categoria = CategoriasCultural.FESTIVIDADES,
            intentos = 0,
            tiempoSegundos = 0,
            puntuacion = 0
        )
        val partidaGuardada = partidaRepository.save(partida)

        // Inicializar TODAS las capas ACTUAL como desbloqueadas
        PuntoInteres.entries.forEach { punto ->
            val progresoActual = ProgresoCapa(
                partida = partidaGuardada,
                puntoId = punto.id,
                capaNivel = CapaNivel.ACTUAL,
                desbloqueada = true,
                fechaDesbloqueo = LocalDateTime.now(),
                fotografiasRequeridas = FotografiasConfig.obtenerObjetivos(punto, CapaNivel.ACTUAL).size
            )
            progresoCapaRepository.save(progresoActual)
            log.info("✅ Capa ACTUAL creada para punto ${punto.nombre}")
        }

        log.info("✅ Partida ${partidaGuardada.id} creada con ${PuntoInteres.entries.size} capas ACTUAL desbloqueadas")

        return construirPartidaDTO(partidaGuardada)
    }

    // ==================== OBTENER MAPA ====================

    fun obtenerMapa(partidaId: Long): MapaDTO {
        val partida = obtenerPartida(partidaId)

        val puntosDTO = PuntoInteres.entries.map { punto ->
            // Buscar todas las capas de este punto
            val todasLasCapas = progresoCapaRepository.findByPartidaAndPuntoId(partida, punto.id)

            val capaActual = todasLasCapas.find { it.capaNivel == CapaNivel.ACTUAL }
            val capaCanari = todasLasCapas.find { it.capaNivel == CapaNivel.CANARI }

            PuntoDTO(
                id = punto.id,
                nombre = punto.nombre,
                descripcion = punto.descripcion,
                coordenadaX = punto.coordenadaX,
                coordenadaY = punto.coordenadaY,
                imagenUrl = punto.imagenUrl,
                explorado = capaActual != null && capaActual.narrativaLeida,
                capas = listOfNotNull(
                    capaActual?.let { construirCapaDTO(it, punto) },
                    capaCanari?.let { construirCapaDTO(it, punto) }
                )
            )
        }

        return MapaDTO(
            partidaId = partida.id!!,
            jugadorId = partida.jugadorId.toLong(),
            puntos = puntosDTO,
            puntosExplorados = partida.puntosExplorados,
            fotografiasCapturadas = partida.fotografiasCapturadas,
            dialogosRealizados = partida.dialogosRealizados,
            puntuacionTotal = partida.puntuacionTotal,
            completada = partida.completada
        )
    }

    // ==================== EXPLORAR CAPA ====================

    fun explorarCapa(request: ExplorarCapaRequest): ExplorarCapaResponse {
        log.info("📍 Explorando: Partida ${request.partidaId}, Punto ${request.puntoId}, Capa ${request.capaNivel}")

        val partida = obtenerPartida(request.partidaId)
        val punto = PuntoInteres.fromId(request.puntoId)
            ?: throw IllegalArgumentException("Punto no válido")

        // Buscar progreso existente
        var progreso = progresoCapaRepository.findByPartidaAndPuntoIdAndCapaNivel(
            partida, punto.id, request.capaNivel
        )

        if (progreso == null) {
            // Solo CANARI puede no existir al inicio
            if (request.capaNivel == CapaNivel.CANARI) {
                val capaActual = progresoCapaRepository.findByPartidaAndPuntoIdAndCapaNivel(
                    partida, punto.id, CapaNivel.ACTUAL
                )

                if (capaActual == null || !capaActual.completada) {
                    return ExplorarCapaResponse(
                        exito = false,
                        mensaje = "Debes completar la capa ACTUAL primero",
                        capa = CapaDTO(
                            id = 0,
                            nivel = CapaNivel.CANARI,
                            nombre = "Capa Cañari",
                            desbloqueada = false,
                            completada = false,
                            narrativaLeida = false,
                            fotografiasCompletadas = 0,
                            fotografiasRequeridas = 0,
                            dialogosRealizados = 0,
                            porcentaje = 0.0
                        ),
                        narrativa = NarrativaDTO("", "", ""),
                        objetivosFotograficos = emptyList(),
                        primerDescubrimiento = false
                    )
                }

                // Crear capa CANARI
                progreso = ProgresoCapa(
                    partida = partida,
                    puntoId = punto.id,
                    capaNivel = CapaNivel.CANARI,
                    desbloqueada = true,
                    fechaDesbloqueo = LocalDateTime.now(),
                    fotografiasRequeridas = FotografiasConfig.obtenerObjetivos(punto, CapaNivel.CANARI).size
                )
                progresoCapaRepository.save(progreso)

                log.info("🔓 Capa CANARI creada y desbloqueada para punto ${punto.nombre}")
            } else {
                throw IllegalStateException("Capa ACTUAL no encontrada - Error de inicialización")
            }
        }

        // Verificar desbloqueada
        if (!progreso.desbloqueada) {
            return ExplorarCapaResponse(
                exito = false,
                mensaje = "Capa bloqueada",
                capa = construirCapaDTO(progreso, punto),
                narrativa = NarrativaDTO("", "", ""),
                objetivosFotograficos = emptyList(),
                primerDescubrimiento = false
            )
        }

        // Primera vez: marcar narrativa como leída
        val primerDescubrimiento = !progreso.narrativaLeida

        if (primerDescubrimiento) {
            progreso.narrativaLeida = true
            progresoCapaRepository.save(progreso)

            // Incrementar puntosExplorados solo en primer ACTUAL
            if (request.capaNivel == CapaNivel.ACTUAL) {
                partida.puntosExplorados++
                partidaRepository.save(partida)
            }

            log.info("✅ Primera exploración de capa ${request.capaNivel} en ${punto.nombre}")
        }

        // Obtener narrativa
        val objetoNarracion = NarrativasConfig.obtener(punto, request.capaNivel)

        val narrativa = this.narrativaIAService.generarNarrativaEducativa(punto.imagenUrl, "LUGARES", punto.descripcion, punto.nombre, request.capaNivel.nombre)

        val descripcion = (narrativa?.get("descripcion") ?: "Respuesta de fallback") as String

        val narrativaDTO = NarrativaDTO(
            titulo = objetoNarracion.titulo,
            texto = descripcion,
            nombreEspiritu = objetoNarracion.nombreEspiritu,
        )

        // Obtener objetivos fotográficos
        val objetivosFoto = FotografiasConfig.obtenerObjetivos(punto, request.capaNivel)

        // Marcar cuáles ya están completadas
        val fotosCapturadas = progreso.fotografiasCapturadas.map { it.objetivoId }.toSet()

        val objetivosDTO = objetivosFoto.map { objetivo ->
            ObjetivoFotoDTO(
                id = objetivo.id,
                descripcion = objetivo.descripcion,
                completada = fotosCapturadas.contains(objetivo.id)
            )
        }

        log.info("📋 Objetivos fotográficos: ${objetivosDTO.count { it.completada }}/${objetivosDTO.size} completados")

        return ExplorarCapaResponse(
            exito = true,
            capa = construirCapaDTO(progreso, punto),
            narrativa = narrativaDTO,
            objetivosFotograficos = objetivosDTO,
            primerDescubrimiento = primerDescubrimiento
        )
    }

    // ==================== CAPTURAR FOTOGRAFÍA ====================

    fun capturarFotografia(request: CapturarFotoRequest): CapturarFotoResponse {
        log.info("📸 Capturando foto: Objetivo ${request.objetivoId}, ProgresoCapa ${request.progresoCapaId}")

        val partida = obtenerPartida(request.partidaId)

        val progreso = progresoCapaRepository.findById(request.progresoCapaId)
            .orElseThrow { IllegalArgumentException("Progreso no encontrado") }

        val punto = PuntoInteres.fromId(progreso.puntoId)
            ?: throw IllegalStateException("Punto no válido")

        // Validar objetivo
        val objetivosValidos = FotografiasConfig.obtenerObjetivos(punto, progreso.capaNivel)
        val objetivo = objetivosValidos.find { it.id == request.objetivoId }
            ?: throw IllegalArgumentException("Objetivo no válido para esta capa")

        // Verificar si ya fue capturada
        val yaCapturada = fotografiaCapturadaRepository.existsByProgresoCapaAndObjetivoId(
            progreso, objetivo.id
        )

        if (yaCapturada) {
            return CapturarFotoResponse(
                exito = false,
                mensaje = "Ya capturaste esta fotografía",
                fotografiasCompletadas = progreso.fotografiasCompletadas,
                fotografiasRequeridas = progreso.fotografiasRequeridas
            )
        }

        // Variables para el resultado
        var descripcionIA = "Fotografía validada manualmente"
        var validadaPorIA = false
        var puntosGanados = 10 // Manual = menos puntos

        // Si hay imagen, validar con IA
        if (!request.imagenBase64.isNullOrBlank()) {
            try {
                log.info("🤖 Validando fotografía con IA...")

                val resultadoIA = dialogoIAService.analizarFotografia(
                    imagenBase64 = request.imagenBase64,
                    objetivoNombre = objetivo.descripcion,
                    objetivoDescripcion = objetivo.descripcion,
                    rarezaEsperada = ""
                )

                log.info("🤖 Resultado IA: válida=${resultadoIA.esValida}, cumple=${resultadoIA.cumpleCriterios}, confianza=${resultadoIA.confianza}")

                descripcionIA = resultadoIA.descripcionIA
                validadaPorIA = resultadoIA.esValida && resultadoIA.cumpleCriterios

                // Si la IA rechaza la foto
                if (!validadaPorIA) {
                    return CapturarFotoResponse(
                        exito = false,
                        mensaje = "La fotografía no cumple con el objetivo",
                        fotografiasCompletadas = progreso.fotografiasCompletadas,
                        fotografiasRequeridas = progreso.fotografiasRequeridas,
                        descripcionIA = descripcionIA
                    )
                }

                // Puntos según confianza de la IA
                puntosGanados = when {
                    resultadoIA.confianza >= 0.9 -> 50
                    resultadoIA.confianza >= 0.7 -> 35
                    else -> 25
                }

            } catch (e: Exception) {
                log.error("❌ Error en validación IA: ${e.message}")
                // Continuar sin validación IA (modo manual)
                descripcionIA = "Error en validación IA: ${e.message}"
                validadaPorIA = false
                puntosGanados = 10
            }
        }

        // Guardar fotografía
        val foto = FotografiaCapturada(
            progresoCapa = progreso,
            objetivoId = objetivo.id,
            imagenBase64 = request.imagenBase64,
            descripcionIA = descripcionIA,
            validadaPorIA = validadaPorIA
        )
        fotografiaCapturadaRepository.save(foto)

        // Actualizar progreso
        progreso.fotografiasCompletadas++
        progresoCapaRepository.save(progreso)

        // Actualizar contador global
        partida.fotografiasCapturadas++
        partida.puntuacionTotal += puntosGanados
        partidaRepository.save(partida)

        log.info("✅ Fotografía capturada: ${progreso.fotografiasCompletadas}/${progreso.fotografiasRequeridas}")

        // Verificar completitud
        verificarCompletitudCapa(progreso, partida)

        return CapturarFotoResponse(
            exito = true,
            mensaje = if (validadaPorIA) "¡Fotografía capturada con validación IA!" else "¡Fotografía capturada!",
            fotografiasCompletadas = progreso.fotografiasCompletadas,
            fotografiasRequeridas = progreso.fotografiasRequeridas,
            puntos = puntosGanados,
            descripcionIA = descripcionIA
        )
    }

    // ==================== DIALOGAR ====================

    fun dialogar(request: DialogarRequest): DialogarResponse {
        log.info("💬 Diálogo: ${request.pregunta.take(50)}...")

        val partida = obtenerPartida(request.partidaId)

        val progreso = progresoCapaRepository.findById(request.progresoCapaId)
            .orElseThrow { IllegalArgumentException("Progreso no encontrado") }

        val punto = PuntoInteres.fromId(progreso.puntoId)
            ?: throw IllegalStateException("Punto no válido")

        val narrativa = NarrativasConfig.obtener(punto, progreso.capaNivel)

        val historial = this.dialogoHistorialRepository.findByProgresoCapa(progreso)

        val narrativaIA = dialogoIAService.generarRespuestaEspiritu(progreso.capaNivel, request.pregunta, punto.imagenUrl,historial, null)

        // Generar respuesta (temporal)
        val respuesta = "Esta es una respuesta del espíritu ${narrativa.nombreEspiritu} sobre tu pregunta: \"${request.pregunta}\""

        // Guardar diálogo
        val dialogo = DialogoHistorial(
            progresoCapa = progreso,
            preguntaUsuario = request.pregunta,
            respuestaEspiritu = narrativaIA
        )
        dialogoHistorialRepository.save(dialogo)

        // Actualizar progreso
        progreso.dialogosRealizados++
        progresoCapaRepository.save(progreso)

        // Actualizar contador global
        partida.dialogosRealizados++
        partida.puntuacionTotal += 15
        partidaRepository.save(partida)

        log.info("✅ Diálogo guardado: ${progreso.dialogosRealizados} total")

        // Verificar completitud
        verificarCompletitudCapa(progreso, partida)

        return DialogarResponse(
            exito = true,
            respuesta = narrativaIA,
            nombreEspiritu = narrativa.nombreEspiritu,
            dialogosRealizados = progreso.dialogosRealizados
        )
    }

    // ==================== COMPLETITUD ====================

    private fun verificarCompletitudCapa(progreso: ProgresoCapa, partida: Partida) {
        if (progreso.completada) {
            log.info("⏭️ Capa ${progreso.capaNivel} ya estaba completada")
            return
        }

        // Requisitos para completar:
        // 1. Narrativa leída
        // 2. TODAS las fotos capturadas
        // 3. Al menos 1 diálogo
        val completa = progreso.narrativaLeida &&
                progreso.fotografiasCompletadas >= progreso.fotografiasRequeridas &&
                progreso.dialogosRealizados >= 1

        if (completa) {
            progreso.completada = true
            progreso.fechaCompletado = LocalDateTime.now()
            progresoCapaRepository.save(progreso)

            log.info("🎉 Capa ${progreso.capaNivel} COMPLETADA en punto ${progreso.puntoId}")

            // Si completó ACTUAL, crear CANARI
            if (progreso.capaNivel == CapaNivel.ACTUAL) {
                desbloquearCapaCanari(partida, progreso.puntoId)
            }

            // Verificar partida completa
            verificarPartidaCompleta(partida)
        } else {
            log.info("""
                📊 Progreso capa ${progreso.capaNivel} punto ${progreso.puntoId}:
                - Narrativa: ${progreso.narrativaLeida}
                - Fotos: ${progreso.fotografiasCompletadas}/${progreso.fotografiasRequeridas}
                - Diálogos: ${progreso.dialogosRealizados}/1
                - Completada: $completa
            """.trimIndent())
        }
    }

    private fun desbloquearCapaCanari(partida: Partida, puntoId: Int) {
        val punto = PuntoInteres.fromId(puntoId)!!

        val capaCanariExiste = progresoCapaRepository.findByPartidaAndPuntoIdAndCapaNivel(
            partida, punto.id, CapaNivel.CANARI
        )

        if (capaCanariExiste == null) {
            val nuevaCapaCanari = ProgresoCapa(
                partida = partida,
                puntoId = punto.id,
                capaNivel = CapaNivel.CANARI,
                desbloqueada = true,
                fechaDesbloqueo = LocalDateTime.now(),
                fotografiasRequeridas = FotografiasConfig.obtenerObjetivos(punto, CapaNivel.CANARI).size
            )
            progresoCapaRepository.save(nuevaCapaCanari)

            log.info("🔓 Capa CANARI creada y desbloqueada automáticamente para ${punto.nombre}")
        } else {
            log.info("⚠️ Capa CANARI ya existía para ${punto.nombre}")
        }
    }

    private fun verificarPartidaCompleta(partida: Partida) {
        if (partida.completada) {
            log.info("⏭️ Partida ya estaba completada")
            return
        }

        // Obtener todas las capas
        val todasLasCapas = progresoCapaRepository.findByPartida(partida)

        // Contar cuántas están completadas
        val completadas = todasLasCapas.count { it.completada }
        val total = PuntoInteres.entries.size * 2 // 3 puntos x 2 capas = 6

        log.info("📊 Progreso partida: $completadas/$total capas completadas")

        // Partida completa = 6 capas completadas
        if (completadas >= total) {
            partida.completada = true
            partida.fechaFin = LocalDateTime.now()
            partidaRepository.save(partida)

            log.info("🏆 ¡PARTIDA ${partida.id} COMPLETADA!")
        }
    }

    // ==================== UTILIDADES ====================

    private fun obtenerPartida(partidaId: Long): Partida {
        return partidaRepository.findById(partidaId)
            .orElseThrow { IllegalArgumentException("Partida no encontrada con ID: $partidaId") }
    }

    private fun construirCapaDTO(progreso: ProgresoCapa, punto: PuntoInteres): CapaDTO {
        return CapaDTO(
            id = progreso.id!!,
            nivel = progreso.capaNivel,
            nombre = when (progreso.capaNivel) {
                CapaNivel.ACTUAL -> "Época Actual"
                CapaNivel.CANARI -> "Época Cañari"
            },
            desbloqueada = progreso.desbloqueada,
            completada = progreso.completada,
            narrativaLeida = progreso.narrativaLeida,
            fotografiasCompletadas = progreso.fotografiasCompletadas,
            fotografiasRequeridas = progreso.fotografiasRequeridas,
            dialogosRealizados = progreso.dialogosRealizados,
            porcentaje = calcularPorcentaje(progreso)
        )
    }

    private fun calcularPorcentaje(progreso: ProgresoCapa): Double {
        var total = 0.0
        var completado = 0.0

        // Narrativa 30%
        total += 30
        if (progreso.narrativaLeida) completado += 30

        // Fotografías 50%
        total += 50
        if (progreso.fotografiasRequeridas > 0) {
            completado += (progreso.fotografiasCompletadas.toDouble() / progreso.fotografiasRequeridas) * 50
        }

        // Diálogos 20%
        total += 20
        if (progreso.dialogosRealizados >= 1) completado += 20

        return (completado / total) * 100
    }

    private fun construirPartidaDTO(partida: Partida): PartidaDTO {
        return PartidaDTO(
            id = partida.id!!,
            jugadorId = partida.jugadorId.toLong(),
            puntosExplorados = partida.puntosExplorados,
            fotografiasCapturadas = partida.fotografiasCapturadas,
            dialogosRealizados = partida.dialogosRealizados,
            puntuacionTotal = partida.puntuacionTotal,
            completada = partida.completada,
            fechaInicio = partida.fechaInicio
        )
    }

    // ==================== ENDPOINTS ADICIONALES ====================

    fun obtenerPartidaDTO(partidaId: Long): PartidaDTO {
        val partida = obtenerPartida(partidaId)
        return construirPartidaDTO(partida)
    }

    fun eliminarPartida(partidaId: Long) {
        val partida = obtenerPartida(partidaId)
        partidaRepository.delete(partida)
        log.info("🗑️ Partida $partidaId eliminada")
    }

    fun obtenerPartidasJugador(jugadorId: Long): List<PartidaDTO> {
        val partidas = partidaRepository.findByJugadorIdOrderByFechaInicioDesc(jugadorId.toString())
        return partidas.map { construirPartidaDTO(it) }
    }

    // Inyecta esto en el constructor de tu servicio
// private val restTemplate: RestTemplate

    private fun descargarImagenConSpring(url: String?): ByteArray? {
        if (url.isNullOrBlank()) return null

        return try {
            restTemplate.getForObject(url, ByteArray::class.java)
        } catch (e: Exception) {
            log.error("❌ Error descargando imagen: ${e.message}")
            null
        }
    }

    fun convertirUrlABase64(url: String?): String? {
        // 1. Validamos que la URL no venga vacía
        if (url.isNullOrBlank()) return null

        return try {
            // 2. Descargamos la imagen (bajamos los bytes)
            val imagenBytes = URL(url).readBytes()

            // 3. Convertimos los bytes a String Base64
            Base64.getEncoder().encodeToString(imagenBytes)

        } catch (e: Exception) {
            // Si la URL está rota o no hay internet, imprimimos error y devolvemos null
            println("❌ Error convirtiendo URL a Base64: ${e.message}")
            null
        }
    }
}