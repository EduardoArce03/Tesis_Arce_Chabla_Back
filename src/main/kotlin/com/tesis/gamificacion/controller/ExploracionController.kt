// src/main/kotlin/com/tesis/gamificacion/controller/ExploracionController.kt
package com.tesis.gamificacion.controller

import com.tesis.gamificacion.model.enums.NivelCapa
import com.tesis.gamificacion.model.request.*
import com.tesis.gamificacion.model.responses.*
import com.tesis.gamificacion.service.ExploracionService
import com.tesis.gamificacion.service.ExploracionCapasService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/exploracion")
@CrossOrigin(origins = ["http://localhost:4200"])
class ExploracionController(
    private val exploracionService: ExploracionService,
    private val exploracionCapasService: ExploracionCapasService
) {

    // ==================== INICIALIZACIÓN ====================

    @PostMapping("/inicializar")
    fun inicializarExploracion(
        @RequestParam partidaId: Long,
        @RequestParam usuarioId: Long
    ): ResponseEntity<ProgresoExploracionResponse> {
        val progreso = exploracionService.inicializarExploracion(partidaId, usuarioId)
        return ResponseEntity.ok(progreso)
    }

    @GetMapping("/progreso/{partidaId}")
    fun obtenerProgresoCompleto(
        @PathVariable partidaId: Long
    ): ResponseEntity<ProgresoExploracionResponse> {
        val progreso = exploracionService.obtenerProgresoCompleto(partidaId)
        return ResponseEntity.ok(progreso)
    }

    // ==================== PUNTOS DE INTERÉS ====================

    @GetMapping("/puntos/{partidaId}")
    fun obtenerPuntosDisponibles(
        @PathVariable partidaId: Long
    ): ResponseEntity<List<PuntoInteresDTO>> {
        val puntos = exploracionService.obtenerPuntosDisponibles(partidaId)
        return ResponseEntity.ok(puntos)
    }

    @PostMapping("/puntos/descubrir")
    fun descubrirPunto(
        @Valid @RequestBody request: DescubrirPuntoRequest
    ): ResponseEntity<DescubrimientoPuntoResponse> {
        val resultado = exploracionService.descubrirPunto(request)
        return ResponseEntity.ok(resultado)
    }

    // ==================== CAPAS POR PUNTO (NUEVO) ====================

    @GetMapping("/puntos/{puntoId}/capas")
    fun obtenerCapasPunto(
        @PathVariable puntoId: Long,
        @RequestParam partidaId: Long
    ): ResponseEntity<List<CapaPuntoDTO>> {
        val capas = exploracionCapasService.obtenerCapasPunto(puntoId, partidaId)
        return ResponseEntity.ok(capas)
    }

    @PostMapping("/puntos/capa/descubrir")
    fun descubrirCapaPunto(
        @Valid @RequestBody request: DescubrirCapaPuntoRequest
    ): ResponseEntity<DescubrirCapaPuntoResponse> {
        val resultado = exploracionCapasService.descubrirCapaPunto(request)
        return ResponseEntity.ok(resultado)
    }

    // ==================== CAPAS TEMPORALES ====================

    @GetMapping("/capas/{partidaId}")
    fun obtenerCapas(
        @PathVariable partidaId: Long
    ): ResponseEntity<List<NivelCapaDTO>> {
        val capas = exploracionService.obtenerCapas(partidaId)
        return ResponseEntity.ok(capas)
    }

    // ==================== FOTOGRAFÍA ====================

    @GetMapping("/fotografia/objetivos/{partidaId}")
    fun obtenerObjetivosFotograficos(
        @PathVariable partidaId: Long,
        @RequestParam(required = false) puntoId: Long?
    ): ResponseEntity<List<FotografiaObjetivoDTO>> {
        val objetivos = exploracionService.obtenerObjetivosFotograficos(partidaId, puntoId)
        return ResponseEntity.ok(objetivos)
    }

    @PostMapping("/fotografia/capturar")
    fun capturarFotografia(
        @Valid @RequestBody request: CapturarFotografiaRequest
    ): ResponseEntity<CapturarFotografiaResponse> {
        val resultado = exploracionService.capturarFotografia(request)
        return ResponseEntity.ok(resultado)
    }

    @GetMapping("/fotografia/galeria/{partidaId}")
    fun obtenerGaleriaFotografias(
        @PathVariable partidaId: Long
    ): ResponseEntity<List<FotografiaCapturadaDTO>> {
        val galeria = exploracionService.obtenerGaleriaFotografias(partidaId)
        return ResponseEntity.ok(galeria)
    }

    // ==================== DIÁLOGOS ====================

    @PostMapping("/dialogo/espiritu")
    fun dialogarConEspiritu(
        @Valid @RequestBody request: DialogarEspirituRequest
    ): ResponseEntity<DialogoEspirituResponse> {
        val resultado = exploracionService.dialogarConEspiritu(request)
        return ResponseEntity.ok(resultado)
    }

    @GetMapping("/dialogo/historial/{partidaId}")
    fun obtenerHistorialDialogos(
        @PathVariable partidaId: Long,
        @RequestParam(required = false) nivelCapa: NivelCapa?
    ): ResponseEntity<List<DialogoHistorialDTO>> {
        val historial = exploracionService.obtenerHistorialDialogos(partidaId, nivelCapa)
        return ResponseEntity.ok(historial)
    }

    // ==================== MISIONES ====================

    @GetMapping("/misiones/{partidaId}")
    fun obtenerMisionesDisponibles(
        @PathVariable partidaId: Long
    ): ResponseEntity<List<MisionDTO>> {
        val misiones = exploracionService.obtenerMisionesDisponibles(partidaId)
        return ResponseEntity.ok(misiones)
    }

    @PostMapping("/misiones/completar")
    fun completarMision(
        @Valid @RequestBody request: CompletarMisionRequest
    ): ResponseEntity<CompletarMisionResponse> {
        val resultado = exploracionService.completarMision(request)
        return ResponseEntity.ok(resultado)
    }
}