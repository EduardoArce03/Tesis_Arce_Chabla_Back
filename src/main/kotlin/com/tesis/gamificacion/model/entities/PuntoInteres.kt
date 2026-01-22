package com.tesis.gamificacion.model.entities

import com.tesis.gamificacion.model.enums.CategoriaArtefacto
import com.tesis.gamificacion.model.enums.CategoriaPunto
import com.tesis.gamificacion.model.enums.DificultadMision
import com.tesis.gamificacion.model.enums.EstadoMision
import com.tesis.gamificacion.model.enums.NivelCapa
import com.tesis.gamificacion.model.enums.RarezaFoto
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

// ========================================
// PUNTOS DE INTERÉS (CONFIGURACIÓN)
// ========================================

@Entity
@Table(name = "puntos_interes")
data class PuntoInteres(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 200)
    val nombre: String,

    @Column(nullable = false, length = 200)
    val nombreKichwa: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val descripcion: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val historiaDetallada: String,

    @Column(nullable = false, length = 500)
    val imagenUrl: String,

    @Column(nullable = false)
    val latitud: Double, // Coordenadas geográficas reales

    @Column(nullable = false)
    val longitud: Double,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    val categoria: CategoriaPunto,

    @Column(nullable = false)
    val nivelMinimo: Int = 1, // Nivel mínimo para descubrir

    @Column(nullable = false)
    val ordenDesbloqueo: Int = 1, // Orden en el mapa

    @Column(nullable = false)
    val activo: Boolean = true,

    // Relaciones
    @OneToMany(mappedBy = "puntoInteres", cascade = [CascadeType.ALL], orphanRemoval = true)
    val capasTemporales: MutableList<CapaTemporal> = mutableListOf(),

    @OneToMany(mappedBy = "puntoInteres", cascade = [CascadeType.ALL], orphanRemoval = true)
    val descubrimientos: MutableList<PuntoDescubrimiento> = mutableListOf()
)

// ========================================
// CAPAS TEMPORALES (CONFIGURACIÓN)
// Cada punto tiene 4 capas: SUPERFICIE, INCA, CANARI, ANCESTRAL
// ========================================

@Entity
@Table(name = "capas_temporales")
data class CapaTemporal(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "punto_interes_id", nullable = false)
    val puntoInteres: PuntoInteres,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    val nivel: NivelCapa,

    @Column(nullable = false, columnDefinition = "TEXT")
    val narrativaBase: String, // Historia base de esta capa

    @Column(nullable = false, columnDefinition = "TEXT")
    val promptNarrativa: String, // Prompt para generar narrativa con IA

    // Espíritu de la capa
    @Column(nullable = false, length = 200)
    val nombreEspiritu: String, // "Willka Kamayuq"

    @Column(nullable = false, length = 200)
    val nombreEspirituKichwa: String,

    @Column(nullable = false, length = 100)
    val epocaEspiritu: String, // "1490 - Imperio Inca"

    @Column(nullable = false, length = 200)
    val personalidadEspiritu: String, // "Sabio y ceremonioso"

    @Column(nullable = false, columnDefinition = "TEXT")
    val promptEspiritu: String, // Prompt para diálogos del espíritu

    @Column(length = 500)
    val avatarEspiritu: String? = null,

    // Relaciones
    @OneToMany(mappedBy = "capaTemporal", cascade = [CascadeType.ALL], orphanRemoval = true)
    val objetivosFotograficos: MutableList<FotografiaObjetivo> = mutableListOf(),

    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
    val mision: Mision? = null
)

// ========================================
// PROGRESO DE EXPLORACIÓN (USUARIO)
// ========================================

