package com.tesis.gamificacion.service

import com.tesis.gamificacion.model.entities.FotografiaObjetivo
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
class FotografiaIAService(
    private val restTemplate: RestTemplate,

    @Value("\${ia.service.url}")
    private val BASE_URL: String
) {

    /**
     * Analiza una foto usando IA
     */

    private fun limpiarBase64(base64String: String): String {
        // Si tiene el prefijo "data:image/...;base64,", removerlo
        return if (base64String.contains(",")) {
            base64String.substringAfter(",")
        } else {
            base64String
        }
    }

    fun analizarFotografia(
        objetivo: FotografiaObjetivo,
        imagenBase64: String,
        descripcionUsuario: String?
    ): FotoAnalisisResultado {

        return try {
            println("📸 Analizando fotografía con LLaVA Cañari...")
            println("   Objetivo: ${objetivo.descripcion}")
            println("   Rareza esperada: ${objetivo.rareza}")

            // 1. Limpiar base64
            val base64Limpio = limpiarBase64(imagenBase64)

            // 2. Decodificar a bytes
            val imagenBytes = Base64.getDecoder().decode(base64Limpio)
            println("   Tamaño imagen: ${imagenBytes.size} bytes")

            // 3. Crear request multipart (Flask espera FormData)
            val body = LinkedMultiValueMap<String, Any>()

            // Agregar parámetros de texto
            body.add("objetivoNombre", objetivo.puntoInteres.nombre)
            body.add("objetivoDescripcion", objetivo.descripcion)
            body.add("rarezaEsperada", objetivo.rareza.name)

            // Agregar imagen como archivo
            body.add("image", object : ByteArrayResource(imagenBytes) {
                override fun getFilename(): String = "foto.jpg"
            })

            // 4. Configurar headers
            val headers = HttpHeaders()
            headers.contentType = MediaType.MULTIPART_FORM_DATA

            val requestEntity = HttpEntity(body, headers)

            // 5. Llamar al servicio Python
            println("📤 Enviando a Python: $BASE_URL")

            val response = restTemplate.exchange(
                BASE_URL+"/analizar-fotografia",
                HttpMethod.POST,
                requestEntity,
                Map::class.java
            )

            val responseBody = response.body as? Map<String, Any>
                ?: throw IllegalStateException("Respuesta vacía del servicio de IA")

            println("✅ Respuesta de IA recibida")
            println("   Es válida: ${responseBody["esValida"]}")
            println("   Cumple criterios: ${responseBody["cumpleCriterios"]}")

            // 6. Mapear respuesta a FotoAnalisisResultado
            FotoAnalisisResultado(
                esValida = responseBody["esValida"] as? Boolean ?: false,
                cumpleCriterios = responseBody["cumpleCriterios"] as? Boolean ?: false,
                descripcionIA = responseBody["descripcionIA"] as? String
                    ?: "No se pudo analizar la imagen",
                confianza = (responseBody["confianza"] as? Number)?.toDouble() ?: 0.0,
                rarezaDetectada = RarezaFoto.valueOf(
                    responseBody["rarezaDetectada"] as? String ?: objetivo.rareza.name
                )
            )

        } catch (e: Exception) {
            println("❌ Error llamando al servicio de IA: ${e.message}")
            e.printStackTrace()

            // Fallback: devolver análisis fallido
            FotoAnalisisResultado(
                esValida = false,
                cumpleCriterios = false,
                descripcionIA = "Error al analizar la fotografía con IA: ${e.message}",
                confianza = 0.0,
                rarezaDetectada = objetivo.rareza
            )
        }
    }

// ⬇️ MODELO DE RESULTADO
data class FotoAnalisisResultado(
    val esValida: Boolean,
    val cumpleCriterios: Boolean,
    val descripcionIA: String,
    val confianza: Double,
    val rarezaDetectada: RarezaFoto
)

    /**
     * Genera descripción de la foto capturada
     */
    fun generarDescripcionFoto(
        imagenBytes: ByteArray,
        contexto: String
    ): String {
        val url = "$BASE_URL/describir-imagen"

        val imagenResource = crearImagenResource(imagenBytes, "foto.jpg")

        val body: MultiValueMap<String, Any> = LinkedMultiValueMap()
        body.add("image", imagenResource)
        body.add("contexto", contexto)

        return try {
            val response = ejecutarLlamadaIA(url, body, Map::class.java)
            (response?.get("descripcion") as? String)
                ?: "Fotografía capturada del sitio arqueológico"
        } catch (e: Exception) {
            "Fotografía histórica de Ingapirca"
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
            val response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                responseType
            )
            if (response.statusCode.is2xxSuccessful) response.body else null
        } catch (e: Exception) {
            null
        }
    }

    private fun generarAnalisisFallback(objetivo: FotografiaObjetivo): AnalisisFotoResult {
        return AnalisisFotoResult(
            esValida = true,
            descripcionIA = "Has capturado un elemento del sitio arqueológico",
            cumpleCriterios = true,
            rarezaDetectada = objetivo.rareza,
            confianza = 0.7
        )
    }
}

data class AnalisisFotoResult(
    val esValida: Boolean,
    val descripcionIA: String,
    val cumpleCriterios: Boolean,
    val rarezaDetectada: RarezaFoto,
    val confianza: Double
)