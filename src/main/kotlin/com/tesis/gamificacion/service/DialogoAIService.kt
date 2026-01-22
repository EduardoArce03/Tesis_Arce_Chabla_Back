package com.tesis.gamificacion.service

import com.tesis.gamificacion.model.entities.CapaDescubrimiento
import com.tesis.gamificacion.model.entities.DialogoHistorial
import com.tesis.gamificacion.model.enums.NivelCapa
import com.tesis.gamificacion.repository.CapaTemporalRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate

@Service
class DialogoIAService(
    private val restTemplate: RestTemplate,
    private val capaTemporalRepository: CapaTemporalRepository, // ✅ AGREGAR ESTO
    @Value("\${ia.service.url}")
    private val BASE_URL: String
) {


    /**
     * Genera respuesta del espíritu usando IA
     */
    fun generarRespuestaEspiritu(
        capa: CapaDescubrimiento,
        pregunta: String,
        historialPrevio: List<DialogoHistorial>,
        puntoInteresNombre: String? = null // ✅ AGREGAR PARÁMETRO OPCIONAL
    ): String {
        val url = "$BASE_URL/dialogo-espiritu"

        // ✅ OBTENER CONFIGURACIÓN DEL ESPÍRITU DESDE CapaTemporal
        val capaTemporal = capaTemporalRepository.findByNivel(capa.nivel).firstOrNull()

        if (capaTemporal == null) {
            return generarRespuestaFallbackSimple(capa.nivel, pregunta)
        }

        val body: MultiValueMap<String, Any> = LinkedMultiValueMap()

        body.add("pregunta", pregunta)
        body.add("nombre_espiritu", capaTemporal.nombreEspiritu)
        body.add("epoca", capaTemporal.epocaEspiritu)
        body.add("personalidad", capaTemporal.personalidadEspiritu)
        body.add("prompt_espiritu", capaTemporal.promptEspiritu)
        body.add("nivel_capa", capa.nivel.nombre)
        body.add("punto_nombre", puntoInteresNombre ?: "sitio arqueológico")

        // Contexto de conversaciones previas
        val contexto = historialPrevio.takeLast(5).joinToString("\n") {
            "Jugador: ${it.preguntaUsuario}\n${capaTemporal.nombreEspiritu}: ${it.respuestaEspiritu}"
        }
        body.add("contexto_previo", contexto)

        return try {
            val response = ejecutarLlamadaIA(url, body, Map::class.java)
            (response?.get("respuesta") as? String)
                ?: generarRespuestaFallback(capaTemporal, pregunta)
        } catch (e: Exception) {
            println("❌ Error generando diálogo: ${e.message}")
            generarRespuestaFallback(capaTemporal, pregunta)
        }
    }

    /**
     * Calcula nivel de confianza basado en número de conversaciones
     */
    fun calcularNivelConfianza(numeroConversaciones: Int): Int {
        return when {
            numeroConversaciones >= 20 -> 5
            numeroConversaciones >= 15 -> 4
            numeroConversaciones >= 10 -> 3
            numeroConversaciones >= 5 -> 2
            else -> 1
        }
    }

    /**
     * Determina qué se desbloquea con este diálogo
     */
    fun determinarDesbloqueos(
        numeroConversaciones: Int,
        nivelCapa: NivelCapa
    ): List<String> {
        val desbloqueos = mutableListOf<String>()

        when {
            numeroConversaciones == 1 -> desbloqueos.add("Primera conversación completada")
            numeroConversaciones == 5 -> desbloqueos.add("Pista para foto rara")
            numeroConversaciones == 10 -> desbloqueos.add("Fragmento de diario desbloqueado")
            numeroConversaciones == 15 -> desbloqueos.add("Anécdota especial del espíritu")
            numeroConversaciones >= 20 && nivelCapa == NivelCapa.ANCESTRAL -> {
                desbloqueos.add("Revelación del secreto ancestral")
            }
        }

        return desbloqueos
    }

    // ==================== HELPERS ====================

    private fun <T> ejecutarLlamadaIA(
        url: String,
        body: MultiValueMap<String, Any>,
        responseType: Class<T>
    ): T? {
        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA
        val requestEntity = HttpEntity(body, headers)

        return try {
            val response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                responseType
            )

            if (response.statusCode.is2xxSuccessful) {
                response.body
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun generarRespuestaFallback(
        capaTemporal: com.tesis.gamificacion.model.entities.CapaTemporal,
        pregunta: String
    ): String {
        return "Soy ${capaTemporal.nombreEspiritu}, guardián de ${capaTemporal.puntoInteres.nombre}. " +
                "Aunque no puedo responder completamente a tu pregunta en este momento, " +
                "te diré que este lugar guarda muchos secretos de la época ${capaTemporal.epocaEspiritu}. " +
                "Continúa explorando y descubrirás más sobre nuestra historia."
    }

    private fun generarRespuestaFallbackSimple(nivel: NivelCapa, pregunta: String): String {
        return when (nivel) {
            NivelCapa.SUPERFICIE ->
                "Bienvenido a Ingapirca. Este sitio arqueológico guarda muchos secretos de nuestros antepasados."
            NivelCapa.INCA ->
                "En tiempos del Tahuantinsuyo, este era un lugar de gran importancia ceremonial."
            NivelCapa.CANARI ->
                "Mucho antes de los incas, los cañaris ya habitábamos estas tierras sagradas."
            NivelCapa.ANCESTRAL ->
                "Los secretos ancestrales solo se revelan a quienes tienen paciencia y sabiduría."
        }
    }
}