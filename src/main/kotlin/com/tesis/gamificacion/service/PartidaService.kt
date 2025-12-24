package com.tesis.gamificacion.service

import com.tesis.gamificacion.dto.request.FinalizarPartidaRequest
import com.tesis.gamificacion.dto.request.IniciarPartidaRequest
import com.tesis.gamificacion.dto.response.EstadisticasJugadorResponse
import com.tesis.gamificacion.dto.response.IniciarPartidaResponse
import com.tesis.gamificacion.dto.response.PartidaResponse
import com.tesis.gamificacion.dto.response.RankingResponse
import com.tesis.gamificacion.model.entities.Partida
import com.tesis.gamificacion.model.enums.CategoriasCultural
import com.tesis.gamificacion.model.enums.NivelDificultad
import com.tesis.gamificacion.repository.PartidaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class PartidaService(
    private val partidaRepository: PartidaRepository,
    private val elementoCulturalService: ElementoCulturalService,
    private val gamificacionService: GamificacionService
) {

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

        return IniciarPartidaResponse(
            partidaId = partidaGuardada.id!!,
            elementos = elementos
        )
    }

    @Transactional
    fun finalizarPartida(request: FinalizarPartidaRequest): PartidaResponse {
        val partida = partidaRepository.findById(request.partidaId)
            .orElseThrow { IllegalArgumentException("Partida no encontrada con ID: ${request.partidaId}") }

        if (partida.completada) {
            throw IllegalStateException("La partida ya fue completada")
        }

        // Calcular puntuación
        val puntuacion = gamificacionService.calcularPuntuacion(
            nivel = partida.nivel,
            intentos = request.intentos,
            tiempoSegundos = request.tiempoSegundos
        )

        // Actualizar partida
        val partidaFinalizada = partida.copy(
            intentos = request.intentos,
            tiempoSegundos = request.tiempoSegundos,
            puntuacion = puntuacion,
            completada = true,
            fechaFin = LocalDateTime.now()
        )

        val partidaGuardada = partidaRepository.save(partidaFinalizada)
        return partidaGuardada.toResponse()
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

        return partidas.mapIndexed { index, partida ->
            RankingResponse(
                posicion = index + 1,
                jugadorId = partida.jugadorId,
                puntuacion = partida.puntuacion,
                nivel = partida.nivel,
                categoria = partida.categoria,
                tiempoSegundos = partida.tiempoSegundos,
                fecha = partida.fechaFin ?: partida.fechaInicio
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
            RankingResponse(
                posicion = index + 1,
                jugadorId = partida.jugadorId,
                puntuacion = partida.puntuacion,
                nivel = partida.nivel,
                categoria = partida.categoria,
                tiempoSegundos = partida.tiempoSegundos,
                fecha = partida.fechaFin ?: partida.fechaInicio
            )
        }
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