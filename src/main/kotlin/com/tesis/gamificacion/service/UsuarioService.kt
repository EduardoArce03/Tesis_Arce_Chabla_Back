package com.tesis.gamificacion.service

import com.tesis.gamificacion.dto.request.UsuarioCreateRequest
import com.tesis.gamificacion.dto.response.UsuarioCreateResponse
import com.tesis.gamificacion.exception.UPSGamificacionCustomException
import com.tesis.gamificacion.mapper.UsuarioMapper.toCreateResponse
import com.tesis.gamificacion.mapper.UsuarioMapper.toEntity
import com.tesis.gamificacion.model.enums.Severity
import com.tesis.gamificacion.repository.UsuarioRepository
import org.springframework.stereotype.Service

@Service
class UsuarioService (
    private val usuarioRepository: UsuarioRepository
) {

    fun guardarUsuario(request: UsuarioCreateRequest): UsuarioCreateResponse {
        if (usuarioRepository.existsUsuarioByUsername(request.username)) {
            throw UPSGamificacionCustomException("Error", "Este usuario ya se encuentra registrado", Severity.ERROR)
        }

        if (usuarioRepository.existsUsuarioByGameTag(request.gameTag)) {
            throw UPSGamificacionCustomException("Error", "Este gametag ya se encuentra registrado", Severity.ERROR)
        }

        if (request.password.toInt() < 8) {
            throw UPSGamificacionCustomException("Error", "La contraseña no debe tener menos de 8 caracteres", Severity.ERROR)
        }

        val usuarioEntity = request.toEntity()

        val usuario = usuarioRepository.save(usuarioEntity)
        return usuario.toCreateResponse()
    }

}