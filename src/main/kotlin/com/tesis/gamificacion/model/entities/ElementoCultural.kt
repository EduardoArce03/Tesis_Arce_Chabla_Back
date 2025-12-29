package com.tesis.gamificacion.model.entities

import com.tesis.gamificacion.model.enums.CategoriasCultural
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "elementos_culturales")
data class ElementoCultural(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val nombreKichwa: String,

    @Column(nullable = false)
    val nombreEspanol: String,

    @Column(nullable = false, length = 1000)
    val imagenUrl: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val categoria: CategoriasCultural,

    @Column(length = 2000)
    val descripcion: String? = null,

    @Column(nullable = false)
    val activo: Boolean = true
)