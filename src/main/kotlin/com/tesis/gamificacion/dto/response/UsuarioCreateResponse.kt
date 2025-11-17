package com.tesis.gamificacion.dto.response

data class UsuarioCreateResponse (
    val username: String,
    val password: String,
    val gameTag: String,
)