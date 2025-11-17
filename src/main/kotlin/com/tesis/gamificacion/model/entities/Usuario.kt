package com.tesis.gamificacion.model.entities

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.util.*

@Entity
data class Usuario (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var uuid: UUID? = UUID.randomUUID(),

    var username: String,

    var password: String ,

    var gameTag: String ,
) {
}