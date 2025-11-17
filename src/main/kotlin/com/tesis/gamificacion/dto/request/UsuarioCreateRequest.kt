package com.tesis.gamificacion.dto.request

data class UsuarioCreateRequest (
    val username: String,
    val password: String,
    val gameTag: String,
)