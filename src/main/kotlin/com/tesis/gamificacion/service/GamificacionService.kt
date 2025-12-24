package com.tesis.gamificacion.service

import com.tesis.gamificacion.model.enums.NivelDificultad
import org.springframework.stereotype.Service
import kotlin.math.max

@Service
class GamificacionService {

    companion object {
        private const val PUNTOS_BASE = 1000
        private const val PENALIZACION_POR_INTENTO = 10
        private const val PENALIZACION_POR_SEGUNDO = 2
        private const val BONUS_PERFECTO = 500 // Si completa sin errores
        private const val BONUS_RAPIDO = 300 // Si completa en menos de la mitad del tiempo esperado
    }

    /**
     * Calcula la puntuación basada en:
     * - Nivel de dificultad
     * - Número de intentos
     * - Tiempo transcurrido
     */
    fun calcularPuntuacion(
        nivel: NivelDificultad,
        intentos: Int,
        tiempoSegundos: Int
    ): Int {
        val intentosMinimos = nivel.pares // El mínimo es igual al número de pares
        val tiempoEsperado = calcularTiempoEsperado(nivel)

        // Puntos base multiplicados por dificultad
        var puntos = (PUNTOS_BASE * nivel.multiplicadorPuntos).toInt()

        // Penalización por intentos extras
        val intentosExtras = max(0, intentos - intentosMinimos)
        puntos -= (intentosExtras * PENALIZACION_POR_INTENTO)

        // Penalización por tiempo extra
        val tiempoExtra = max(0, tiempoSegundos - tiempoEsperado)
        puntos -= (tiempoExtra * PENALIZACION_POR_SEGUNDO)

        // Bonus por juego perfecto (sin errores)
        if (intentos == intentosMinimos) {
            puntos += BONUS_PERFECTO
        }

        // Bonus por velocidad (completar en menos de la mitad del tiempo esperado)
        if (tiempoSegundos < tiempoEsperado / 2) {
            puntos += BONUS_RAPIDO
        }

        // La puntuación nunca puede ser negativa
        return max(0, puntos)
    }

    /**
     * Calcula el tiempo esperado según el nivel
     */
    private fun calcularTiempoEsperado(nivel: NivelDificultad): Int {
        return when (nivel) {
            NivelDificultad.FACIL -> 120 // 2 minutos
            NivelDificultad.MEDIO -> 180 // 3 minutos
            NivelDificultad.DIFICIL -> 300 // 5 minutos
        }
    }

    /**
     * Determina si el jugador obtuvo una insignia especial
     */
    fun determinarInsignia(
        nivel: NivelDificultad,
        intentos: Int,
        tiempoSegundos: Int,
        puntuacion: Int
    ): String? {
        val intentosMinimos = nivel.pares
        val tiempoEsperado = calcularTiempoEsperado(nivel)

        return when {
            intentos == intentosMinimos && tiempoSegundos < tiempoEsperado / 2 -> "MAESTRO_CULTURAL"
            intentos == intentosMinimos -> "MEMORIA_PERFECTA"
            tiempoSegundos < tiempoEsperado / 2 -> "VELOCISTA"
            puntuacion >= PUNTOS_BASE * nivel.multiplicadorPuntos -> "EXPLORADOR_CULTURAL"
            else -> null
        }
    }

    /**
     * Calcula la precisión del jugador
     */
    fun calcularPrecision(intentos: Int, nivel: NivelDificultad): Double {
        val intentosMinimos = nivel.pares
        return (intentosMinimos.toDouble() / intentos) * 100
    }
}