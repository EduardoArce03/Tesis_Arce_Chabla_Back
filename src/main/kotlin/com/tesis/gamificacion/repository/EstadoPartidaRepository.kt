package com.tesis.gamificacion.repository

import com.tesis.gamificacion.model.entities.EstadoPartida
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EstadoPartidaRepository : JpaRepository<EstadoPartida, Long> {
    fun findByPartidaId(partidaId: Long): EstadoPartida?
}