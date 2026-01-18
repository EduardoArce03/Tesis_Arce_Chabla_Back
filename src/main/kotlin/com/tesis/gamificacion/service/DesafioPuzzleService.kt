// src/main/kotlin/com/tesis/gamificacion/service/DesafioPuzzleService.kt
package com.tesis.gamificacion.service

import com.tesis.gamificacion.dto.DesafioGeneradoResponse
import com.tesis.gamificacion.dto.PowerUpDisponibleDTO
import com.tesis.gamificacion.dto.ResponderDesafioRequest
import com.tesis.gamificacion.dto.ResponderDesafioResponse
import com.tesis.gamificacion.dto.UsarPowerUpRequest
import com.tesis.gamificacion.dto.UsarPowerUpResponse
import com.tesis.gamificacion.model.entities.*
import com.tesis.gamificacion.repository.*
import com.tesis.gamificacion.dto.request.*
import com.tesis.gamificacion.dto.response.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.random.Random

@Service
class DesafioPuzzleService(
    private val desafioPuzzleRepository: DesafioPuzzleRepository,
    private val powerUpActivoRepository: PowerUpActivoRepository,
    private val partidaPuzzleRepository: PartidaPuzzleRepository,
    private val imagenPuzzleRepository: ImagenPuzzleRepository,
    private val modeloService: ModeloService,
    private val restTemplate: org.springframework.web.client.RestTemplate
) {

    companion object {
        private const val MAX_POWER_UPS = 3
    }

    /**
     * Genera un nuevo desafío cultural con IA
     */
    @Transactional
    fun generarDesafio(partidaId: Long): DesafioGeneradoResponse {
        val partida = partidaPuzzleRepository.findById(partidaId)
            .orElseThrow { IllegalArgumentException("Partida no encontrada") }

        val imagen = partida.imagen

        // Descargar imagen
        val imagenBytes = descargarImagenDesdeUrl(imagen.imagenUrl)

        // Generar pregunta con IA
        val preguntaIA = modeloService.generarPreguntaRapidaPuzzle(
            imagenBytes = imagenBytes,
            titulo = imagen.titulo,
            nombreKichwa = imagen.nombreKichwa,
            categoria = imagen.categoria.name
        )

        // Guardar desafío
        val desafio = DesafioPuzzle(
            partidaId = partidaId,
            pregunta = preguntaIA.pregunta,
            opciones = preguntaIA.opciones.joinToString("|"),  // Serialize
            respuestaCorrecta = preguntaIA.respuestaCorrecta
        )

        val desafioGuardado = desafioPuzzleRepository.save(desafio)

        println("🎲 Desafío generado para partida $partidaId")

        return DesafioGeneradoResponse(
            desafioId = desafioGuardado.id!!,
            pregunta = preguntaIA.pregunta,
            opciones = preguntaIA.opciones,
            tiempoLimite = 15  // segundos
        )
    }

    /**
     * Responde un desafío y otorga power-up si es correcto
     */
    @Transactional
    fun responderDesafio(request: ResponderDesafioRequest): ResponderDesafioResponse {
        val desafio = desafioPuzzleRepository.findById(request.desafioId)
            .orElseThrow { IllegalArgumentException("Desafío no encontrado") }

        if (desafio.respondida) {
            throw IllegalStateException("Este desafío ya fue respondido")
        }

        // Validar respuesta
        val correcto = request.respuestaSeleccionada == desafio.respuestaCorrecta

        desafio.respondida = true
        desafio.correcta = correcto
        desafio.fechaRespuesta = java.time.LocalDateTime.now()

        var powerUpObtenido: PowerUpPuzzle? = null
        var mensaje = ""

        if (correcto) {
            // Verificar límite de power-ups
            val powerUpsActuales = powerUpActivoRepository.countByPartidaIdAndUsadoFalse(desafio.partidaId)

            if (powerUpsActuales < MAX_POWER_UPS) {
                // Otorgar power-up aleatorio
                powerUpObtenido = PowerUpPuzzle.values().random()

                val powerUp = PowerUpActivo(
                    partidaId = desafio.partidaId,
                    tipo = powerUpObtenido
                )
                powerUpActivoRepository.save(powerUp)

                desafio.powerUpObtenido = powerUpObtenido
                mensaje = "¡Correcto! Obtuviste: ${obtenerNombrePowerUp(powerUpObtenido)}"

                println("🎁 Power-up otorgado: $powerUpObtenido")
            } else {
                mensaje = "¡Correcto! (Límite de power-ups alcanzado)"
            }
        } else {
            mensaje = "Incorrecto. ¡Sigue intentando!"
        }

        desafioPuzzleRepository.save(desafio)

        // Obtener power-ups disponibles
        val powerUpsDisponibles = powerUpActivoRepository.findByPartidaIdAndUsadoFalse(desafio.partidaId)

        return ResponderDesafioResponse(
            correcto = correcto,
            mensaje = mensaje,
            powerUpObtenido = powerUpObtenido,
            powerUpsDisponibles = powerUpsDisponibles.map {
                PowerUpDisponibleDTO(
                    id = it.id!!,
                    tipo = it.tipo,
                    nombre = obtenerNombrePowerUp(it.tipo),
                    descripcion = obtenerDescripcionPowerUp(it.tipo),
                    icono = obtenerIconoPowerUp(it.tipo)
                )
            }
        )
    }

    /**
     * Usa un power-up
     */
    @Transactional
    fun usarPowerUp(request: UsarPowerUpRequest): UsarPowerUpResponse {
        val powerUp = powerUpActivoRepository.findById(request.powerUpId)
            .orElseThrow { IllegalArgumentException("Power-up no encontrado") }

        if (powerUp.usado) {
            throw IllegalStateException("Este power-up ya fue usado")
        }

        powerUp.usado = true
        powerUp.fechaUsado = java.time.LocalDateTime.now()
        powerUpActivoRepository.save(powerUp)

        println("⚡ Power-up usado: ${powerUp.tipo}")

        // Generar datos específicos según el tipo
        val datos = when (powerUp.tipo) {
            PowerUpPuzzle.VISION_CONDOR -> mapOf("duracion" to 5)
            PowerUpPuzzle.TIEMPO_PACHAMAMA -> mapOf("duracion" to 30)
            PowerUpPuzzle.SABIDURIA_AMAWTA -> mapOf("piezas" to 1)
            PowerUpPuzzle.BENDICION_SOL -> mapOf("multiplicador" to 2, "duracion" to 120)
        }

        return UsarPowerUpResponse(
            tipo = powerUp.tipo,
            mensaje = "Power-up activado: ${obtenerNombrePowerUp(powerUp.tipo)}",
            datos = datos
        )
    }

    /**
     * Obtiene power-ups disponibles para una partida
     */
    fun obtenerPowerUpsDisponibles(partidaId: Long): List<PowerUpDisponibleDTO> {
        val powerUps = powerUpActivoRepository.findByPartidaIdAndUsadoFalse(partidaId)

        return powerUps.map {
            PowerUpDisponibleDTO(
                id = it.id!!,
                tipo = it.tipo,
                nombre = obtenerNombrePowerUp(it.tipo),
                descripcion = obtenerDescripcionPowerUp(it.tipo),
                icono = obtenerIconoPowerUp(it.tipo)
            )
        }
    }

    // ==================== HELPERS ====================

    private fun obtenerNombrePowerUp(tipo: PowerUpPuzzle): String {
        return when (tipo) {
            PowerUpPuzzle.VISION_CONDOR -> "Visión del Cóndor"
            PowerUpPuzzle.TIEMPO_PACHAMAMA -> "Tiempo de la Pachamama"
            PowerUpPuzzle.SABIDURIA_AMAWTA -> "Sabiduría del Amawta"
            PowerUpPuzzle.BENDICION_SOL -> "Bendición del Sol"
        }
    }

    private fun obtenerDescripcionPowerUp(tipo: PowerUpPuzzle): String {
        return when (tipo) {
            PowerUpPuzzle.VISION_CONDOR -> "Revela la imagen completa por 5 segundos"
            PowerUpPuzzle.TIEMPO_PACHAMAMA -> "Congela el cronómetro por 30 segundos"
            PowerUpPuzzle.SABIDURIA_AMAWTA -> "Coloca automáticamente 1 pieza correcta"
            PowerUpPuzzle.BENDICION_SOL -> "Duplica los puntos durante 2 minutos"
        }
    }

    private fun obtenerIconoPowerUp(tipo: PowerUpPuzzle): String {
        return when (tipo) {
            PowerUpPuzzle.VISION_CONDOR -> "👁️"
            PowerUpPuzzle.TIEMPO_PACHAMAMA -> "⏱️"
            PowerUpPuzzle.SABIDURIA_AMAWTA -> "🧠"
            PowerUpPuzzle.BENDICION_SOL -> "☀️"
        }
    }

    private fun descargarImagenDesdeUrl(url: String): ByteArray {
        return try {
            val headers = org.springframework.http.HttpHeaders()
            headers.accept = listOf(
                org.springframework.http.MediaType.IMAGE_JPEG,
                org.springframework.http.MediaType.IMAGE_PNG
            )
            headers.set("User-Agent", "Mozilla/5.0")

            val requestEntity = org.springframework.http.HttpEntity<String>(headers)
            val response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, requestEntity, ByteArray::class.java)

            response.body ?: byteArrayOf()
        } catch (e: Exception) {
            println("❌ Error descargando imagen: ${e.message}")
            byteArrayOf()
        }
    }
}