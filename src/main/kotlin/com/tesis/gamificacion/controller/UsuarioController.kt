package com.tesis.gamificacion.controller

import com.tesis.gamificacion.dto.request.UsuarioCreateRequest
import com.tesis.gamificacion.dto.response.UsuarioCreateResponse
import com.tesis.gamificacion.model.entities.Usuario
import com.tesis.gamificacion.service.UsuarioService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/usuarios")
class UsuarioController (
    private val usuarioService: UsuarioService
){

    @PostMapping
    fun create(@RequestBody request: UsuarioCreateRequest): ResponseEntity<UsuarioCreateResponse> {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(usuarioService.guardarUsuario(request))
    }

}