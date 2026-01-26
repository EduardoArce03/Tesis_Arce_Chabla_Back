package com.tesis.gamificacion.model.request

import com.tesis.gamificacion.model.enums.CategoriasCultural
import com.tesis.gamificacion.model.enums.NivelDificultad
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull


/**
 * DTO para recibir datos de una partida a guardar
 */
data class GuardarPartidaRequest(
    @field:NotBlank(message = "El ID del jugador es requerido")
    val jugadorId: String,

    @field:NotNull(message = "El nivel es requerido")
    val nivel: NivelDificultad,

    @field:NotNull(message = "La categoría es requerida")
    val categoria: CategoriasCultural,

    @field:Min(value = 0, message = "La puntuación no puede ser negativa")
    val puntuacion: Int,

    @field:Min(value = 1, message = "Los intentos deben ser al menos 1")
    val intentos: Int,

    @field:Min(value = 0, message = "El tiempo no puede ser negativo")
    val tiempoSegundos: Int,

    @field:NotNull(message = "Debe indicar si la partida fue completada")
    val completada: Boolean
)