package com.tesis.gamificacion.controller

import com.tesis.gamificacion.dto.request.FinalizarPartidaRequest
import com.tesis.gamificacion.dto.request.IniciarPartidaRequest
import com.tesis.gamificacion.dto.response.EstadisticasJugadorResponse
import com.tesis.gamificacion.dto.response.IniciarPartidaResponse
import com.tesis.gamificacion.dto.response.PartidaResponse
import com.tesis.gamificacion.dto.response.RankingResponse
import com.tesis.gamificacion.model.enums.CategoriasCultural
import com.tesis.gamificacion.model.enums.NivelDificultad
import com.tesis.gamificacion.model.request.ProcesarErrorRequest
import com.tesis.gamificacion.model.request.ProcesarParejaRequest
import com.tesis.gamificacion.model.request.ResponderPreguntaRequest
import com.tesis.gamificacion.model.request.SolicitarHintRequest
import com.tesis.gamificacion.model.responses.FinalizarPartidaResponse
import com.tesis.gamificacion.model.responses.ProcesarErrorResponse
import com.tesis.gamificacion.model.responses.ProcesarParejaResponse
import com.tesis.gamificacion.model.responses.ResponderPreguntaResponse
import com.tesis.gamificacion.model.responses.SolicitarHintResponse
import com.tesis.gamificacion.service.PartidaService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/partidas")
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
    ): ResponseEntity<FinalizarPartidaResponse> {
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


    @GetMapping("/ranking/{nivel}/{categoria}")
    fun obtenerRankingPorNivelYCategoria(
        @PathVariable nivel: NivelDificultad,
        @PathVariable categoria: CategoriasCultural,
        @RequestParam(defaultValue = "10") limite: Int
    ): ResponseEntity<List<RankingResponse>> {
        val ranking = partidaService.obtenerRankingPorNivelYCategoria(nivel, categoria, limite)
        return ResponseEntity.ok(ranking)
    }

    // NUEVOS ENDPOINTS DE GAMIFICACIÓN

    /**
     * Procesa un error (pareja incorrecta) - Retorna narrativa educativa
     */
    @PostMapping("/{id}/error")
    fun procesarError(
        @PathVariable id: Long,
        @RequestBody request: ProcesarErrorRequest
    ): ResponseEntity<ProcesarErrorResponse> {
        return ResponseEntity.ok(partidaService.procesarError(request))
    }

    /**
     * Procesa una pareja correcta - Puede retornar diálogo cultural
     */
    @PostMapping("/{id}/pareja-correcta")
    fun procesarParejaCorrecta(
        @PathVariable id: Long,
        @RequestBody request: ProcesarParejaRequest
    ): ResponseEntity<ProcesarParejaResponse> {
        return ResponseEntity.ok(partidaService.procesarParejaCorrecta(request))
    }

    /**
     * Solicita un hint - Cuesta puntos
     */
    @PostMapping("/{id}/solicitar-hint")
    fun solicitarHint(
        @PathVariable id: Long,
        @RequestBody request: SolicitarHintRequest
    ): ResponseEntity<SolicitarHintResponse> {
        return ResponseEntity.ok(partidaService.solicitarHint(request))
    }

    /**
     * Responde pregunta de recuperación de vida
     */
    @PostMapping("/{id}/responder-pregunta")
    fun responderPregunta(
        @PathVariable id: Long,
        @RequestBody request: ResponderPreguntaRequest
    ): ResponseEntity<ResponderPreguntaResponse> {
        return ResponseEntity.ok(partidaService.responderPregunta(request))
    }

    @GetMapping("/ranking")
    fun obtenerRanking(@RequestParam(defaultValue = "10") limite: Int): ResponseEntity<List<RankingResponse>> {
        return ResponseEntity.ok(partidaService.obtenerRankingGlobal(limite))
    }
}