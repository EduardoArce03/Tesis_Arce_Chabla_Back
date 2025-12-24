package com.tesis.gamificacion.repository

import com.tesis.gamificacion.model.entities.Partida
import com.tesis.gamificacion.model.enums.CategoriasCultural
import com.tesis.gamificacion.model.enums.NivelDificultad
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface PartidaRepository : JpaRepository<Partida, Long> {

    fun findByJugadorIdOrderByFechaInicioDesc(jugadorId: String): List<Partida>

    fun findByJugadorIdAndCompletadaTrueOrderByPuntuacionDesc(jugadorId: String): List<Partida>

    @Query("SELECT p FROM Partida p WHERE p.completada = true ORDER BY p.puntuacion DESC")
    fun findTopScores(limit: Int = 10): List<Partida>

    @Query("SELECT p FROM Partida p WHERE p.nivel = :nivel AND p.categoria = :categoria AND p.completada = true ORDER BY p.puntuacion DESC")
    fun findTopScoresByNivelAndCategoria(nivel: NivelDificultad, categoria: CategoriasCultural): List<Partida>

    @Query("SELECT AVG(p.puntuacion) FROM Partida p WHERE p.jugadorId = :jugadorId AND p.completada = true")
    fun findAverageScoreByJugador(jugadorId: String): Double?
}