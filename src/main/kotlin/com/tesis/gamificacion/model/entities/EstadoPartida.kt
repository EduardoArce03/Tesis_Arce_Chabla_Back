package com.tesis.gamificacion.model.entities

import com.tesis.gamificacion.model.enums.TipoDialogo
import jakarta.persistence.*
import java.time.LocalDateTime

// src/main/kotlin/com/tesis/gamificacion/model/entities/EstadoPartida.kt

@Entity
@Table(name = "estado_partida")
data class EstadoPartida(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val partidaId: Long,

    // Sistema de Vidas
    @Column(nullable = false)
    var vidasActuales: Int = 3,

    @Column(nullable = false)
    val vidasMaximas: Int = 3,

    @Column(nullable = false)
    var erroresConsecutivos: Int = 0,

    // ⬇️ NUEVO: Contador de errores sin mostrar pregunta
    @Column(nullable = false, name = "errores_sin_pregunta")
    var erroresSinPregunta: Int = 0,

    // Sistema de Combos
    @Column(nullable = false)
    var parejasConsecutivas: Int = 0,

    @Column(nullable = false)
    var multiplicadorActual: Double = 1.0,

    @Column(nullable = false)
    var mejorCombo: Int = 0,

    // Sistema de Hints
    @Column(nullable = false)
    var hintsDisponibles: Int = 3,

    @Column(nullable = false)
    var hintsUsados: Int = 0,

    // Tracking de elementos
    @ElementCollection
    @CollectionTable(name = "elementos_descubiertos", joinColumns = [JoinColumn(name = "estado_partida_id")])
    @Column(name = "elemento_id")
    var elementosDescubiertos: MutableSet<Long> = mutableSetOf(),

    @Column(nullable = false)
    val fechaCreacion: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var fechaActualizacion: LocalDateTime = LocalDateTime.now()
)

// DTOs para respuestas
data class EstadoVidasDTO(
    val vidasActuales: Int,
    val vidasMaximas: Int,
    val erroresConsecutivos: Int
)

data class EstadoComboDTO(
    val parejasConsecutivas: Int,
    val multiplicador: Double,
    val comboActivo: Boolean,
    val mejorCombo: Int
)

data class HintDisponibleDTO(
    val costo: Int = 50,
    val usosRestantes: Int,
    val mensaje: String? = null
)