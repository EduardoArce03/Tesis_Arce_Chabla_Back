package com.tesis.gamificacion.model.entities

import com.tesis.gamificacion.model.enums.CapaNivel
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
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

// 1. Partida (Sesión de juego del usuario)

// 2. Progreso de Capa (Una fila por cada punto+capa que el usuario explore)
@Entity
@Table(
    name = "progresos_capas",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["partida_id", "punto_id", "capa_nivel"])
    ]
)
data class ProgresoCapa(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partida_id", nullable = false)
    val partida: Partida,

    @Column(name = "punto_id", nullable = false)
    val puntoId: Int, // ID del enum PuntoInteres

    @Enumerated(EnumType.STRING)
    @Column(name = "capa_nivel", nullable = false)
    val capaNivel: CapaNivel,

    var desbloqueada: Boolean = false,

    var narrativaLeida: Boolean = false,

    var fotografiasCompletadas: Int = 0,

    var fotografiasRequeridas: Int = 0,

    var dialogosRealizados: Int = 0,

    var completada: Boolean = false,

    @Column(nullable = false)
    val fechaCreacion: LocalDateTime = LocalDateTime.now(),

    var fechaDesbloqueo: LocalDateTime? = null,

    var fechaCompletado: LocalDateTime? = null,

    @OneToMany(mappedBy = "progresoCapa", cascade = [CascadeType.ALL])
    val fotografiasCapturadas: MutableList<FotografiaCapturada> = mutableListOf(),

    @OneToMany(mappedBy = "progresoCapa", cascade = [CascadeType.ALL])
    val dialogos: MutableList<DialogoHistorial> = mutableListOf()
)

// 3. Fotografía Capturada
@Entity
@Table(name = "fotografias_capturadas")
data class FotografiaCapturada(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "progreso_capa_id", nullable = false)
    val progresoCapa: ProgresoCapa,

    @Column(nullable = false)
    val objetivoId: Int, // ID del ObjetivoFoto

    @Column(columnDefinition = "TEXT")
    val imagenBase64: String? = null,

    @Column(columnDefinition = "TEXT")
    val descripcionIA: String? = null,

    var validadaPorIA: Boolean = false,

    @Column(nullable = false)
    val fecha: LocalDateTime = LocalDateTime.now()
)

// 4. Diálogo con Espíritu
@Entity
@Table(name = "dialogos_historial")
data class DialogoHistorial(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "progreso_capa_id", nullable = false)
    val progresoCapa: ProgresoCapa,

    @Column(columnDefinition = "TEXT", nullable = false)
    val preguntaUsuario: String,

    @Column(columnDefinition = "TEXT", nullable = false)
    val respuestaEspiritu: String,

    @Column(nullable = false)
    val fecha: LocalDateTime = LocalDateTime.now()
)