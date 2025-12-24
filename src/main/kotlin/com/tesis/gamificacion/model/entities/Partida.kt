package com.tesis.gamificacion.model.entities

import com.tesis.gamificacion.model.enums.CategoriasCultural
import com.tesis.gamificacion.model.enums.NivelDificultad
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "partidas")
data class Partida(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val jugadorId: String, // Podría ser un UUID o identificador de sesión

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val nivel: NivelDificultad,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val categoria: CategoriasCultural,

    @Column(nullable = false)
    val intentos: Int,

    @Column(nullable = false)
    val tiempoSegundos: Int,

    @Column(nullable = false)
    val puntuacion: Int,

    @Column(nullable = false)
    val completada: Boolean = false,

    @Column(nullable = false)
    val fechaInicio: LocalDateTime = LocalDateTime.now(),

    @Column
    val fechaFin: LocalDateTime? = null
)