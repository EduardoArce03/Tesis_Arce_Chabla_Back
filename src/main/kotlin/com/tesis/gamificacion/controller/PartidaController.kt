package com.tesis.gamificacion.controller

import com.tesis.gamificacion.dto.request.FinalizarPartidaRequest
import com.tesis.gamificacion.dto.request.IniciarPartidaRequest
import com.tesis.gamificacion.dto.response.EstadisticasJugadorResponse
import com.tesis.gamificacion.dto.response.IniciarPartidaResponse
import com.tesis.gamificacion.dto.response.PartidaResponse
import com.tesis.gamificacion.dto.response.RankingResponse
import com.tesis.gamificacion.model.enums.CategoriasCultural
import com.tesis.gamificacion.model.enums.NivelDificultad
import com.tesis.gamificacion.service.PartidaService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/partidas")
@CrossOrigin(origins = ["http://localhost:4200"])
class PartidaController(
    private val partidaService: PartidaService
) {

    @PostMapping("/iniciar")
    fun iniciarPartida(
        @Valid @RequestBody request: IniciarPartidaRequest
    ): ResponseEntity<IniciarPartidaResponse> {
        val response = partidaService.iniciarPartida(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/finalizar")
    fun finalizarPartida(
        @Valid @RequestBody request: FinalizarPartidaRequest
    ): ResponseEntity<PartidaResponse> {
        val response = partidaService.finalizarPartida(request)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/historial/{jugadorId}")
    fun obtenerHistorial(
        @PathVariable jugadorId: String
    ): ResponseEntity<List<PartidaResponse>> {
        val historial = partidaService.obtenerHistorialJugador(jugadorId)
        return ResponseEntity.ok(historial)
    }

    @GetMapping("/estadisticas/{jugadorId}")
    fun obtenerEstadisticas(
        @PathVariable jugadorId: String
    ): ResponseEntity<EstadisticasJugadorResponse> {
        val estadisticas = partidaService.obtenerEstadisticasJugador(jugadorId)
        return ResponseEntity.ok(estadisticas)
    }

    @GetMapping("/ranking")
    fun obtenerRankingGlobal(
        @RequestParam(defaultValue = "10") limite: Int
    ): ResponseEntity<List<RankingResponse>> {
        val ranking = partidaService.obtenerRankingGlobal(limite)
        return ResponseEntity.ok(ranking)
    }

    @GetMapping("/ranking/{nivel}/{categoria}")
    fun obtenerRankingPorNivelYCategoria(
        @PathVariable nivel: NivelDificultad,
        @PathVariable categoria: CategoriasCultural,
        @RequestParam(defaultValue = "10") limite: Int
    ): ResponseEntity<List<RankingResponse>> {
        val ranking = partidaService.obtenerRankingPorNivelYCategoria(nivel, categoria, limite)
        return ResponseEntity.ok(ranking)
    }
}