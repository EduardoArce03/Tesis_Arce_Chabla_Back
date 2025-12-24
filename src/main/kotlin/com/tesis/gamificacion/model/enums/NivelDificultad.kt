package com.tesis.gamificacion.model.enums

enum class NivelDificultad(val pares: Int, val multiplicadorPuntos: Double) {
    FACIL(6, 1.0),
    MEDIO(8, 1.5),
    DIFICIL(12, 2.0)
}