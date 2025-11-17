package com.tesis.gamificacion.repository

import com.tesis.gamificacion.model.entities.Usuario
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UsuarioRepository: JpaRepository<Usuario, UUID> {
    fun existsUsuarioByUsername(username: String): Boolean
    fun existsUsuarioByGameTag(gameTag: String): Boolean
}