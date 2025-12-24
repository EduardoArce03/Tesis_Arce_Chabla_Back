package com.tesis.gamificacion.dto.response

import com.tesis.gamificacion.model.enums.CategoriasCultural

data class ElementoCulturalResponse(
    val id: Long,
    val nombreKichwa: String,
    val nombreEspanol: String,
    val imagenUrl: String,
    val categoria: CategoriasCultural,
    val descripcion: String?
)