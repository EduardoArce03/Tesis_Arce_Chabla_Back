package com.tesis.gamificacion.dto

import com.tesis.gamificacion.model.entities.PowerUpPuzzle

data class ResponderDesafioRequest(
    val desafioId: Long,
    val respuestaSeleccionada: String
)

data class UsarPowerUpRequest(
    val powerUpId: Long,
    val partidaId: Long
)

data class DesafioGeneradoResponse(
    val desafioId: Long,
    val pregunta: String,
    val opciones: List<String>,
    val tiempoLimite: Int  // en segundos
)

data class ResponderDesafioResponse(
    val correcto: Boolean,
    val mensaje: String,
    val powerUpObtenido: PowerUpPuzzle?,
    val powerUpsDisponibles: List<PowerUpDisponibleDTO>,
    val tiempoBonus: Int = 0
)

data class UsarPowerUpResponse(
    val tipo: PowerUpPuzzle,
    val mensaje: String,
    val datos: Map<String, Any>  // Datos específicos según el tipo de power-up
)

data class PowerUpDisponibleDTO(
    val id: Long,
    val tipo: PowerUpPuzzle,
    val nombre: String,
    val descripcion: String,
    val icono: String
)

enum class PowerUpPuzzle {
    VISION_CONDOR,        // 👁️ Revela la imagen completa por 5 segundos
    TIEMPO_PACHAMAMA,     // ⏱️ Congela el cronómetro por 30 segundos
    SABIDURIA_AMAWTA,     // 🧠 Coloca automáticamente 1 pieza correcta
    BENDICION_SOL         // ☀️ Duplica los puntos durante 2 minutos
}