@Entity
@Table(name = "progreso_exploracion")
data class ProgresoExploracion(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val partidaId: Long,

    @Column(nullable = false)
    val usuarioId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    var nivelActual: NivelCapa = NivelCapa.SUPERFICIE,

    // Estadísticas generales
    @Column(nullable = false)
    var puntosDescubiertos: Int = 0,

    @Column(nullable = false)
    val puntosTotales: Int = 0,

    @Column(nullable = false)
    var puntosTotal: Int = 0, // Puntos acumulados

    @Column(nullable = false)
    var misionesCompletadas: Int = 0,

    @Column(nullable = false)
    var fotografiasCapturadas: Int = 0,

    @Column(nullable = false)
    var fotosRaras: Int = 0,

    @Column(nullable = false)
    var fotosLegendarias: Int = 0,

    @Column(nullable = false)
    var dialogosRealizados: Int = 0,

    // Progreso por capa
    @Column(nullable = false)
    var capasDesbloqueadas: Int = 1,

    // Sistema de niveles
    @Column(nullable = false)
    var experienciaTotal: Int = 0,

    @Column(nullable = false)
    var nivelArqueologo: Int = 1,

    // Fechas
    @Column(nullable = false, updatable = false)
    val fechaInicio: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var ultimaActividad: LocalDateTime = LocalDateTime.now(),

    // Relaciones
    @OneToMany(mappedBy = "progreso", cascade = [CascadeType.ALL], orphanRemoval = true)
    val capasProgreso: MutableList<CapaDescubrimiento> = mutableListOf()
)

// ========================================
// CAPA DESCUBRIMIENTO (PROGRESO USUARIO EN CADA CAPA)
// Representa el progreso del usuario en cada nivel temporal
// ========================================

@Entity
@Table(
    name = "capas_descubrimiento",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["progreso_id", "nivel"])
    ]
)
data class CapaDescubrimiento(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "progreso_id", nullable = false)
    val progreso: ProgresoExploracion,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    val nivel: NivelCapa,

    @Column(nullable = false)
    var desbloqueada: Boolean = false,

    @Column(nullable = false)
    var porcentajeDescubrimiento: Double = 0.0,

    @Column(nullable = false)
    val puntosPorDescubrir: Int = 0,

    @Column
    var fechaDesbloqueo: LocalDateTime? = null,

    // Relaciones
    @OneToMany(mappedBy = "capa", cascade = [CascadeType.ALL], orphanRemoval = true)
    val dialogos: MutableList<DialogoHistorial> = mutableListOf()
)

// ========================================
// PUNTO DESCUBRIMIENTO (USUARIO DESCUBRE UN PUNTO)
// ========================================

@Entity
@Table(
    name = "puntos_descubrimiento",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["usuario_id", "punto_interes_id"])
    ]
)
data class PuntoDescubrimiento(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "punto_interes_id", nullable = false)
    val puntoInteres: PuntoInteres,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "progreso_id", nullable = false)
    val progreso: ProgresoExploracion,

    @Column(nullable = false, name = "usuario_id")
    val usuarioId: Long, // Denormalizado para queries rápidas

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    var nivelDescubrimiento: NivelCapa,

    @Column(nullable = false)
    var visitas: Int = 1,

    @Column(nullable = false)
    var tiempoExplorado: Int = 0, // En segundos

    @Column(nullable = false)
    var quizCompletado: Boolean = false,

    @Column(nullable = false, updatable = false)
    val primerDescubrimiento: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var ultimaVisita: LocalDateTime = LocalDateTime.now(),

    @Column(columnDefinition = "TEXT")
    var narrativa: String? = null
)

// ========================================
// FOTOGRAFÍA - OBJETIVOS
// ========================================

@Entity
@Table(name = "fotografia_objetivos")
data class FotografiaObjetivo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "capa_temporal_id", nullable = false)
    val capaTemporal: CapaTemporal,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "punto_interes_id", nullable = false)
    val puntoInteres: PuntoInteres, // Denormalizado para queries

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    val nivelRequerido: NivelCapa,

    @Column(nullable = false, length = 500)
    val descripcion: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    val rareza: RarezaFoto,

    @Column(nullable = false, columnDefinition = "TEXT")
    val criteriosValidacion: String,

    @Column(nullable = false)
    val esBonus: Boolean = false,

    @Column(nullable = false)
    val puntosRecompensa: Int = 0,

    @Column(nullable = false)
    val activo: Boolean = true
)

// ========================================
// FOTOGRAFÍA - CAPTURADAS
// ========================================

