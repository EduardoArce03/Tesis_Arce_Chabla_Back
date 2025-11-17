package com.tesis.gamificacion.mapper

import com.tesis.gamificacion.dto.request.UsuarioCreateRequest
import com.tesis.gamificacion.dto.response.UsuarioCreateResponse
import com.tesis.gamificacion.model.entities.Usuario

object UsuarioMapper {

    fun UsuarioCreateRequest.toEntity(): Usuario {
        return Usuario(
            username = this.username,
            password = this.password,
            gameTag = this.gameTag,
        )
    }

    fun Usuario.toCreateResponse(): UsuarioCreateResponse {
        return UsuarioCreateResponse(
            username = this.username,
            password = this.password,
            gameTag = this.gameTag,
        )
    }

}