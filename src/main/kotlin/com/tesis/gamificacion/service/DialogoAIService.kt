package com.tesis.gamificacion.service

import com.tesis.gamificacion.model.entities.DialogoHistorial
import com.tesis.gamificacion.model.enums.CapaNivel
import com.tesis.gamificacion.model.enums.NivelCapa
import com.tesis.gamificacion.model.enums.RarezaFoto
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import java.util.Base64

@Service
class DialogoIAService(
    private val restTemplate: RestTemplate,
    @Value("\${ia.service.url}")
    private val BASE_URL: String,

) {

    fun analizarFotografia(
        imagenBase64: String?,
        objetivoNombre: String,
        objetivoDescripcion: String,
        rarezaEsperada: String
    ): FotografiaIAService.FotoAnalisisResultado {

        return try {
            println("📸 Analizando fotografía con LLaVA Cañari...")
            println("   Objetivo: $objetivoNombre")
            println("   Descripción: $objetivoDescripcion")
            println("   Rareza esperada: $rarezaEsperada")

            if (imagenBase64.isNullOrBlank()) {
                throw IllegalArgumentException("La imagen Base64 está vacía")
            }

            // 1. Limpiar base64 (remover prefijos como "data:image/jpeg;base64,")
            val base64Limpio = imagenBase64
                .replace("data:image/jpeg;base64,", "")
                .replace("data:image/png;base64,", "")
                .replace("data:image/webp;base64,", "")
                .replace("\n", "")
                .replace("\r", "")
                .trim()

            println("   Base64 limpio: ${base64Limpio.take(50)}... (${base64Limpio.length} chars)")

            // 2. Decodificar a bytes
            val imagenBytes = try {
                Base64.getDecoder().decode(base64Limpio)
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Error decodificando Base64: ${e.message}")
            }

            if (imagenBytes.isEmpty()) {
                throw IllegalStateException("La imagen decodificada tiene 0 bytes")
            }

            println("   ✅ Imagen decodificada: ${imagenBytes.size} bytes")

            // 3. Crear ByteArrayResource con nombre de archivo
            val imageResource = object : ByteArrayResource(imagenBytes) {
                override fun getFilename(): String = "foto.jpg"
            }

            // 4. Crear request multipart
            val body = LinkedMultiValueMap<String, Any>()
            body.add("image", imageResource)
            body.add("objetivoNombre", objetivoNombre)
            body.add("objetivoDescripcion", objetivoDescripcion)
            body.add("rarezaEsperada", rarezaEsperada)

            // 5. Configurar headers
            val headers = HttpHeaders()
            headers.contentType = MediaType.MULTIPART_FORM_DATA

            val requestEntity = HttpEntity(body, headers)

            // 6. Llamar al servicio Python
            val url = "$BASE_URL/analizar-fotografia"
            println("📤 Enviando a Python: $url")

            val response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                Map::class.java
            )

            val responseBody = response.body as? Map<String, Any>
                ?: throw IllegalStateException("Respuesta vacía del servicio de IA")

            println("✅ Respuesta de IA recibida:")
            println("   Es válida: ${responseBody["esValida"]}")
            println("   Cumple criterios: ${responseBody["cumpleCriterios"]}")
            println("   Confianza: ${responseBody["confianza"]}")

            // 7. Verificar si hubo error en Python
            val exito = responseBody["exito"] as? Boolean ?: false
            if (!exito) {
                val errorMsg = responseBody["error"] as? String ?: "Error desconocido"
                throw Exception("Error en servicio Python: $errorMsg")
            }

            // 8. Mapear respuesta
            FotografiaIAService.FotoAnalisisResultado(
                esValida = responseBody["esValida"] as? Boolean ?: false,
                cumpleCriterios = responseBody["cumpleCriterios"] as? Boolean ?: false,
                descripcionIA = responseBody["descripcionIA"] as? String
                    ?: "No se pudo analizar la imagen",
                confianza = (responseBody["confianza"] as? Number)?.toDouble() ?: 0.0,
                rarezaDetectada = try {
                    RarezaFoto.valueOf(
                        (responseBody["rarezaDetectada"] as? String ?: rarezaEsperada).uppercase()
                    )
                } catch (e: Exception) {
                    RarezaFoto.COMUN
                }
            )

        } catch (e: Exception) {
            println("❌ Error llamando al servicio de IA: ${e.message}")
            e.printStackTrace()

            // Fallback: devolver análisis fallido
            FotografiaIAService.FotoAnalisisResultado(
                esValida = false,
                cumpleCriterios = false,
                descripcionIA = "Error al analizar la fotografía con IA: ${e.message}",
                confianza = 0.0,
                rarezaDetectada = RarezaFoto.COMUN
            )
        }
    }


    /**
     * Genera respuesta del espíritu usando IA
     */
    fun generarRespuestaEspiritu(
        capa: CapaNivel,  // ⬅️ Recibe solo el nivel
        pregunta: String,
        imagenUrl: String,
        historialPrevio: List<DialogoHistorial>,
        puntoInteresNombre: String?,
        puntoInteresId: Long? = null  // ⬅️ NUEVO: opcional
    ): String {

        return try {
            println("🗣️ Generando respuesta del espíritu...")
            println("   Nivel Capa: $capa")
            println("   Punto: $puntoInteresNombre")
            println("   Punto ID: $puntoInteresId")

            // ⬇️ OBTENER URL DE LA IMAGEN DEL PUNTO DE INTERÉS
            val imagenUrl = imagenUrl

            println("   Imagen URL: $imagenUrl")

            // Preparar historial
            val historialDTO = historialPrevio.map { dialogo ->
                mapOf(
                    "pregunta" to dialogo.preguntaUsuario,
                    "respuesta" to dialogo.respuestaEspiritu
                )
            }

            // ⬇️ REQUEST CON URL DE IMAGEN
            val requestBody = mapOf(
                "pregunta" to pregunta,
                "nivelCapa" to capa.name,  // ⬅️ Usar el enum como String
                "puntoInteresNombre" to (puntoInteresNombre ?: "Ingapirca"),
                "imagenUrl" to imagenUrl,
                "historialPrevio" to historialDTO
            )

            println("📤 Enviando a Python: $BASE_URL/dialogo-espiritu")

            // Llamar al servicio Python
            val response = restTemplate.postForObject(
                "$BASE_URL/dialogo-espiritu",
                requestBody,
                Map::class.java
            ) as? Map<String, Any> ?: throw RuntimeException("Respuesta vacía del servicio Python")

            val respuesta = response["respuestaEspiritu"] as? String
                ?: throw RuntimeException("El espíritu no generó respuesta")

            println("✅ Respuesta recibida (${respuesta.length} chars): ${respuesta.take(100)}...")

            respuesta

        } catch (e: Exception) {
            println("❌ Error en servicio de IA: ${e.message}")
            e.printStackTrace()
            generarRespuestaFallback(capa, pregunta, puntoInteresNombre)
        }
    }

    private fun generarRespuestaFallback(
        nivel: CapaNivel,
        pregunta: String,
        puntoNombre: String?
    ): String {
        val punto = puntoNombre ?: "este lugar sagrado"

        return when (nivel) {
            CapaNivel.CANARI ->
                "Bienvenido, explorador. Tu pregunta sobre $punto resuena en estas piedras ancestrales. Nuestros antepasados Cañari dejaron aquí su legado para las futuras generaciones."

            CapaNivel.ACTUAL ->
                "Las piedras del Tawantinsuyu en $punto guardan muchos secretos. La unión entre la sabiduría Cañari e Inca creó este lugar extraordinario donde el cielo y la tierra se encuentran."

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
        pregunta: String
    ): String {
        return "Soy edu, guardián de ingapirca. " +
                "Aunque no puedo responder completamente a tu pregunta en este momento, " +
                "te diré que este lugar guarda muchos secretos de la época xd. " +
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