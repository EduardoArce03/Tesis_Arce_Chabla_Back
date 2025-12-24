package com.tesis.gamificacion.dto.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class FinalizarPartidaRequest(
    @field:NotNull(message = "El ID de la partida es requerido")
    val partidaId: Long,

    @field:Min(value = 0, message = "Los intentos deben ser mayor o igual a 0")
    val intentos: Int,

    @field:Min(value = 0, message = "El tiempo debe ser mayor o igual a 0")
    val tiempoSegundos: Int
)