// src/main/kotlin/com/tesis/gamificacion/model/enums/NivelCapa.kt
package com.tesis.gamificacion.model.enums

enum class NivelCapa(val numero: Int, val nombre: String, val descripcion: String) {
    SUPERFICIE(1, "Superficie", "Hoy en día - 2025"),
    INCA(2, "Período Inca", "1470-1532 d.C."),
    CANARI(3, "Período Cañari", "500-1470 d.C."),
    ANCESTRAL(4, "Secreto Ancestral", "Mitología y Leyendas");

    companion object {
        fun fromNumero(numero: Int): NivelCapa? = values().find { it.numero == numero }
    }
}