package com.tesis.gamificacion.service

import com.tesis.gamificacion.model.entities.EstadoPartida
import com.tesis.gamificacion.repository.EstadoPartidaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.max
import kotlin.math.min

@Service
class GamificacionAvanzadaService(
    private val estadoPartidaRepository: EstadoPartidaRepository
) {

    companion object {
        private const val COSTO_HINT = 50
        private const val MAX_HINTS = 3

        private const val ERRORES_PARA_PREGUNTA = 2  // Cada 2 errores
        private const val VIDAS_MINIMAS_PARA_PREGUNTA = 2  // Solo si tienes ≤2 vidas

    }

    /**
     * Inicializa el estado de gamificación para una partida
     */
    @Transactional
    fun inicializarEstadoPartida(partidaId: Long): EstadoPartida {
        val estado = EstadoPartida(
            partidaId = partidaId,
            vidasActuales = 3,
            vidasMaximas = 3,
            hintsDisponibles = MAX_HINTS
        )
        return estadoPartidaRepository.save(estado)
    }

    /**
     * Procesa un error (pareja incorrecta)
     */
    @Transactional
    fun procesarError(partidaId: Long): Pair<EstadoPartida, Boolean> {
        val estado = obtenerEstadoPartida(partidaId)

        // Perder vida
        estado.vidasActuales = max(0, estado.vidasActuales - 1)
        estado.erroresConsecutivos++
        estado.erroresSinPregunta++

        // Romper combo
        if (estado.parejasConsecutivas > 0) {
            estado.mejorCombo = max(estado.mejorCombo, estado.parejasConsecutivas)
        }
        estado.parejasConsecutivas = 0
        estado.multiplicadorActual = 1.0

        // ⬇️ DECIDIR SI MOSTRAR PREGUNTA
        val debesMostrarPregunta = debesMostrarPreguntaRecuperacion(estado)

        if (debesMostrarPregunta) {
            estado.erroresSinPregunta = 0  // Resetear contador
        }

        val estadoActualizado = estadoPartidaRepository.save(estado)
        return Pair(estadoActualizado, debesMostrarPregunta)
    }



    /**
     * Determina si debe mostrar pregunta de recuperación
     */
    private fun debesMostrarPreguntaRecuperacion(estado: EstadoPartida): Boolean {
        // Condición 1: Al menos 2 errores desde la última pregunta
        val suficientesErrores = estado.erroresSinPregunta >= ERRORES_PARA_PREGUNTA

        // Condición 2: Tiene 2 o menos vidas (está en riesgo)
        val vidasBajas = estado.vidasActuales <= VIDAS_MINIMAS_PARA_PREGUNTA

        // Condición 3: Aún tiene vidas (si ya murió, no mostrar)
        val tieneVidas = estado.vidasActuales > 0

        return suficientesErrores && vidasBajas && tieneVidas
    }

    /**
     * Procesa una pareja correcta
     */
    @Transactional
    fun procesarParejaCorrecta(partidaId: Long, elementoId: Long): EstadoPartida {
        val estado = obtenerEstadoPartida(partidaId)

        // Incrementar combo
        estado.parejasConsecutivas++
        estado.erroresConsecutivos = 0
        estado.multiplicadorActual = calcularMultiplicador(estado.parejasConsecutivas)

        // Registrar descubrimiento
        estado.elementosDescubiertos.add(elementoId)

        return estadoPartidaRepository.save(estado)
    }

    /**
     * Procesa el uso de un hint
     */
    @Transactional
    fun usarHint(partidaId: Long): Pair<EstadoPartida, Int> {
        val estado = obtenerEstadoPartida(partidaId)

        if (estado.hintsDisponibles <= 0) {
            throw IllegalStateException("No quedan hints disponibles")
        }

        estado.hintsDisponibles--
        estado.hintsUsados++

        val estadoActualizado = estadoPartidaRepository.save(estado)
        return Pair(estadoActualizado, COSTO_HINT)
    }

    /**
     * Recupera una vida al responder correctamente
     */
    @Transactional
    fun recuperarVida(partidaId: Long): EstadoPartida {
        val estado = obtenerEstadoPartida(partidaId)
        estado.vidasActuales = min(estado.vidasMaximas, estado.vidasActuales + 1)
        estado.erroresSinPregunta = 0  // Resetear contador también aquí
        return estadoPartidaRepository.save(estado)
    }

    /**
     * Calcula la puntuación total con multiplicadores
     */
    fun calcularPuntuacionConMultiplicador(
        puntuacionBase: Int,
        estado: EstadoPartida
    ): Int {
        var puntos = puntuacionBase.toDouble()

        // Multiplicador de combo
        if (estado.mejorCombo >= 3) {
            puntos *= 1.2
        }

        // Bonus por vidas restantes
        puntos += (estado.vidasActuales * 100)

        // Penalización por hints usados
        puntos -= (estado.hintsUsados * COSTO_HINT)

        return max(0, puntos.toInt())
    }

    /**
     * Obtiene el estado de una partida
     */
    @Transactional(readOnly = true)
    fun obtenerEstadoPartida(partidaId: Long): EstadoPartida {
        return estadoPartidaRepository.findByPartidaId(partidaId)
            ?: throw IllegalArgumentException("Estado de partida no encontrado: $partidaId")
    }

    /**
     * Verifica si es pareja perfecta (primer intento sin errores previos)
     */
    fun esParejaLimpia(estado: EstadoPartida, elementoId: Long): Boolean {
        return elementoId !in estado.elementosDescubiertos && estado.erroresConsecutivos == 0
    }

    // ==================== HELPERS ====================

    private fun calcularMultiplicador(parejas: Int): Double {
        return when {
            parejas >= 5 -> 3.0  // SUPER COMBO
            parejas >= 3 -> 2.0  // GRAN COMBO
            parejas >= 2 -> 1.5  // COMBO
            else -> 1.0
        }
    }
}