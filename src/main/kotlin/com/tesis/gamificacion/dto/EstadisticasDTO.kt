package com.tesis.gamificacion.dto
import com.tesis.gamificacion.model.enums.CategoriasCultural
import com.tesis.gamificacion.model.enums.NivelDificultad
import java.time.LocalDateTime

data class EstadisticasDetalladasResponse(
    val resumenGeneral: ResumenEstadisticas,
    val historialPartidas: List<PartidaHistorial>,
    val estadisticasPorNivel: List<EstadisticasPorNivel>,
    val estadisticasPorCategoria: List<EstadisticasPorCategoria>,
    val graficoPuntuaciones: List<PuntoGrafico>,
    val mejoresPartidas: List<PartidaHistorial>,
    val rachas: RachasEstadisticas
)

data class ResumenEstadisticas(
    val totalPartidas: Int,
    val partidasCompletadas: Int,
    val tasaCompletacion: Double,
    val puntuacionTotal: Int,
    val puntuacionPromedio: Double,
    val mejorPuntuacion: Int?,
    val tiempoTotalMinutos: Int,
    val tiempoPromedioMinutos: Double,
    val precisionPromedio: Double,
    val nivelFavorito: NivelDificultad?,
    val categoriaFavorita: CategoriasCultural?
)

data class PartidaHistorial(
    val id: Long,
    val fechaInicio: LocalDateTime,
    val nivel: NivelDificultad,
    val categoria: CategoriasCultural,
    val puntuacion: Int,
    val intentos: Int,
    val tiempoSegundos: Int,
    val completada: Boolean,
    val precision: Double
)

data class EstadisticasPorNivel(
    val nivel: NivelDificultad,
    val partidasJugadas: Int,
    val partidasCompletadas: Int,
    val puntuacionPromedio: Double,
    val mejorPuntuacion: Int?,
    val precisionPromedio: Double
)

data class EstadisticasPorCategoria(
    val categoria: CategoriasCultural,
    val partidasJugadas: Int,
    val puntuacionPromedio: Double,
    val mejorPuntuacion: Int?
)

data class PuntoGrafico(
    val fecha: String,
    val puntuacion: Int,
    val nivel: NivelDificultad
)

data class RachasEstadisticas(
    val rachaActual: Int,
    val mejorRacha: Int,
    val partidasConsecutivasSinPerder: Int,
    val ultimaPartidaGanada: LocalDateTime?
)