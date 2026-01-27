package com.tesis.gamificacion.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate

@Service
class NarrativaIAService(
    private val restTemplate: RestTemplate,

    @Value("\${ia.service.url}")
    private val BASE_URL: String
) {
    /**
     * Genera narrativa histórica usando IA
     */
    fun generarNarrativa(
        nombrePunto: String,
        imagenPunto: ByteArray? = null
    ): String {
        val url = "$BASE_URL/narrativa-exploracion2"

        // Obtener configuración de la capa temporal

        val body: MultiValueMap<String, Any> = LinkedMultiValueMap()

        // Agregar imagen si existe
        imagenPunto?.let {
            val imagenResource = crearImagenResource(it, "punto_${nombrePunto}.jpg")
            body.add("image", imagenResource)
        }

        body.add("punto_nombre", nombrePunto)
        body.add("prompt_narrativa",  "")

        return try {
            val response = ejecutarLlamadaIA(url, body, Map::class.java)
            (response?.get("narrativa") as? String)
                ?: generarNarrativaFallback(nombrePunto)
        } catch (e: Exception) {
            println("❌ Error generando narrativa: ${e.message}")
            generarNarrativaFallback(nombrePunto)
        }
    }

    /**
     * Genera narrativa cultural para un descubrimiento de punto
     */
    fun generarNarrativaDescubrimiento(
        nombrePunto: String,
        categoria: String,
        nivel: String,
        descripcionBase: String
    ): String? {
        val url = "$BASE_URL/generar-narrativa-descubrimiento"

        val body: MultiValueMap<String, Any> = LinkedMultiValueMap()
        body.add("nombre_punto", nombrePunto)
        body.add("categoria", categoria)
        body.add("nivel_capa", nivel)
        body.add("descripcion_base", descripcionBase)

        return try {
            val response = ejecutarLlamadaIA(url, body, Map::class.java)
            (response?.get("narrativa") as? String)
        } catch (e: Exception) {
            println("❌ Error generando narrativa: ${e.message}")
            null
        }
    }

    fun generarNarrativaEducativa(
        imagenUrl: String,
        categoria: String,
        nombreKichwa: String,
        nombreEspanol: String,
        epoca: String,
    ): Map<String, Any>? {
        val url = "$BASE_URL/narrativa-educativa2"

        return try {
            // 1. Descargar la imagen
            val imagenBytes = descargarImagen(imagenUrl)

            // 2. Crear MultiValueMap
            val body: MultiValueMap<String, Any> = LinkedMultiValueMap()

            // Convertir imagen a Resource (Esto está bien, pero asegúrate que sea object)
            val imagenResource = object : ByteArrayResource(imagenBytes) {
                override fun getFilename(): String = "punto.jpg"
            }

            body.add("image", imagenResource)
            body.add("concepto", categoria)
            body.add("nombre_kichwa", nombreKichwa)
            body.add("nombre_espanol", nombreEspanol)
            body.add("epoca", epoca)

            // 3. Enviar request
            // Java devuelve Map<*,*>, así que lo guardamos en una variable intermedia
            val responseRaw = ejecutarLlamadaIA(url, body, Map::class.java)

            // 4. ✅ SOLUCIÓN: Hacemos el cast explícito y seguro
            @Suppress("UNCHECKED_CAST")
            return responseRaw as? Map<String, Any>

        } catch (e: Exception) {
            println("❌ Error generando narrativa educativa: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    private fun descargarImagen(imagenUrl: String): ByteArray {
        return try {
            restTemplate.getForObject(imagenUrl, ByteArray::class.java)
                ?: throw IllegalArgumentException("No se pudo descargar la imagen")
        } catch (e: Exception) {
            println("⚠️ Error descargando imagen: ${e.message}, usando imagen por defecto")
            // Retornar imagen placeholder pequeña (1x1 pixel transparente PNG)
            byteArrayOf(
                0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52
            )
        }
    }

    /**
     * Extrae elementos clave de la narrativa usando IA
     */
    fun extraerElementosClave(narrativa: String): List<String> {
        val url = "$BASE_URL/extraer-elementos"

        val body: MultiValueMap<String, Any> = LinkedMultiValueMap()
        body.add("texto", narrativa)

        return try {
            val response = ejecutarLlamadaIA(url, body, Map::class.java)
            @Suppress("UNCHECKED_CAST")
            (response?.get("elementos") as? List<String>)
                ?: listOf("Arquitectura", "Ceremonias", "Cultura")
        } catch (e: Exception) {
            println("❌ Error extrayendo elementos: ${e.message}")
            listOf("Historia", "Cultura", "Tradición")
        }
    }

    // ==================== HELPERS ====================

    private fun crearImagenResource(imagenBytes: ByteArray, nombreArchivo: String): ByteArrayResource {
        return object : ByteArrayResource(imagenBytes) {
            override fun getFilename(): String = nombreArchivo
            override fun getInputStream(): java.io.InputStream {
                return java.io.ByteArrayInputStream(imagenBytes)
            }
        }
    }

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
            println("❌ Error en llamada a IA: ${e.message}")
            null
        }
    }

    private fun generarNarrativaFallback(
        nombrePunto: String,
    ): String {
        return "Esta es una narrativa de fallback"
    }
}