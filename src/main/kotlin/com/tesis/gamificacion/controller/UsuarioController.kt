package com.tesis.gamificacion.controller

import com.tesis.gamificacion.dto.request.LoginConCodigoRequest
import com.tesis.gamificacion.dto.request.UsuarioCreateRequest
import com.tesis.gamificacion.dto.request.UsuarioResponse
import com.tesis.gamificacion.dto.response.UsuarioCreateResponse
import com.tesis.gamificacion.model.entities.Usuario
import com.tesis.gamificacion.service.UsuarioService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/usuarios")
class UsuarioController(
    private val usuarioService: UsuarioService
) {

    /**
     * POST /api/usuarios/registrar
     * Crear nuevo usuario (primera vez)
     */
    @PostMapping("/registrar")
    fun registrar(
        @Valid @RequestBody request: UsuarioCreateRequest
    ): ResponseEntity<UsuarioResponse> {
        val response = usuarioService.crearUsuario(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    /**
     * POST /api/usuarios/login
     * Login con código existente
     */
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginConCodigoRequest
    ): ResponseEntity<UsuarioResponse> {
        val response = usuarioService.loginConCodigo(request)
        return ResponseEntity.ok(response)
    }

    /**
     * GET /api/usuarios/{id}
     * Obtener datos del usuario
     */
    @GetMapping("/{id}")
    fun obtener(@PathVariable id: Long): ResponseEntity<UsuarioResponse> {
        val response = usuarioService.obtenerPorId(id)
        return ResponseEntity.ok(response)
    }
}