@Entity
@Table(name = "fotografias_capturadas")
data class FotografiaCapturada(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "objetivo_id", nullable = false)
    val objetivo: FotografiaObjetivo,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "progreso_id", nullable = false)
    val progreso: ProgresoExploracion,

    @Column(nullable = false, length = 500)
    val imagenUrl: String,

    @Column(length = 1000)
    val descripcionUsuario: String? = null,

    @Column(nullable = false, columnDefinition = "TEXT")
    val descripcionIA: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    val rarezaObtenida: RarezaFoto,

    @Column(nullable = false)
    val puntuacionIA: Double = 0.0, // Confianza 0.0-1.0

    @Column(nullable = false)
    val puntosObtenidos: Int = 0,

    @Column(nullable = false, updatable = false)
    val fecha: LocalDateTime = LocalDateTime.now()
)

// ========================================
// DIÁLOGOS CON ESPÍRITUS
// ========================================

@Entity
@Table(name = "dialogo_historial")
data class DialogoHistorial(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "progreso_id", nullable = false)
    val progreso: ProgresoExploracion,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "capa_id", nullable = false)
    val capa: CapaDescubrimiento,

    @Column(nullable = false, columnDefinition = "TEXT")
    val preguntaUsuario: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val respuestaEspiritu: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "punto_interes_id")
    val puntoInteresRelacionado: PuntoInteres? = null,

    @Column(nullable = false, updatable = false)
    val fecha: LocalDateTime = LocalDateTime.now()
)

// ========================================
// MISIONES
// ========================================

@Entity
@Table(name = "mision_progreso")
data class MisionProgreso(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "progreso_id", nullable = false)
    val progreso: ProgresoExploracion,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mision_id", nullable = false)
    val mision: Mision,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    var estado: EstadoMision = EstadoMision.NO_INICIADA,

    @Column(nullable = false)
    var faseActual: Int = 0,

    @Column(nullable = false)
    var puntuacion: Int = 0,

    @Column(columnDefinition = "TEXT")
    var respuestasJugador: String? = null,

    @Column(columnDefinition = "TEXT")
    var feedbackIA: String? = null,

    @Column(nullable = false, updatable = false)
    val fechaInicio: LocalDateTime = LocalDateTime.now(),

    @Column
    var fechaCompletado: LocalDateTime? = null
)

// ========================================
// ARTEFACTOS
// ========================================

@Entity
@Table(name = "artefactos")
data class Artefacto(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 200)
    val nombre: String,

    @Column(nullable = false, length = 200)
    val nombreKichwa: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val descripcion: String,

    @Column(nullable = false, length = 500)
    val imagenUrl: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    val categoria: CategoriaArtefacto,

    @Column(nullable = false)
    val rareza: Int, // 1-5 estrellas

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "punto_interes_id", nullable = false)
    val puntoInteres: PuntoInteres,

    @Column(nullable = false)
    val probabilidadEncuentro: Double = 0.1,

    @Column(nullable = false)
    val activo: Boolean = true
)

@Entity
@Table(
    name = "usuario_artefactos",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["progreso_id", "artefacto_id"])
    ]
)
data class UsuarioArtefacto(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "progreso_id", nullable = false)
    val progreso: ProgresoExploracion,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artefacto_id", nullable = false)
    val artefacto: Artefacto,

    @Column(nullable = false)
    val cantidad: Int = 1,

    @Column(nullable = false, updatable = false)
    val fechaEncontrado: LocalDateTime = LocalDateTime.now()
)

// ========================================
// PREGUNTAS QUIZ
// ========================================

@Entity
@Table(name = "preguntas_quiz")
data class PreguntaQuiz(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "punto_interes_id", nullable = false)
    val puntoInteres: PuntoInteres,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    val nivelCapa: NivelCapa,

    @Column(nullable = false, columnDefinition = "TEXT")
    val pregunta: String,

    @Column(nullable = false, length = 500)
    val opcionA: String,

    @Column(nullable = false, length = 500)
    val opcionB: String,

    @Column(nullable = false, length = 500)
    val opcionC: String,

    @Column(nullable = false, length = 500)
    val opcionD: String,

    @Column(nullable = false, length = 1)
    val respuestaCorrecta: String, // A, B, C, D

    @Column(nullable = false, columnDefinition = "TEXT")
    val explicacion: String,

    @Column(nullable = false)
    val dificultad: Int = 1, // 1-3

    @Column(nullable = false)
    val puntos: Int = 10,

    @Column(nullable = false)
    val activa: Boolean = true
)