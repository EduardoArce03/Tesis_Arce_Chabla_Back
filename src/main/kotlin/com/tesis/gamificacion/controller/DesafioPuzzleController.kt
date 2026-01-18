// src/main/kotlin/com/tesis/gamificacion/controller/DesafioPuzzleController.kt
package com.tesis.gamificacion.controller

import com.tesis.gamificacion.dto.DesafioGeneradoResponse
import com.tesis.gamificacion.dto.PowerUpDisponibleDTO
import com.tesis.gamificacion.dto.ResponderDesafioRequest
import com.tesis.gamificacion.dto.ResponderDesafioResponse
import com.tesis.gamificacion.dto.UsarPowerUpRequest
import com.tesis.gamificacion.dto.UsarPowerUpResponse
import com.tesis.gamificacion.dto.request.*
import com.tesis.gamificacion.dto.response.*
import com.tesis.gamificacion.service.DesafioPuzzleService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/puzzle/desafios")
@CrossOrigin(origins = ["http://localhost:4200"])
class DesafioPuzzleController(
    private val desafioPuzzleService: DesafioPuzzleService
) {

    @PostMapping("/generar/{partidaId}")
    fun generarDesafio(@PathVariable partidaId: Long): ResponseEntity<DesafioGeneradoResponse> {
        val desafio = desafioPuzzleService.generarDesafio(partidaId)
        return ResponseEntity.ok(desafio)
    }

    @PostMapping("/responder")
    fun responderDesafio(@RequestBody request: ResponderDesafioRequest): ResponseEntity<ResponderDesafioResponse> {
        val respuesta = desafioPuzzleService.responderDesafio(request)
        return ResponseEntity.ok(respuesta)
    }

    @PostMapping("/usar-powerup")
    fun usarPowerUp(@RequestBody request: UsarPowerUpRequest): ResponseEntity<UsarPowerUpResponse> {
        val respuesta = desafioPuzzleService.usarPowerUp(request)
        return ResponseEntity.ok(respuesta)
    }

    @GetMapping("/powerups/{partidaId}")
    fun obtenerPowerUps(@PathVariable partidaId: Long): ResponseEntity<List<PowerUpDisponibleDTO>> {
        val powerUps = desafioPuzzleService.obtenerPowerUpsDisponibles(partidaId)
        return ResponseEntity.ok(powerUps)
    }
}