package com.tesis.gamificacion.service

import com.tesis.gamificacion.dto.request.LoginConCodigoRequest
import com.tesis.gamificacion.dto.request.UsuarioCreateRequest
import com.tesis.gamificacion.dto.request.UsuarioResponse
import com.tesis.gamificacion.model.entities.Usuario
import com.tesis.gamificacion.model.enums.Severity
import com.tesis.gamificacion.repository.UsuarioRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class UsuarioService(
    private val usuarioRepository: UsuarioRepository
) {

    /**
     * Genera un código único basado en nombre y timestamp
     */
    fun generarCodigoJugador(nombre: String): String {
        // Limpiar nombre: solo letras, máximo 10 caracteres
        val nombreLimpio = nombre.uppercase()
            .replace(Regex("[^A-ZÁÉÍÓÚÑ]"), "")
            .take(10)

        // Formato: NOMBRE-YYYYMMDD-HHMM
        val timestamp = LocalDateTime.now()
        val fecha = timestamp.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val hora = timestamp.format(DateTimeFormatter.ofPattern("HHmm"))

        return "$nombreLimpio-$fecha-$hora"
    }

    /**
     * Crea un nuevo usuario (primera vez)
     */
    @Transactional
    fun crearUsuario(request: UsuarioCreateRequest): UsuarioResponse {
        val codigo = generarCodigoJugador(request.nombre)

        val usuario = Usuario(
            nombre = request.nombre.trim(),
            codigoJugador = codigo,
            edadAproximada = request.edadAproximada,
            nivelEducativo = request.nivelEducativo
        )

        val guardado = usuarioRepository.save(usuario)

        return UsuarioResponse(
            id = guardado.id!!,
            nombre = guardado.nombre,
            codigoJugador = guardado.codigoJugador,
            fechaCreacion = guardado.fechaCreacion,
            mensaje = "¡Bienvenido ${guardado.nombre}! Guarda tu código: ${guardado.codigoJugador}"
        )
    }

    /**
     * Login con código existente
     */
    @Transactional(readOnly = true)
    fun loginConCodigo(request: LoginConCodigoRequest): UsuarioResponse {
        val usuario = usuarioRepository.findByCodigoJugador(request.codigoJugador)
            ?: throw IllegalArgumentException("Código no encontrado")

        if (!usuario.activo) {
            throw IllegalArgumentException("Usuario inactivo")
        }

        return UsuarioResponse(
            id = usuario.id!!,
            nombre = usuario.nombre,
            codigoJugador = usuario.codigoJugador,
            fechaCreacion = usuario.fechaCreacion,
            mensaje = "¡Bienvenido de vuelta, ${usuario.nombre}!"
        )
    }

    /**
     * Obtener usuario por ID
     */
    @Transactional(readOnly = true)
    fun obtenerPorId(id: Long): UsuarioResponse {
        val usuario = usuarioRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Usuario no encontrado") }

        return usuario.toResponse()
    }

    private fun Usuario.toResponse() = UsuarioResponse(
        id = id!!,
        nombre = nombre,
        codigoJugador = codigoJugador,
        fechaCreacion = fechaCreacion,
        mensaje = ""
    )
}