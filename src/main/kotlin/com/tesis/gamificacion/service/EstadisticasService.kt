package com.tesis.gamificacion.service

import com.tesis.gamificacion.dto.EstadisticasDetalladasResponse
import com.tesis.gamificacion.dto.EstadisticasPorCategoria
import com.tesis.gamificacion.dto.EstadisticasPorNivel
import com.tesis.gamificacion.dto.PartidaHistorial
import com.tesis.gamificacion.dto.PuntoGrafico
import com.tesis.gamificacion.dto.RachasEstadisticas
import com.tesis.gamificacion.dto.ResumenEstadisticas
import com.tesis.gamificacion.dto.response.*
import com.tesis.gamificacion.model.enums.CategoriasCultural
import com.tesis.gamificacion.model.enums.NivelDificultad
import com.tesis.gamificacion.repository.PartidaRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.format.DateTimeFormatter

@Service
class EstadisticasService(
    private val partidaRepository: PartidaRepository
) {
    private val logger = LoggerFactory.getLogger(EstadisticasService::class.java)
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    @Transactional(readOnly = true)
    fun obtenerEstadisticasDetalladas(usuarioId: Long): EstadisticasDetalladasResponse {
        logger.info("📊 Obteniendo estadísticas detalladas para usuario: {}", usuarioId)

        val jugadorId = usuarioId.toString()
        val todasLasPartidas = partidaRepository.findByJugadorIdOrderByFechaInicioDesc(jugadorId)
        val partidasCompletadas = todasLasPartidas.filter { it.completada }

        logger.info("Partidas encontradas: {} (completadas: {})", todasLasPartidas.size, partidasCompletadas.size)

        return EstadisticasDetalladasResponse(
            resumenGeneral = calcularResumenGeneral(todasLasPartidas, partidasCompletadas),
            historialPartidas = obtenerHistorial(todasLasPartidas),
            estadisticasPorNivel = calcularEstadisticasPorNivel(jugadorId),
            estadisticasPorCategoria = calcularEstadisticasPorCategoria(jugadorId),
            graficoPuntuaciones = generarGraficoPuntuaciones(partidasCompletadas),
            mejoresPartidas = obtenerMejoresPartidas(partidasCompletadas),
            rachas = calcularRachas(todasLasPartidas)
        )
    }

    private fun calcularResumenGeneral(
        todas: List<com.tesis.gamificacion.model.entities.Partida>,
        completadas: List<com.tesis.gamificacion.model.entities.Partida>
    ): ResumenEstadisticas {
        val totalPartidas = todas.size
        val numCompletadas = completadas.size
        val tasaCompletacion = if (totalPartidas > 0) (numCompletadas.toDouble() / totalPartidas) * 100 else 0.0

        val puntuacionTotal = completadas.sumOf { it.puntuacion }
        val puntuacionPromedio = if (numCompletadas > 0) puntuacionTotal.toDouble() / numCompletadas else 0.0
        val mejorPuntuacion = completadas.maxOfOrNull { it.puntuacion }

        val tiempoTotal = completadas.sumOf { it.tiempoSegundos }
        val tiempoPromedioMinutos = if (numCompletadas > 0) (tiempoTotal.toDouble() / numCompletadas) / 60 else 0.0

        val precisionPromedio = if (completadas.isNotEmpty()) {
            completadas.map { calcularPrecision(it.intentos, it.nivel) }.average()
        } else 0.0

        val nivelFavorito = completadas.groupBy { it.nivel }
            .maxByOrNull { it.value.size }?.key

        val categoriaFavorita = completadas.groupBy { it.categoria }
            .maxByOrNull { it.value.size }?.key

        return ResumenEstadisticas(
            totalPartidas = totalPartidas,
            partidasCompletadas = numCompletadas,
            tasaCompletacion = tasaCompletacion,
            puntuacionTotal = puntuacionTotal,
            puntuacionPromedio = puntuacionPromedio,
            mejorPuntuacion = mejorPuntuacion,
            tiempoTotalMinutos = tiempoTotal / 60,
            tiempoPromedioMinutos = tiempoPromedioMinutos,
            precisionPromedio = precisionPromedio,
            nivelFavorito = nivelFavorito,
            categoriaFavorita = categoriaFavorita
        )
    }

    private fun obtenerHistorial(partidas: List<com.tesis.gamificacion.model.entities.Partida>): List<PartidaHistorial> {
        return partidas.take(20).map { partida ->
            PartidaHistorial(
                id = partida.id ?: 0,
                fechaInicio = partida.fechaInicio,
                nivel = partida.nivel,
                categoria = partida.categoria,
                puntuacion = partida.puntuacion,
                intentos = partida.intentos,
                tiempoSegundos = partida.tiempoSegundos,
                completada = partida.completada,
                precision = calcularPrecision(partida.intentos, partida.nivel)
            )
        }
    }

    private fun calcularEstadisticasPorNivel(jugadorId: String): List<EstadisticasPorNivel> {
        return NivelDificultad.entries.map { nivel ->
            val partidas = partidaRepository.findByJugadorIdAndNivelOrderByFechaInicioDesc(jugadorId, nivel)
            val completadas = partidas.filter { it.completada }

            EstadisticasPorNivel(
                nivel = nivel,
                partidasJugadas = partidas.size,
                partidasCompletadas = completadas.size,
                puntuacionPromedio = if (completadas.isNotEmpty()) completadas.map { it.puntuacion }.average() else 0.0,
                mejorPuntuacion = completadas.maxOfOrNull { it.puntuacion },
                precisionPromedio = if (completadas.isNotEmpty()) {
                    completadas.map { calcularPrecision(it.intentos, it.nivel) }.average()
                } else 0.0
            )
        }
    }

    private fun calcularEstadisticasPorCategoria(jugadorId: String): List<EstadisticasPorCategoria> {
        return CategoriasCultural.entries.map { categoria ->
            val partidas = partidaRepository.findByJugadorIdAndCategoriaOrderByFechaInicioDesc(jugadorId, categoria)
            val completadas = partidas.filter { it.completada }

            EstadisticasPorCategoria(
                categoria = categoria,
                partidasJugadas = partidas.size,
                puntuacionPromedio = if (completadas.isNotEmpty()) completadas.map { it.puntuacion }.average() else 0.0,
                mejorPuntuacion = completadas.maxOfOrNull { it.puntuacion }
            )
        }
    }

    private fun generarGraficoPuntuaciones(partidas: List<com.tesis.gamificacion.model.entities.Partida>): List<PuntoGrafico> {
        return partidas.take(10).reversed().map { partida ->
            PuntoGrafico(
                fecha = partida.fechaInicio.format(dateFormatter),
                puntuacion = partida.puntuacion,
                nivel = partida.nivel
            )
        }
    }

    private fun obtenerMejoresPartidas(partidas: List<com.tesis.gamificacion.model.entities.Partida>): List<PartidaHistorial> {
        return partidas.sortedByDescending { it.puntuacion }.take(5).map { partida ->
            PartidaHistorial(
                id = partida.id ?: 0,
                fechaInicio = partida.fechaInicio,
                nivel = partida.nivel,
                categoria = partida.categoria,
                puntuacion = partida.puntuacion,
                intentos = partida.intentos,
                tiempoSegundos = partida.tiempoSegundos,
                completada = partida.completada,
                precision = calcularPrecision(partida.intentos, partida.nivel)
            )
        }
    }

    private fun calcularRachas(partidas: List<com.tesis.gamificacion.model.entities.Partida>): RachasEstadisticas {
        var rachaActual = 0
        var mejorRacha = 0
        var rachaTemp = 0

        partidas.reversed().forEach { partida ->
            if (partida.completada) {
                rachaTemp++
                if (rachaTemp > mejorRacha) mejorRacha = rachaTemp
            } else {
                rachaTemp = 0
            }
        }

        // Racha actual (desde el final hacia atrás)
        partidas.takeWhile { it.completada }.also { rachaActual = it.size }

        val ultimaGanada = partidas.firstOrNull { it.completada }?.fechaInicio

        return RachasEstadisticas(
            rachaActual = rachaActual,
            mejorRacha = mejorRacha,
            partidasConsecutivasSinPerder = rachaActual,
            ultimaPartidaGanada = ultimaGanada
        )
    }

    private fun calcularPrecision(intentos: Int, nivel: NivelDificultad): Double {
        val intentosMinimos = when (nivel) {
            NivelDificultad.FACIL -> 6
            NivelDificultad.MEDIO -> 8
            NivelDificultad.DIFICIL -> 12
        }
        return ((intentosMinimos.toDouble() / intentos) * 100).coerceAtMost(100.0)
    }
}