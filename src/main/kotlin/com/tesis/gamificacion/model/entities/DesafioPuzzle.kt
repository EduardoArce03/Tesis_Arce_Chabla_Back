package com.tesis.gamificacion.model.entities

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "desafios_puzzle")
data class DesafioPuzzle(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val partidaId: Long,

    @Column(nullable = false)
    val pregunta: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val opciones: String,  // JSON: ["A", "B", "C"]

    @Column(nullable = false)
    val respuestaCorrecta: String,

    @Column(nullable = false)
    var respondida: Boolean = false,

    @Column(nullable = false)
    var correcta: Boolean = false,

    @Enumerated(EnumType.STRING)
    var powerUpObtenido: PowerUpPuzzle? = null,

    val fechaCreacion: LocalDateTime = LocalDateTime.now(),
    var fechaRespuesta: LocalDateTime? = null
)

enum class PowerUpPuzzle {
    VISION_CONDOR,       // Ver imagen completa 5s
    TIEMPO_PACHAMAMA,    // Congelar cronómetro 30s
    SABIDURIA_AMAWTA,    // Auto-colocar 1 pieza
    BENDICION_SOL        // x2 puntos siguientes 2min
}

@Entity
@Table(name = "power_ups_activos")
data class PowerUpActivo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val partidaId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipo: PowerUpPuzzle,

    @Column(nullable = false)
    var usado: Boolean = false,

    val fechaObtenido: LocalDateTime = LocalDateTime.now(),
    var fechaUsado: LocalDateTime? = null
)