package com.tesis.gamificacion.controller

import com.tesis.gamificacion.model.request.FinalizarPuzzleRequest
import com.tesis.gamificacion.model.request.IniciarPuzzleRequest
import com.tesis.gamificacion.model.responses.FinalizarPuzzleResponse
import com.tesis.gamificacion.model.responses.ImagenPuzzleDTO
import com.tesis.gamificacion.model.responses.IniciarPuzzleResponse
import com.tesis.gamificacion.model.responses.ProgresoJugadorDTO
import com.tesis.gamificacion.service.PuzzleService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/puzzle")
@CrossOrigin(origins = ["http://localhost:4200"])
class PuzzleController(
    private val puzzleService: PuzzleService
) {

    @GetMapping("/imagenes/{jugadorId}")
    fun obtenerImagenesDisponibles(
        @PathVariable jugadorId: String
    ): ResponseEntity<List<ImagenPuzzleDTO>> {
        val imagenes = puzzleService.obtenerImagenesDisponibles(jugadorId)
        return ResponseEntity.ok(imagenes)
    }

    @GetMapping("/progreso/{jugadorId}")
    fun obtenerProgreso(
        @PathVariable jugadorId: String
    ): ResponseEntity<ProgresoJugadorDTO> {
        val progreso = puzzleService.obtenerProgreso(jugadorId)
        return ResponseEntity.ok(progreso)
    }

    @PostMapping("/iniciar")
    fun iniciarPuzzle(
        @RequestBody request: IniciarPuzzleRequest
    ): ResponseEntity<IniciarPuzzleResponse> {
        val response = puzzleService.iniciarPuzzle(request)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/finalizar")
    fun finalizarPuzzle(
        @RequestBody request: FinalizarPuzzleRequest
    ): ResponseEntity<FinalizarPuzzleResponse> {
        val response = puzzleService.finalizarPuzzle(request)
        return ResponseEntity.ok(response)
    }
}