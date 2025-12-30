package com.tesis.gamificacion.repository

import com.tesis.gamificacion.model.entities.Usuario
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
interface UsuarioRepository: JpaRepository<Usuario, Long> {
    fun findByCodigoJugador(codigoJugador: String): Usuario?

    fun findByNombreContainingIgnoreCase(nombre: String): List<Usuario>

    fun countByFechaCreacionBetween(inicio: LocalDateTime, fin: LocalDateTime): Long
}