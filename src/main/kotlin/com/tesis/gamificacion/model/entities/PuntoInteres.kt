package com.tesis.gamificacion.model.entities

import com.tesis.gamificacion.model.enums.CategoriaArtefacto
import com.tesis.gamificacion.model.enums.*
import jakarta.persistence.*
import java.time.LocalDateTime

// Puntos de Interés
@Entity
@Table(name = "puntos_interes")
data class PuntoInteres(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val nombre: String,

    @Column(nullable = false)
    val nombreKichwa: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val descripcion: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val historiaDetallada: String,

    @Column(nullable = false)
    val imagenUrl: String,

    @Column(nullable = false)
    val coordenadaX: Double,

    @Column(nullable = false)
    val coordenadaY: Double,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val categoria: CategoriaPunto,

    @Column(nullable = false)
    val nivelRequerido: Int = 1,

    @Column(nullable = false)
    val puntosPorDescubrir: Int = 100,

    @Column(nullable = false)
    val activo: Boolean = true
)

// Progreso del Usuario
@Entity
@Table(name = "progreso_exploracion")
data class ProgresoExploracion(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val usuarioId: Long,

    @Column(nullable = false)
    var nivelArqueologo: Int = 1,

    @Column(nullable = false)
    var experienciaTotal: Int = 0,

    @Column(nullable = false)
    var puntosDescubiertos: Int = 0,

    @Column(nullable = false)
    var artefactosEncontrados: Int = 0,

    @Column(nullable = false)
    val misionesCompletadas: Int = 0,

    @Column(nullable = false)
    val fechaCreacion: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var ultimaVisita: LocalDateTime = LocalDateTime.now()
)

// Descubrimientos del Usuario
@Entity
@Table(name = "descubrimientos")
data class Descubrimiento(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, name = "usuario_id")
    val usuarioId: Long,

    @Column(nullable = false, name = "punto_id")
    val puntoId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val nivelDescubrimiento: NivelDescubrimiento,

    @Column(nullable = false)
    val visitas: Int = 1,

    @Column(nullable = false)
    val tiempoExplorado: Int = 0, // segundos

    @Column(nullable = false)
    val quizCompletado: Boolean = false,

    @Column(nullable = false)
    val fechaPrimerDescubrimiento: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    val fechaUltimaVisita: LocalDateTime = LocalDateTime.now()
)

// Artefactos Coleccionables
@Entity
@Table(name = "artefactos")
data class Artefacto(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val nombre: String,

    @Column(nullable = false)
    val nombreKichwa: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val descripcion: String,

    @Column(nullable = false)
    val imagenUrl: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val categoria: CategoriaArtefacto,

    @Column(nullable = false)
    val rareza: Int, // 1-5 estrellas

    @Column(nullable = false)
    val puntoInteresId: Long,

    @Column(nullable = false)
    val probabilidadEncuentro: Double, // 0.0 - 1.0

    @Column(nullable = false)
    val activo: Boolean = true
)

// Colección de Artefactos del Usuario
@Entity
@Table(name = "usuario_artefactos")
data class UsuarioArtefacto(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val usuarioId: Long,

    @Column(nullable = false)
    val artefactoId: Long,

    @Column(nullable = false)
    val fechaEncontrado: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    val cantidad: Int = 1
)

// Misiones de Exploración
@Entity
@Table(name = "misiones_exploracion")
data class MisionExploracion(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val titulo: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val descripcion: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipo: TipoMision,

    @Column(nullable = false)
    val objetivo: String, // JSON: {"puntos": [1,2,3], "cantidad": 3}

    @Column(nullable = false)
    val recompensaXP: Int,

    @Column(nullable = false)
    val recompensaPuntos: Int,

    @Column(nullable = false)
    val nivelRequerido: Int = 1,

    @Column(nullable = true)
    val duracionDias: Int? = null,

    @Column(nullable = false)
    val activa: Boolean = true
)

// Preguntas del Quiz
@Entity
@Table(name = "preguntas_quiz")
data class PreguntaQuiz(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val puntoInteresId: Long,

    @Column(nullable = false, columnDefinition = "TEXT")
    val pregunta: String,

    @Column(nullable = false)
    val opcionA: String,

    @Column(nullable = false)
    val opcionB: String,

    @Column(nullable = false)
    val opcionC: String,

    @Column(nullable = false)
    val opcionD: String,

    @Column(nullable = false)
    val respuestaCorrecta: String, // A, B, C, D

    @Column(nullable = false, columnDefinition = "TEXT")
    val explicacion: String,

    @Column(nullable = false)
    val dificultad: Int, // 1-3

    @Column(nullable = false)
    val activa: Boolean = true
)