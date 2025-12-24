package com.tesis.gamificacion.model.enums

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id

@Entity
data class Desafio(
    @Id @GeneratedValue
    val id: Long = 0,
    val titulo: String,
    val narrativaGenerada: String, // Del modelo BLIP-2
    val imagenReferencia: String,
    val dificultad: Dificultad,
    val cultura: String = "CAÑARI", // INCA, CAÑARI
    val recompensaInti: Int
)

enum class Dificultad{
    FACIL, MEDIO, DIFICIL
}