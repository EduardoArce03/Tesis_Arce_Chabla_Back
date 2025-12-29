package com.tesis.gamificacion.service

import com.tesis.gamificacion.model.enums.Cultura
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import com.tesis.gamificacion.model.request.Blip2Request
import com.tesis.gamificacion.model.request.Blip2Response

@Service
class Blip2IntegrationService (
    //private val restTemplate: RestTemplate
){

    fun generarNarrativaCultural(
        imagenUrl: String,
        cultura: Cultura,
        conceptoClave: String
    ): String {
        val request = Blip2Request(
            imagen = imagenUrl,
            prompt = construirPromptCultural(Cultura.CAÑARI ,conceptoClave)
        )

        // Llamada al servidor del modelo BLIP-2
        //val response = restTemplate.postForObject(
        //    "http://tu-servidor-blip2/generate",
        //    request,
        //    Blip2Response::class.java
        //)

        //return response?.narrativa ?: "Error generando narrativa"
        return "xd"
    }

    private fun construirPromptCultural(cultura: Cultura, concepto: String): String {
        return when(cultura) {
            //Cultura.INCA -> "Describe este elemento desde la perspectiva de la cultura Inca, enfocándote en $concepto"
            Cultura.CAÑARI -> "Explica este elemento desde el contexto cultural Cañari, considerando $concepto"
            //Cultura.MIXTO -> "Relaciona este elemento con las culturas Inca y Cañari, destacando $concepto"
        }
    }
}