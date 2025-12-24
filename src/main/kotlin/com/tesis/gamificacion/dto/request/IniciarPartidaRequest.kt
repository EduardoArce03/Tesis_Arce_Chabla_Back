package com.tesis.gamificacion.dto.request

import com.tesis.gamificacion.model.enums.CategoriasCultural
import com.tesis.gamificacion.model.enums.NivelDificultad
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class IniciarPartidaRequest(
    @field:NotBlank(message = "El ID del jugador es requerido")
    val jugadorId: String,

    @field:NotNull(message = "El nivel es requerido")
    val nivel: NivelDificultad,

    @field:NotNull(message = "La categoría es requerida")
    val categoria: CategoriasCultural
)