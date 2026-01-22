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
    fun analizarFotografia(
        objetivo: FotografiaObjetivo,
        imagenBase64: String,
        descripcionUsuario: String?
    ): AnalisisFotoResult {
        val url = "$BASE_URL/analizar-fotografia"

        // Decodificar base64 a bytes
        val imagenBytes = Base64.getDecoder().decode(imagenBase64)
        val imagenResource = crearImagenResource(imagenBytes, "foto_captura.jpg")

        val body: MultiValueMap<String, Any> = LinkedMultiValueMap()
        body.add("image", imagenResource)
        body.add("objetivo_descripcion", objetivo.descripcion)
        body.add("criterios_validacion", objetivo.criteriosValidacion)
        body.add("rareza_esperada", objetivo.rareza.name)
        descripcionUsuario?.let { body.add("descripcion_usuario", it) }

        return try {
            val response = ejecutarLlamadaIA(url, body, Map::class.java)

            if (response != null) {
                AnalisisFotoResult(
                    esValida = response["es_valida"] as? Boolean ?: false,
                    descripcionIA = response["descripcion"] as? String ?: "Foto analizada",
                    cumpleCriterios = response["cumple_criterios"] as? Boolean ?: false,
                    rarezaDetectada = objetivo.rareza,
                    confianza = (response["confianza"] as? Number)?.toDouble() ?: 0.5
                )
            } else {
                generarAnalisisFallback(objetivo)
            }
        } catch (e: Exception) {
            println("❌ Error analizando foto: ${e.message}")
            generarAnalisisFallback(objetivo)
        }
    }

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