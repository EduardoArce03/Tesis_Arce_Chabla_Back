package com.tesis.gamificacion.repository

import com.tesis.gamificacion.model.entities.ImagenPuzzle
import com.tesis.gamificacion.model.entities.PartidaPuzzle
import com.tesis.gamificacion.model.entities.ProgresoPuzzle
import com.tesis.gamificacion.model.enums.CategoriasCultural
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ImagenPuzzleRepository : JpaRepository<ImagenPuzzle, Long> {
    fun findByOrdenDesbloqueo(orden: Int): ImagenPuzzle?
    fun findByCategoria(categoria: CategoriasCultural): List<ImagenPuzzle>
    fun findAllByOrderByOrdenDesbloqueoAsc(): List<ImagenPuzzle>
}


@Repository
interface PartidaPuzzleRepository : JpaRepository<PartidaPuzzle, Long> {
    fun findByJugadorId(jugadorId: String): List<PartidaPuzzle>
    fun findByJugadorIdAndCompletadaTrue(jugadorId: String): List<PartidaPuzzle>
}



@Repository
interface ProgresoPuzzleRepository : JpaRepository<ProgresoPuzzle, Long> {
    fun findByJugadorId(jugadorId: String): ProgresoPuzzle?
}