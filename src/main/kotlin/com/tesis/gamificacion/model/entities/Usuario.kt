package com.tesis.gamificacion.model.entities

import jakarta.persistence.*
import java.util.*

@Entity
data class Usuario (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var uuid: UUID? = UUID.randomUUID(),

    var username: String,

    var password: String ,

    var gameTag: String ,

    var puntosInti: Int = 0,
    var nivelChakana: Int = 1,
    @OneToMany(mappedBy = "usuario")
    val logros: MutableList<Logro> = mutableListOf()
) {
}