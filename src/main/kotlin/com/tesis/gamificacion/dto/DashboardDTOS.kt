package com.tesis.gamificacion.dto

data class DashboardResponse(
    val usuario: UsuarioInfo,
    val estadisticas: EstadisticasResumen,
    val rankingPosicion: RankingPosicion,
    val logrosRecientes: List<Logro>,
    val juegosDisponibles: List<JuegoDisponible>
)

data class UsuarioInfo(
    val nombre: String,
    val codigoJugador: String,
    val nivel: Int,
    val experiencia: Int,
    val experienciaParaSiguienteNivel: Int
)

data class EstadisticasResumen(
    val totalPartidas: Int,
    val partidasCompletadas: Int,
    val puntuacionTotal: Int,
    val tiempoTotalMinutos: Int,
    val precisionPromedio: Double,
    val mejorPuntuacion: Int?
)

data class RankingPosicion(
    val posicionGlobal: Int,
    val totalJugadores: Int,
    val top3: List<RankingItem>
)

data class RankingItem(
    val posicion: Int,
    val nombre: String,
    val codigoJugador: String,
    val puntuacion: Int
)

data class Logro(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val icono: String,
    val fechaObtenido: String,
    val nuevo: Boolean = false
)

data class JuegoDisponible(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val icono: String,
    val ruta: String,
    val partidasJugadas: Int,
    val disponible: Boolean = true
)