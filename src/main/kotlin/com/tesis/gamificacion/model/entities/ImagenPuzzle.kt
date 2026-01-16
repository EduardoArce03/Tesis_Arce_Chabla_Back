// src/main/kotlin/com/tesis/gamificacion/model/entities/ImagenPuzzle.kt
package com.tesis.gamificacion.model.entities

import com.tesis.gamificacion.model.enums.CategoriasCultural
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "imagenes_puzzle")
data class ImagenPuzzle(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val titulo: String,

    @Column(nullable = false)
    val nombreKichwa: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val categoria: CategoriasCultural,

//    @Column(nullable = false)
//    val imagenPath: String? = null,  // "lugares/ingapirca.jpg"

    @Column(nullable = false, length = 500)
    val imagenUrl: String,

    @Column(columnDefinition = "TEXT")
    val descripcionCompleta: String? = null,

    @Column(nullable = false)
    val dificultadMinima: Int = 3,  // Mínimo 3x3

    @Column(nullable = false)
    val dificultadMaxima: Int = 6,  // Máximo 6x6

    @Column(nullable = false)
    var desbloqueada: Boolean = false,

    @Column(nullable = false)
    val ordenDesbloqueo: Int = 1
)

@Entity
@Table(name = "partidas_puzzle")
data class PartidaPuzzle(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 100)
    val jugadorId: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "imagen_id", nullable = false)
    val imagen: ImagenPuzzle,

    @Column(nullable = false)
    val gridSize: Int,

    @Column(nullable = false)
    val tiempoLimiteSegundos: Int, // ⬅️ NUEVO: Tiempo inicial dado

    @Column(nullable = false)
    var completada: Boolean = false,

    @Column
    var movimientos: Int? = null,

    @Column
    var tiempoRestanteSegundos: Int? = null, // ⬅️ CAMBIADO: De tiempoSegundos a tiempoRestante

    @Column
    var hintsUsados: Int? = null,

    @Column
    var estrellas: Int? = null,

    @Column
    var puntosObtenidos: Int? = null,

    @Column(nullable = false, updatable = false)
    val fechaInicio: LocalDateTime = LocalDateTime.now(),

    @Column
    var fechaFin: LocalDateTime? = null
)

@Entity
@Table(name = "progreso_puzzle")
data class ProgresoPuzzle(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true, length = 100)
    val jugadorId: String,

    @Column(nullable = false)
    var estrellasTotal: Int = 0,

    @Column(nullable = false)
    var puntosTotal: Int = 0, // ⬅️ Este campo debe existir

    @Column(nullable = false)
    var puzzlesCompletados: Int = 0,

    @Column(nullable = false)
    var mejorTiempo: Int = Int.MAX_VALUE,

    @ElementCollection
    @CollectionTable(
        name = "imagenes_desbloqueadas",
        joinColumns = [JoinColumn(name = "progreso_id")]
    )
    @Column(name = "imagen_id")
    val imagenesDesbloqueadas: MutableSet<Long> = mutableSetOf()
)