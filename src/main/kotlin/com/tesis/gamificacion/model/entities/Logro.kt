package com.tesis.gamificacion.model.entities

import com.tesis.gamificacion.model.enums.CategoriaLogro
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne

@Entity
data class Logro(
    @Id @GeneratedValue
    val id: Long = 0,
    val nombre: String,
    val descripcion: String,
    val categoria: CategoriaLogro, // INCA, CAÑARI, MIXTO
    val puntosRequeridos: Int,
    val iconoUrl: String,
    @ManyToOne
    val usuario: Usuario
)
