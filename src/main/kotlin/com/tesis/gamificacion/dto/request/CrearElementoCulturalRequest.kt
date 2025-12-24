package com.tesis.gamificacion.dto.request

import com.tesis.gamificacion.model.enums.CategoriasCultural
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CrearElementoCulturalRequest(
    @field:NotBlank(message = "El nombre en kichwa es requerido")
    val nombreKichwa: String,

    @field:NotBlank(message = "El nombre en español es requerido")
    val nombreEspanol: String,

    @field:NotBlank(message = "La URL de la imagen es requerida")
    val imagenUrl: String,

    @field:NotNull(message = "La categoría es requerida")
    val categoria: CategoriasCultural,

    val descripcion: String? = null
)