package com.tesis.gamificacion.model.entities

import com.tesis.gamificacion.model.enums.*
import jakarta.persistence.*
import java.time.LocalDateTime

// ========== MISIÓN PRINCIPAL ==========
@Entity
@Table(name = "misiones")
data class Mision(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val titulo: String,

    @Column(nullable = false)
    val tituloKichwa: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val descripcionCorta: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val descripcionLarga: String,

    @Column(nullable = false)
    val imagenPortada: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val dificultad: DificultadMision,

    @Column(nullable = false)
    val tiempoEstimado: Int, // minutos

    @Column(nullable = false)
    val experienciaRecompensa: Int,

    @Column(nullable = false)
    val puntosRecompensa: Int,

    // NPC Guía
    @Column(nullable = false)
    val npcNombre: String,

    @Column(nullable = false)
    val npcNombreKichwa: String,

    @Column(nullable = false)
    val npcAvatar: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val npcDialogoInicial: String,

    // Requisitos (JSON)
    @Column(nullable = false)
    val nivelMinimo: Int = 1,

    @Column(nullable = true, columnDefinition = "TEXT")
    val misionesPrevias: String? = null, // JSON: [1,2,3]

    @Column(nullable = true, columnDefinition = "TEXT")
    val insigniasRequeridas: String? = null, // JSON: ["insignia_1"]

    // Orden y estado
    @Column(nullable = false)
    val orden: Int,

    @Column(nullable = false)
    val activa: Boolean = true
)

// ========== FASE DE MISIÓN ==========
@Entity
@Table(name = "fases_mision")
data class FaseMision(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val misionId: Long,

    @Column(nullable = false)
    val numeroFase: Int,

    @Column(nullable = false)
    val titulo: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val descripcion: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipoFase: TipoFase,

    // Configuración específica según tipo
    @Column(nullable = true, columnDefinition = "TEXT")
    val configuracion: String? = null, // JSON específico por tipo

    @Column(nullable = true)
    val puntoInteresId: Long? = null, // Si requiere visitar un punto

    @Column(nullable = false)
    val experienciaFase: Int = 0
)

// ========== PREGUNTA DE FASE ==========
@Entity
@Table(name = "preguntas_fase")
data class PreguntaFase(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val faseId: Long,

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
    val retroalimentacionCorrecta: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val retroalimentacionIncorrecta: String,

    @Column(nullable = false)
    val puntos: Int,

    @Column(nullable = false)
    val orden: Int
)

// ========== PROGRESO DEL USUARIO EN MISIÓN ==========
@Entity
@Table(indexes = [
    Index(name = "idx_usuario_mision", columnList = "usuario_id,mision_id")
])
data class UsuarioMision(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, name = "usuario_id")
    val usuarioId: Long,

    @Column(nullable = false, name = "mision_id")
    val misionId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val estado: EstadoMision,

    @Column(nullable = false)
    val faseActual: Int = 1,

    @Column(nullable = false)
    val puntuacion: Int = 0,

    @Column(nullable = false)
    val intentos: Int = 0,

    @Column(nullable = false)
    val respuestasCorrectas: Int = 0,

    @Column(nullable = false)
    val respuestasIncorrectas: Int = 0,

    @Column(nullable = false)
    val tiempoInicio: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = true)
    val tiempoCompletado: LocalDateTime? = null,

    @Column(nullable = true, columnDefinition = "TEXT")
    val progresoFases: String? = null // JSON: {"1": "completada", "2": "en_progreso"}
)

// ========== INSIGNIAS ==========
@Entity
@Table(name = "insignias")
data class Insignia(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    val codigo: String, // "insignia_primera_mision"

    @Column(nullable = false)
    val nombre: String,

    @Column(nullable = false)
    val nombreKichwa: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val descripcion: String,

    @Column(nullable = false)
    val icono: String, // Emoji o URL

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val rareza: RarezaInsignia,

    @Column(nullable = false)
    val activa: Boolean = true
)

// ========== INSIGNIAS DEL USUARIO ==========
@Entity
@Table(name = "usuario_insignias")
data class UsuarioInsignia(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val usuarioId: Long,

    @Column(nullable = false)
    val insigniaId: Long,

    @Column(nullable = false)
    val fechaObtencion: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    val misionId: Long? = null // En qué misión la obtuvo
)

// ========== RELACIÓN MISIÓN-INSIGNIA ==========
@Entity
@Table(name = "mision_insignias")
data class MisionInsignia(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val misionId: Long,

    @Column(nullable = false)
    val insigniaId: Long
)