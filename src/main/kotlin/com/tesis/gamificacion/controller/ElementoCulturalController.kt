package com.tesis.gamificacion.controller

import com.tesis.gamificacion.dto.request.CrearElementoCulturalRequest
import com.tesis.gamificacion.dto.response.ElementoCulturalResponse
import com.tesis.gamificacion.model.enums.CategoriasCultural
import com.tesis.gamificacion.service.ElementoCulturalService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/elementos-culturales")
@CrossOrigin(origins = ["http://localhost:4200"])
class ElementoCulturalController(
    private val elementoCulturalService: ElementoCulturalService

) {

    @GetMapping
    fun obtenerTodos(): ResponseEntity<List<ElementoCulturalResponse>> {
        val elementos = elementoCulturalService.obtenerTodos()
        return ResponseEntity.ok(elementos)
    }

    @GetMapping("/categoria/{categoria}")
    fun obtenerPorCategoria(
        @PathVariable categoria: CategoriasCultural
    ): ResponseEntity<List<ElementoCulturalResponse>> {
        val elementos = elementoCulturalService.obtenerPorCategoria(categoria)
        return ResponseEntity.ok(elementos)
    }

    @GetMapping("/{id}")
    fun obtenerPorId(@PathVariable id: Long): ResponseEntity<ElementoCulturalResponse> {
        val elemento = elementoCulturalService.obtenerPorId(id)
        return ResponseEntity.ok(elemento)
    }

    @GetMapping("/aleatorios/{categoria}")
    fun obtenerAleatorios(
        @PathVariable categoria: CategoriasCultural,
        @RequestParam(defaultValue = "6") cantidad: Int
    ): ResponseEntity<List<ElementoCulturalResponse>> {
        val elementos = elementoCulturalService.obtenerAleatoriosPorCategoria(categoria, cantidad)
        return ResponseEntity.ok(elementos)
    }

    @PostMapping
    fun crear(
        @Valid @RequestBody request: CrearElementoCulturalRequest
    ): ResponseEntity<ElementoCulturalResponse> {
        val elemento = elementoCulturalService.crear(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(elemento)
    }

    @PutMapping("/{id}")
    fun actualizar(
        @PathVariable id: Long,
        @Valid @RequestBody request: CrearElementoCulturalRequest
    ): ResponseEntity<ElementoCulturalResponse> {
        val elemento = elementoCulturalService.actualizar(id, request)
        return ResponseEntity.ok(elemento)
    }

    @DeleteMapping("/{id}")
    fun eliminar(@PathVariable id: Long): ResponseEntity<Void> {
        elementoCulturalService.eliminar(id)
        return ResponseEntity.noContent().build()
    }
}