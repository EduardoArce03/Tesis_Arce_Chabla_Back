package com.tesis.gamificacion.dto.response

data class EstadisticasJugadorResponse(
    val jugadorId: String,
    val totalPartidas: Int,
    val partidasCompletadas: Int,
    val puntuacionPromedio: Double,
    val mejorPuntuacion: Int?,
    val tiempoPromedioSegundos: Double,
    val intentosPromedio: Double
)