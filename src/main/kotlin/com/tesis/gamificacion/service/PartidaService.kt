package com.tesis.gamificacion.service

import com.tesis.gamificacion.dto.request.FinalizarPartidaRequest
import com.tesis.gamificacion.dto.request.IniciarPartidaRequest
import com.tesis.gamificacion.dto.response.EstadisticasJugadorResponse
import com.tesis.gamificacion.dto.response.IniciarPartidaResponse
import com.tesis.gamificacion.dto.response.PartidaResponse
import com.tesis.gamificacion.dto.response.RankingResponse
import com.tesis.gamificacion.model.entities.EstadoComboDTO
import com.tesis.gamificacion.model.entities.EstadoVidasDTO
import com.tesis.gamificacion.model.entities.HintDisponibleDTO
import com.tesis.gamificacion.model.entities.Partida
import com.tesis.gamificacion.model.enums.CategoriasCultural
import com.tesis.gamificacion.model.enums.NivelDificultad
import com.tesis.gamificacion.model.enums.TipoDialogo
import com.tesis.gamificacion.model.request.GuardarPartidaRequest
import com.tesis.gamificacion.model.request.ProcesarErrorRequest
import com.tesis.gamificacion.model.request.ProcesarParejaRequest
import com.tesis.gamificacion.model.request.ResponderPreguntaRequest
import com.tesis.gamificacion.model.request.SolicitarHintRequest
import com.tesis.gamificacion.model.responses.EstadisticasDetalladasDTO
import com.tesis.gamificacion.model.responses.EstadoPartidaResponse
import com.tesis.gamificacion.model.responses.FinalizarPartidaResponse
import com.tesis.gamificacion.model.responses.InsigniaDTO
import com.tesis.gamificacion.model.responses.ProcesarErrorResponse
import com.tesis.gamificacion.model.responses.ProcesarParejaResponse
import com.tesis.gamificacion.model.responses.ResponderPreguntaResponse
import com.tesis.gamificacion.model.responses.SolicitarHintResponse
import com.tesis.gamificacion.repository.PartidaRepository
import com.tesis.gamificacion.repository.UsuarioRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class PartidaService(
    private val partidaRepository: PartidaRepository,
    private val elementoCulturalService: ElementoCulturalService,
    private val gamificacionService: GamificacionService,
    private val gamificacionAvanzadaService: GamificacionAvanzadaService,
    private val cacheLlamadaIAService: CacheLlamadaIAService,
    private val usuarioRepository: UsuarioRepository

) {

    private val logger = LoggerFactory.getLogger(PartidaService::class.java)

    @Transactional
    fun iniciarPartida(request: IniciarPartidaRequest): IniciarPartidaResponse {
        // Obtener elementos aleatorios según el nivel
        val cantidadPares = request.nivel.pares
        val elementos = elementoCulturalService.obtenerAleatoriosPorCategoria(
            request.categoria,
            cantidadPares
        )

        // Crear la partida
        val partida = Partida(
            jugadorId = request.jugadorId,
            nivel = request.nivel,
            categoria = request.categoria,
            intentos = 0,
            tiempoSegundos = 0,
            puntuacion = 0,
            completada = false
        )

        val partidaGuardada = partidaRepository.save(partida)

        // NUEVO: Inicializar estado de gamificación
        val estadoPartida = gamificacionAvanzadaService.inicializarEstadoPartida(partidaGuardada.id!!)

        // NUEVO: Pre-cargar narrativas en caché (async)
        cacheLlamadaIAService.precargarNarrativas(partidaGuardada.id!!, elementos)


        return IniciarPartidaResponse(
            partidaId = partidaGuardada.id!!,
            elementos = elementos
        )
    }

    @Transactional
    fun procesarError(request: ProcesarErrorRequest): ProcesarErrorResponse {
        // ⬇️ AHORA RETORNA PAR
        val (estado, mostrarPregunta) = gamificacionAvanzadaService.procesarError(request.partidaId)

        val elemento = elementoCulturalService.obtenerPorId(request.elementoId)

        val narrativa = cacheLlamadaIAService.obtenerNarrativaEducativa(
            partidaId = request.partidaId,
            elementoId = request.elementoId
        )

        return ProcesarErrorResponse(
            vidasRestantes = estado.vidasActuales,
            comboRoto = estado.parejasConsecutivas > 0,
            narrativa = narrativa,
            estadoActualizado = estadoToDTO(estado),
            mostrarPregunta = mostrarPregunta  // ⬅️ NUEVO
        )
    }

    /**
     * NUEVO: Procesar pareja correcta
     */
    @Transactional
    fun procesarParejaCorrecta(request: ProcesarParejaRequest): ProcesarParejaResponse {
        val estado = gamificacionAvanzadaService.procesarParejaCorrecta(
            request.partidaId,
            request.elementoId
        )

        // Verificar si es pareja perfecta (primer descubrimiento sin errores)
        val esParejaLimpia = gamificacionAvanzadaService.esParejaLimpia(estado, request.elementoId)

        // Generar diálogo cultural solo si es pareja limpia
        val dialogo = if (esParejaLimpia) {
            cacheLlamadaIAService.obtenerDialogoCultural(
                partidaId = request.partidaId,
                elementoId = request.elementoId,
                tipoDialogo = TipoDialogo.PAREJA_PERFECTA
            )
        } else null

        return ProcesarParejaResponse(
            comboActual = estado.parejasConsecutivas,
            multiplicador = estado.multiplicadorActual,
            dialogo = dialogo,
            esPrimerDescubrimiento = esParejaLimpia,
            estadoActualizado = estadoToDTO(estado)
        )
    }

    @Transactional
    fun solicitarHint(request: SolicitarHintRequest): SolicitarHintResponse {
        val (estado, costo) = gamificacionAvanzadaService.usarHint(request.partidaId)

        // Obtener elemento aleatorio no descubierto
        val partida = partidaRepository.findById(request.partidaId)
            .orElseThrow { IllegalArgumentException("Partida no encontrada") }

        val elementosNoDescubiertos = elementoCulturalService
            .obtenerAleatoriosPorCategoria(partida.categoria, partida.nivel.pares)
            .filter { it.id !in estado.elementosDescubiertos }

        if (elementosNoDescubiertos.isEmpty()) {
            throw IllegalStateException("No hay elementos disponibles para hint")
        }

        val elementoTarget = elementosNoDescubiertos.random()

        // Generar hint
        val hintMensaje = cacheLlamadaIAService.obtenerHint(
            partidaId = request.partidaId,
            elementoId = elementoTarget.id,
            tipoHint = request.tipoHint
        )

        return SolicitarHintResponse(
            mensaje = hintMensaje,
            costoPuntos = costo,
            usosRestantes = estado.hintsDisponibles,
            estadoActualizado = estadoToDTO(estado)
        )
    }

    /**
     * NUEVO: Responder pregunta de recuperación
     */
    @Transactional
    fun responderPregunta(request: ResponderPreguntaRequest): ResponderPreguntaResponse {
        val narrativa = cacheLlamadaIAService.obtenerNarrativaEducativa(
            request.partidaId,
            request.elementoId
        )

        val esCorrecta = narrativa.preguntaRecuperacion?.respuestaCorrecta == request.respuestaSeleccionada

        val estado = if (esCorrecta) {
            gamificacionAvanzadaService.recuperarVida(request.partidaId)
        } else {
            gamificacionAvanzadaService.obtenerEstadoPartida(request.partidaId)
        }

        return ResponderPreguntaResponse(
            esCorrecta = esCorrecta,
            vidaRecuperada = esCorrecta,
            vidasActuales = estado.vidasActuales,
            explicacion = narrativa.preguntaRecuperacion?.explicacion ?: "",
            estadoActualizado = estadoToDTO(estado)
        )
    }


    @Transactional
    fun finalizarPartida(request: FinalizarPartidaRequest): FinalizarPartidaResponse {
        val partida = partidaRepository.findById(request.partidaId)
            .orElseThrow { IllegalArgumentException("Partida no encontrada con ID: ${request.partidaId}") }

        if (partida.completada) {
            throw IllegalStateException("La partida ya fue completada")
        }

        // Obtener estado de gamificación
        val estado = gamificacionAvanzadaService.obtenerEstadoPartida(request.partidaId)

        // Calcular puntuación base
        val puntuacionBase = gamificacionService.calcularPuntuacion(
            nivel = partida.nivel,
            intentos = request.intentos,
            tiempoSegundos = request.tiempoSegundos
        )

        // Aplicar multiplicadores de gamificación
        val puntuacionFinal = gamificacionAvanzadaService.calcularPuntuacionConMultiplicador(
            puntuacionBase,
            estado
        )

        // Actualizar partida
        val partidaFinalizada = partida.copy(
            intentos = request.intentos,
            tiempoSegundos = request.tiempoSegundos,
            puntuacion = puntuacionFinal,
            completada = true,
            fechaFin = LocalDateTime.now()
        )

        val partidaGuardada = partidaRepository.save(partidaFinalizada)

        // Determinar insignias
        val insignias = determinarInsignias(partida.nivel, request.intentos, estado)

        return FinalizarPartidaResponse(
            puntuacion = puntuacionFinal,
            insignias = insignias,
            estadisticas = EstadisticasDetalladasDTO(
                precision = gamificacionService.calcularPrecision(request.intentos, partida.nivel),
                mejorCombo = estado.mejorCombo,
                vidasRestantes = estado.vidasActuales,
                hintsUsados = estado.hintsUsados,
                tiempoTotal = request.tiempoSegundos,
                nuevosDescubrimientos = estado.elementosDescubiertos.size
            )
        )
    }

    @Transactional(readOnly = true)
    fun obtenerHistorialJugador(jugadorId: String): List<PartidaResponse> {
        return partidaRepository.findByJugadorIdOrderByFechaInicioDesc(jugadorId)
            .map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun obtenerEstadisticasJugador(jugadorId: String): EstadisticasJugadorResponse {
        val partidas = partidaRepository.findByJugadorIdOrderByFechaInicioDesc(jugadorId)
        val partidasCompletadas = partidas.filter { it.completada }

        val puntuacionPromedio = partidaRepository.findAverageScoreByJugador(jugadorId) ?: 0.0
        val mejorPuntuacion = partidasCompletadas.maxOfOrNull { it.puntuacion }

        val tiempoPromedio = if (partidasCompletadas.isNotEmpty()) {
            partidasCompletadas.map { it.tiempoSegundos }.average()
        } else 0.0

        val intentosPromedio = if (partidasCompletadas.isNotEmpty()) {
            partidasCompletadas.map { it.intentos }.average()
        } else 0.0

        return EstadisticasJugadorResponse(
            jugadorId = jugadorId,
            totalPartidas = partidas.size,
            partidasCompletadas = partidasCompletadas.size,
            puntuacionPromedio = puntuacionPromedio,
            mejorPuntuacion = mejorPuntuacion,
            tiempoPromedioSegundos = tiempoPromedio,
            intentosPromedio = intentosPromedio
        )
    }

    @Transactional(readOnly = true)
    fun obtenerRankingGlobal(limite: Int = 10): List<RankingResponse> {
        val partidas = partidaRepository.findTopScores(limite)

        val usuario = usuarioRepository

        return partidas.mapIndexed { index, partida ->
            val usuario =usuarioRepository.findById(partida.jugadorId.toLong()).orElse(null)
            RankingResponse(
                posicion = index + 1,
                jugadorId = partida.jugadorId,
                puntuacion = partida.puntuacion,
                nivel = partida.nivel,
                categoria = partida.categoria,
                tiempoSegundos = partida.tiempoSegundos,
                fecha = partida.fechaFin ?: partida.fechaInicio,
                nombreJugador = usuario.nombre
            )
        }
    }

    @Transactional(readOnly = true)
    fun obtenerRankingPorNivelYCategoria(
        nivel: NivelDificultad,
        categoria: CategoriasCultural,
        limite: Int = 10
    ): List<RankingResponse> {
        val partidas = partidaRepository.findTopScoresByNivelAndCategoria(nivel, categoria)
            .take(limite)



        return partidas.mapIndexed { index, partida ->
            val usuario = usuarioRepository.findById(partida.jugadorId.toLong()).orElse(null)
            RankingResponse(
                posicion = index + 1,
                jugadorId = partida.jugadorId,
                puntuacion = partida.puntuacion,
                nivel = partida.nivel,
                categoria = partida.categoria,
                tiempoSegundos = partida.tiempoSegundos,
                fecha = partida.fechaFin ?: partida.fechaInicio,
                nombreJugador = usuario.nombre
            )
        }
    }

    // ==================== HELPERS ====================

    private fun estadoToDTO(estado: com.tesis.gamificacion.model.entities.EstadoPartida): EstadoPartidaResponse {
        return EstadoPartidaResponse(
            vidas = EstadoVidasDTO(
                vidasActuales = estado.vidasActuales,
                vidasMaximas = estado.vidasMaximas,
                erroresConsecutivos = estado.erroresConsecutivos
            ),
            combo = EstadoComboDTO(
                parejasConsecutivas = estado.parejasConsecutivas,
                multiplicador = estado.multiplicadorActual,
                comboActivo = estado.parejasConsecutivas >= 2,
                mejorCombo = estado.mejorCombo
            ),
            hints = HintDisponibleDTO(
                costo = 50,
                usosRestantes = estado.hintsDisponibles
            )
        )
    }

    private fun determinarInsignias(
        nivel: NivelDificultad,
        intentos: Int,
        estado: com.tesis.gamificacion.model.entities.EstadoPartida
    ): List<InsigniaDTO> {
        val insignias = mutableListOf<InsigniaDTO>()

        // Memoria Perfecta
        if (intentos == nivel.pares && estado.vidasActuales == 3) {
            insignias.add(InsigniaDTO(
                nombre = "Memoria Perfecta",
                nombreKichwa = "Yuyarina Allilla",
                icono = "🏆",
                descripcion = "Completaste sin errores"
            ))
        }

        // Maestro del Combo
        if (estado.mejorCombo >= 5) {
            insignias.add(InsigniaDTO(
                nombre = "Maestro del Combo",
                nombreKichwa = "Tantanakuy Yachaq",
                icono = "🔥",
                descripcion = "Combo de 5 o más"
            ))
        }

        // Explorador sin Ayuda
        if (estado.hintsUsados == 0) {
            insignias.add(InsigniaDTO(
                nombre = "Explorador Independiente",
                nombreKichwa = "Sapalla Maskaq",
                icono = "🧭",
                descripcion = "Sin usar pistas"
            ))
        }

        return insignias
    }

    @Transactional
    fun guardarPartida(request: GuardarPartidaRequest): Partida {
        logger.info("💾 Guardando partida en la base de datos")
        logger.info("📋 Request: {}", request)

        // Validar que el jugador existe (opcional, pero recomendado)
        // val jugador = jugadorRepository.findById(request.jugadorId.toLong())
        //     .orElseThrow { IllegalArgumentException("Jugador no encontrado") }

        val partida = Partida(
            jugadorId = request.jugadorId,
            nivel = request.nivel,
            categoria = request.categoria,
            puntuacion = request.puntuacion,
            intentos = request.intentos,
            tiempoSegundos = request.tiempoSegundos,
            completada = request.completada,
            fechaInicio = LocalDateTime.now().minusSeconds(request.tiempoSegundos.toLong()),
            fechaFin = if (request.completada) LocalDateTime.now() else null
        )

        logger.info("🔨 Entidad Partida creada: {}", partida)

        val partidaGuardada = try {
            partidaRepository.save(partida)
        } catch (e: Exception) {
            logger.error("❌ Error al guardar en la BD: {}", e.message, e)
            throw e
        }

        logger.info("✅ Partida guardada con ID: {}", partidaGuardada.id)
        logger.info("📊 Datos guardados: Puntuación={}, Intentos={}, Tiempo={}s",
            partidaGuardada.puntuacion,
            partidaGuardada.intentos,
            partidaGuardada.tiempoSegundos
        )

        return partidaGuardada
    }

    /**
     * Obtener partidas de un jugador
     */
    @Transactional(readOnly = true)
    fun obtenerPartidasDeJugador(jugadorId: String): List<Partida> {
        logger.info("🔍 Buscando partidas del jugador: {}", jugadorId)
        val partidas = partidaRepository.findByJugadorIdOrderByFechaInicioDesc(jugadorId)
        logger.info("📊 Partidas encontradas: {}", partidas.size)
        return partidas
    }

    private fun Partida.toResponse() = PartidaResponse(
        id = id!!,
        jugadorId = jugadorId,
        nivel = nivel,
        categoria = categoria,
        intentos = intentos,
        tiempoSegundos = tiempoSegundos,
        puntuacion = puntuacion,
        completada = completada,
        fechaInicio = fechaInicio,
        fechaFin = fechaFin
    )
}