package com.tesis.gamificacion.service

import com.tesis.gamificacion.model.responses.ModeloResponse
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate

@Service
class ModeloService(
    private val restTemplate: RestTemplate
) {

    fun generarNarrativaCultural(
        imagenBytes: ByteArray,
        nombreArchivo: String,
        conceptoClave: String
    ): ModeloResponse? {

        val url = "https://tu-url-ngrok.ngrok-free.app/predict"

        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA

        val body: MultiValueMap<String, Any> = LinkedMultiValueMap()

        val imagenResource = object : ByteArrayResource(imagenBytes) {
            override fun getFilename(): String = nombreArchivo
        }

        body.add("image", imagenResource)
        body.add("concepto", conceptoClave)

        val requestEntity = HttpEntity(body, headers)

        return try {
            val response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                ModeloResponse::class.java
            )
            response.body
        } catch (e: Exception) {
            println("Error conectando con IA: ${e.message}")
            null
        }
    }
}