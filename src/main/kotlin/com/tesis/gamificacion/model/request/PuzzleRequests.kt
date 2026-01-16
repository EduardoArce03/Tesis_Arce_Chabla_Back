package com.tesis.gamificacion.model.request

data class IniciarPuzzleRequest(
    val jugadorId: String,
    val imagenId: Long,
    val gridSize: Int  // 3, 4, 5, 6
)

data class FinalizarPuzzleRequest(
    val partidaId: Long,
    val movimientos: Int,
    val tiempoRestante: Int, // ⬅️ CAMBIADO: De tiempoSegundos a tiempoRestante
    val hintsUsados: Int
)

data class SolicitarHintPuzzleRequest(
    val partidaId: Long,
    val imagenId: Long
)

data class ResponderPreguntaPuzzleRequest(
    val partidaId: Long,
    val imagenId: Long,
    val respuestaSeleccionada: Int
)

data class RegistrarProgresoSeccionRequest(
    val partidaId: Long,
    val porcentajeCompletado: Int  // 25, 50, 75, 100
)