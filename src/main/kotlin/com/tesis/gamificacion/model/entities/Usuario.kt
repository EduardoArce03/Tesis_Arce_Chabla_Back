package com.tesis.gamificacion.model.entities

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "usuarios")
data class Usuario(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 100)
    val nombre: String,

    @Column(unique = true, nullable = false, length = 50)
    val codigoJugador: String,  // Ej: "MARIA-20241228-1430"

    @Column(nullable = false)
    val fechaCreacion: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    val activo: Boolean = true,

    // Opcional: para análisis de tesis
    @Column
    val edadAproximada: Int? = null,

    @Column
    val nivelEducativo: String? = null
)