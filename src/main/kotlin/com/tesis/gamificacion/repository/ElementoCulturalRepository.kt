package com.tesis.gamificacion.repository

import com.tesis.gamificacion.model.entities.ElementoCultural
import com.tesis.gamificacion.model.enums.CategoriasCultural
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ElementoCulturalRepository : JpaRepository<ElementoCultural, Long> {

    fun findByCategoriaAndActivoTrue(categoria: CategoriasCultural): List<ElementoCultural>

    fun findByActivoTrue(): List<ElementoCultural>

    @Query("SELECT e FROM ElementoCultural e WHERE e.categoria = :categoria AND e.activo = true ORDER BY RANDOM()")
    fun findRandomByCategoria(categoria: CategoriasCultural): List<ElementoCultural>
}