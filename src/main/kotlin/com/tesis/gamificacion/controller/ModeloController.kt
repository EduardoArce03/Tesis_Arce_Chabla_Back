package com.tesis.gamificacion.controller

import com.tesis.gamificacion.model.responses.ModeloResponse
import com.tesis.gamificacion.service.ModeloService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/narrativa")
class ModeloController(
    private val blip2Service: ModeloService
) {

    @PostMapping("/generar")
    fun generarNarrativa(
        @RequestParam("image") file: MultipartFile,
        @RequestParam("concepto") conceptoClave: String,
        @RequestParam("cultura") cultura: String
    ): ResponseEntity<ModeloResponse> {

        if (file.isEmpty) {
            return ResponseEntity.badRequest().build()
        }

        val response = blip2Service.generarNarrativaCultural(
            imagenBytes = file.bytes,
            nombreArchivo = file.originalFilename ?: "imagen.jpg",
            conceptoClave = conceptoClave
        )

        return if (response != null) {
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.status(500).build()
        }
    }
}