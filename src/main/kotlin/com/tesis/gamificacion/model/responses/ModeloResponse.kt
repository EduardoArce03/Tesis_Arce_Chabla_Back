package com.tesis.gamificacion.model.responses

data class ModeloResponse (
    val status: String,
    val descripcion: String,
    val audio64: String?,
    val cultura: String
)