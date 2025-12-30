package com.tesis.gamificacion.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class UsuarioCreateRequest (
    @field:NotBlank(message = "El nombre es requerido")
    @field:Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    val nombre: String,

    // Opcional para análisis académico
    val edadAproximada: Int? = null,
    val nivelEducativo: String? = null
)

// Response al crear usuario
data class UsuarioResponse(
    val id: Long,
    val nombre: String,
    val codigoJugador: String,
    val fechaCreacion: LocalDateTime,
    val mensaje: String  // Mensaje amigable para el usuario
)

// Request para login con código
data class LoginConCodigoRequest(
    @field:NotBlank(message = "El código es requerido")
    val codigoJugador: String
)