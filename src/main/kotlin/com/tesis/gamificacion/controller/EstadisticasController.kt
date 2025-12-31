package com.tesis.gamificacion.controller

import com.tesis.gamificacion.dto.EstadisticasDetalladasResponse
import com.tesis.gamificacion.service.EstadisticasService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/estadisticas")
class EstadisticasController(
    private val estadisticasService: EstadisticasService
) {
    private val logger = LoggerFactory.getLogger(EstadisticasController::class.java)

    @GetMapping("/{usuarioId}")
    fun obtenerEstadisticasDetalladas(
        @PathVariable usuarioId: Long
    ): ResponseEntity<EstadisticasDetalladasResponse> {
        logger.info("🌐 GET /estadisticas/{}", usuarioId)

        return try {
            val estadisticas = estadisticasService.obtenerEstadisticasDetalladas(usuarioId)
            logger.info("✅ Estadísticas obtenidas exitosamente")
            ResponseEntity.ok(estadisticas)
        } catch (e: Exception) {
            logger.error("❌ Error obteniendo estadísticas: {}", e.message, e)
            throw e
        }
    }
}