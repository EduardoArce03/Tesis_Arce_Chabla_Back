package com.tesis.gamificacion.dto.misiones

data class ProgresoMisionDTO(
    val faseActual: Int,
    val totalFases: Int,
    val puntuacion: Int,
    val intentos: Int,
    val respuestasCorrectas: Int,
    val respuestasIncorrectas: Int,
    val porcentajeCompletado: Double
)