package com.tesis.gamificacion.model.entities

import com.tesis.gamificacion.model.enums.CategoriasCultural
import com.tesis.gamificacion.model.enums.NivelDificultad
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
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
    val intentos: Int ,

    @Column(nullable = false)
    val tiempoSegundos: Int ,

    @Column(nullable = false)
    val puntuacion: Int,

    @Column(nullable = false)
    var completada: Boolean = false,

    @Column(nullable = false)
    val fechaInicio: LocalDateTime = LocalDateTime.now(),

    @Column
    var fechaFin: LocalDateTime? = null,

    //exploracion

    var puntosExplorados: Int = 0,

    var fotografiasCapturadas: Int = 0,

    var dialogosRealizados: Int = 0,

    var puntuacionTotal: Int = 0,

    @OneToMany(mappedBy = "partida", cascade = [CascadeType.ALL])
    val progresosCapas: MutableList<ProgresoCapa> = mutableListOf()
)