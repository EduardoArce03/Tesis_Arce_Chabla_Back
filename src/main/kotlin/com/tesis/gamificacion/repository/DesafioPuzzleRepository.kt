// src/main/kotlin/com/tesis/gamificacion/repository/DesafioPuzzleRepository.kt
package com.tesis.gamificacion.repository

import com.tesis.gamificacion.model.entities.DesafioPuzzle
import com.tesis.gamificacion.model.entities.PowerUpActivo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DesafioPuzzleRepository : JpaRepository<DesafioPuzzle, Long> {
    fun findByPartidaId(partidaId: Long): List<DesafioPuzzle>
    fun countByPartidaIdAndRespondidaTrue(partidaId: Long): Int
}

@Repository
interface PowerUpActivoRepository : JpaRepository<PowerUpActivo, Long> {
    fun findByPartidaIdAndUsadoFalse(partidaId: Long): List<PowerUpActivo>
    fun countByPartidaIdAndUsadoFalse(partidaId: Long): Int
}