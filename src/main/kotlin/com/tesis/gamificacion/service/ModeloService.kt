// src/main/kotlin/com/tesis/gamificacion/service/ModeloService.kt

package com.tesis.gamificacion.service

import com.tesis.gamificacion.model.enums.TipoHint
import com.tesis.gamificacion.model.enums.TipoDialogo
import com.tesis.gamificacion.model.responses.*
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import com.fasterxml.jackson.databind.ObjectMapper

@Service
class ModeloService(
    private val restTemplate: RestTemplate,
    private val objectMapper: ObjectMapper
) {

    companion object {
        private const val BASE_URL = "https://nonpacific-uninitialed-jarrett.ngrok-free.dev"
    }

    // ==================== MÉTODO HELPER CRÍTICO ====================

    /**
     * Crea un ByteArrayResource que se puede leer múltiples veces
     * ESTO ES CRÍTICO: El stream no se puede reutilizar
     */
    private fun crearImagenResource(imagenBytes: ByteArray, nombreArchivo: String): ByteArrayResource {
        return object : ByteArrayResource(imagenBytes) {
            override fun getFilename(): String = nombreArchivo

            // ⬇️ IMPORTANTE: Permite múltiples lecturas
            override fun getInputStream(): java.io.InputStream {
                return java.io.ByteArrayInputStream(imagenBytes)
            }
        }
    }

    // ==================== TU MÉTODO EXISTENTE ====================

    fun generarNarrativaCultural(
        imagenBytes: ByteArray,
        nombreArchivo: String,
        conceptoClave: String
    ): ModeloResponse? {
        val url = "$BASE_URL/predict"

        val body: MultiValueMap<String, Any> = LinkedMultiValueMap()

        // ⬇️ USAR EL NUEVO MÉTODO
        val imagenResource = crearImagenResource(imagenBytes, nombreArchivo)

        body.add("image", imagenResource)
        body.add("concepto", conceptoClave)

        return try {
            val response = ejecutarLlamadaIA(url, body, ModeloResponse::class.java)
            response
        } catch (e: Exception) {
            println("Error conectando con IA: ${e.message}")
            null
        }
    }

    // ==================== NUEVOS MÉTODOS ====================

    fun generarNarrativaEducativa(
        imagenBytes: ByteArray,
        nombreArchivo: String,
        conceptoClave: String,
        nombreKichwa: String,
        nombreEspanol: String
    ): NarrativaEducativaResponse? {
        val url = "$BASE_URL/narrativa-educativa"

        val body: MultiValueMap<String, Any> = LinkedMultiValueMap()

        // ⬇️ CREAR NUEVO RESOURCE CADA VEZ
        val imagenResource = crearImagenResource(imagenBytes, nombreArchivo)

        body.add("image", imagenResource)
        body.add("concepto", conceptoClave)
        body.add("nombre_kichwa", nombreKichwa)
        body.add("nombre_espanol", nombreEspanol)

        return try {
            val response = ejecutarLlamadaIA(url, body, NarrativaEducativaResponse::class.java)
            response ?: generarNarrativaFallback(nombreKichwa, nombreEspanol, conceptoClave)
        } catch (e: Exception) {
            println("Error generando narrativa educativa: ${e.message}")
            e.printStackTrace()
            generarNarrativaFallback(nombreKichwa, nombreEspanol, conceptoClave)
        }
    }

    fun generarHintContextual(
        imagenBytes: ByteArray,
        nombreArchivo: String,
        tipoHint: TipoHint,
        categoria: String
    ): String? {
        val url = "$BASE_URL/generar-hint"

        val body: MultiValueMap<String, Any> = LinkedMultiValueMap()

        // ⬇️ CREAR NUEVO RESOURCE
        val imagenResource = crearImagenResource(imagenBytes, nombreArchivo)

        body.add("image", imagenResource)
        body.add("tipo_hint", tipoHint.name)
        body.add("categoria", categoria)

        return try {
            val response = ejecutarLlamadaIA(url, body, Map::class.java)
            (response?.get("hint") as? String) ?: generarHintFallback(tipoHint, categoria)
        } catch (e: Exception) {
            println("Error generando hint: ${e.message}")
            generarHintFallback(tipoHint, categoria)
        }
    }

    fun generarDatoCurioso(
        imagenBytes: ByteArray,
        nombreArchivo: String,
        conceptoClave: String
    ): DatoCuriosoResponse? {
        val url = "$BASE_URL/dato-curioso"

        val body: MultiValueMap<String, Any> = LinkedMultiValueMap()

        // ⬇️ CREAR NUEVO RESOURCE
        val imagenResource = crearImagenResource(imagenBytes, nombreArchivo)

        body.add("image", imagenResource)
        body.add("concepto", conceptoClave)

        return try {
            ejecutarLlamadaIA(url, body, DatoCuriosoResponse::class.java)
                ?: DatoCuriosoResponse(
                    titulo = "¿Sabías que...?",
                    descripcion = "Este elemento forma parte importante de la cultura Cañari."
                )
        } catch (e: Exception) {
            println("Error generando dato curioso: ${e.message}")
            DatoCuriosoResponse(
                titulo = "Cultura Cañari",
                descripcion = "Descubre más sobre este fascinante elemento cultural."
            )
        }
    }

    fun generarDialogoCultural(
        imagenBytes: ByteArray,
        nombreArchivo: String,
        conceptoClave: String,
        tipoDialogo: TipoDialogo
    ): DialogoCulturalResponse? {
        val url = "$BASE_URL/dialogo-cultural"

        val body: MultiValueMap<String, Any> = LinkedMultiValueMap()

        // ⬇️ CREAR NUEVO RESOURCE
        val imagenResource = crearImagenResource(imagenBytes, nombreArchivo)

        body.add("image", imagenResource)
        body.add("concepto", conceptoClave)
        body.add("tipo_dialogo", tipoDialogo.name)

        return try {
            ejecutarLlamadaIA(url, body, DialogoCulturalResponse::class.java)
                ?: DialogoCulturalResponse(
                    textoKichwa = "¡Allillachu!",
                    textoEspanol = "¡Muy bien!",
                    tipo = tipoDialogo.name
                )
        } catch (e: Exception) {
            println("Error generando diálogo cultural: ${e.message}")
            DialogoCulturalResponse(
                textoKichwa = "¡Allillachu!",
                textoEspanol = "¡Muy bien!",
                tipo = tipoDialogo.name
            )
        }
    }

    // ==================== MÉTODOS HELPER ====================

    private fun <T> ejecutarLlamadaIA(
        url: String,
        body: MultiValueMap<String, Any>,
        responseType: Class<T>
    ): T? {
        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA
        val requestEntity = HttpEntity(body, headers)

        return try {
            println("🔄 Llamando a IA: $url")
            val response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                responseType
            )

            if (response.statusCode.is2xxSuccessful) {
                println("✅ IA respondió correctamente")
                response.body
            } else {
                println("⚠️ IA respondió con error: ${response.statusCode}")
                null
            }
        } catch (e: Exception) {
            println("❌ Error en llamada a IA ($url): ${e.message}")
            e.printStackTrace()
            null
        }
    }

    private fun generarNarrativaFallback(
        nombreKichwa: String,
        nombreEspanol: String,
        concepto: String
    ): NarrativaEducativaResponse {
        return NarrativaEducativaResponse(
            titulo = nombreEspanol,
            descripcion = "El $nombreEspanol ($nombreKichwa) es un elemento importante de la cultura Cañari. " +
                    "Representa la riqueza de las tradiciones ancestrales que se mantienen vivas hasta hoy.",
            nombreKichwa = nombreKichwa,
            nombreEspanol = nombreEspanol,
            preguntaRecuperacion = PreguntaRecuperacionResponse(
                pregunta = "¿A qué categoría pertenece el $nombreEspanol?",
                opciones = listOf("Vestimenta", "Música", "Lugares", "Festividades"),
                respuestaCorrecta = obtenerIndiceCategoria(concepto),
                explicacion = "El $nombreEspanol forma parte de la categoría de $concepto."
            )
        )
    }

    private fun generarHintFallback(tipoHint: TipoHint, categoria: String): String {
        return when (tipoHint) {
            TipoHint.DESCRIPCION_CONTEXTUAL -> "Busca un elemento relacionado con $categoria"
            TipoHint.PISTA_VISUAL -> "Observa los detalles característicos de la cultura Cañari"
            TipoHint.CATEGORIA_CULTURAL -> "Este elemento se usa en contextos de $categoria"
        }
    }

    private fun obtenerIndiceCategoria(categoria: String): Int {
        return when (categoria.lowercase()) {
            "vestimenta" -> 0
            "musica", "música" -> 1
            "lugares" -> 2
            "festividades" -> 3
            else -> 0
        }
    }

    // Agregar al ModeloService.kt

    /**
     * Genera una pregunta rápida de trivia para el puzzle
     */
    fun generarPreguntaRapidaPuzzle(
        imagenBytes: ByteArray,
        titulo: String,
        nombreKichwa: String,
        categoria: String
    ): PreguntaRapidaResponse {
        val url = "$BASE_URL/pregunta-rapida-puzzle"

        val body: MultiValueMap<String, Any> = LinkedMultiValueMap()

        // Crear resource de imagen
        val imagenResource = crearImagenResource(imagenBytes, "puzzle_image.jpg")

        body.add("image", imagenResource)
        body.add("titulo", titulo)
        body.add("nombre_kichwa", nombreKichwa)
        body.add("categoria", categoria)

        return try {
            println("🎯 Generando pregunta rápida para: $titulo")
            ejecutarLlamadaIA(url, body, PreguntaRapidaResponse::class.java)
                ?: generarPreguntaFallback(titulo, nombreKichwa, categoria)
        } catch (e: Exception) {
            println("❌ Error generando pregunta: ${e.message}")
            generarPreguntaFallback(titulo, nombreKichwa, categoria)
        }
    }

    /**
     * Genera una pregunta fallback si la IA falla
     */
    private fun generarPreguntaFallback(
        titulo: String,
        nombreKichwa: String,
        categoria: String
    ): PreguntaRapidaResponse {
        val preguntas = mapOf(
            "VESTIMENTA" to listOf(
                PreguntaRapidaResponse(
                    pregunta = "¿Cómo se dice '$titulo' en kichwa?",
                    opciones = listOf(nombreKichwa, "Chumbi", "Anaku", "Ushuta"),
                    respuestaCorrecta = nombreKichwa
                ),
                PreguntaRapidaResponse(
                    pregunta = "¿A qué cultura pertenece el $titulo?",
                    opciones = listOf("Cañari", "Inca", "Azteca", "Maya"),
                    respuestaCorrecta = "Cañari"
                )
            ),
            "MUSICA" to listOf(
                PreguntaRapidaResponse(
                    pregunta = "¿En qué ocasiones se usa el $titulo?",
                    opciones = listOf("Festividades", "Solo ceremonias", "Nunca", "Solo bodas"),
                    respuestaCorrecta = "Festividades"
                ),
                PreguntaRapidaResponse(
                    pregunta = "¿Cómo se dice '$titulo' en kichwa?",
                    opciones = listOf(nombreKichwa, "Runa", "Wasi", "Mama"),
                    respuestaCorrecta = nombreKichwa
                )
            ),
            "LUGARES" to listOf(
                PreguntaRapidaResponse(
                    pregunta = "¿Dónde se encuentra $titulo?",
                    opciones = listOf("Ecuador", "Perú", "Bolivia", "Colombia"),
                    respuestaCorrecta = "Ecuador"
                ),
                PreguntaRapidaResponse(
                    pregunta = "¿Qué significa '$nombreKichwa' en español?",
                    opciones = listOf(titulo, "Montaña", "Río", "Camino"),
                    respuestaCorrecta = titulo
                )
            ),
            "FESTIVIDADES" to listOf(
                PreguntaRapidaResponse(
                    pregunta = "¿Cuál es el nombre en kichwa de $titulo?",
                    opciones = listOf(nombreKichwa, "Inti Raymi", "Pawkar Raymi", "Kulla Raymi"),
                    respuestaCorrecta = nombreKichwa
                )
            )
        )

        val preguntasCategoria = preguntas[categoria.uppercase()] ?: preguntas["VESTIMENTA"]!!
        return preguntasCategoria.random()
    }
}