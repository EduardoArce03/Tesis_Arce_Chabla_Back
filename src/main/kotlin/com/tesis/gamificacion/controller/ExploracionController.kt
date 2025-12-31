package com.tesis.gamificacion.controller

import com.tesis.gamificacion.dto.request.*
import com.tesis.gamificacion.dto.response.*
import com.tesis.gamificacion.service.ExploracionService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/exploracion")
class ExploracionController(
    private val exploracionService: ExploracionService
) {
    private val logger = LoggerFactory.getLogger(ExploracionController::class.java)

    /**
     * GET /api/exploracion/dashboard/{usuarioId}
     * Obtener dashboard completo de exploración
     */
    @GetMapping("/dashboard/{usuarioId}")
    fun obtenerDashboard(
        @PathVariable usuarioId: Long
    ): ResponseEntity<DashboardExploracionResponse> {
        logger.info("🌐 GET /exploracion/dashboard/{}", usuarioId)

        return try {
            val dashboard = exploracionService.obtenerDashboard(usuarioId)
            logger.info("✅ Dashboard de exploración obtenido exitosamente")
            ResponseEntity.ok(dashboard)
        } catch (e: Exception) {
            logger.error("❌ Error obteniendo dashboard: {}", e.message, e)
            throw e
        }
    }

    /**
     * POST /api/exploracion/visitar
     * Visitar un punto de interés
     */
    @PostMapping("/visitar")
    fun visitarPunto(
        @Valid @RequestBody request: VisitarPuntoRequest
    ): ResponseEntity<VisitaPuntoResponse> {
        logger.info("🌐 POST /exploracion/visitar - Usuario: {}, Punto: {}",
            request.usuarioId, request.puntoId)

        return try {
            val resultado = exploracionService.visitarPunto(request)
            logger.info("✅ Punto visitado exitosamente")
            ResponseEntity.ok(resultado)
        } catch (e: IllegalArgumentException) {
            logger.error("❌ Error de validación: {}", e.message)
            throw e
        } catch (e: Exception) {
            logger.error("❌ Error visitando punto: {}", e.message, e)
            throw e
        }
    }

    /**
     * GET /api/exploracion/punto/{puntoId}/detalle
     * Obtener detalle completo de un punto
     */
    @GetMapping("/punto/{puntoId}/detalle")
    fun obtenerDetallePunto(
        @PathVariable puntoId: Long,
        @RequestParam usuarioId: Long
    ): ResponseEntity<DetallePuntoResponse> {
        logger.info("🌐 GET /exploracion/punto/{}/detalle", puntoId)

        return try {
            val detalle = exploracionService.obtenerDetallePunto(puntoId, usuarioId)
            logger.info("✅ Detalle del punto obtenido exitosamente")
            ResponseEntity.ok(detalle)
        } catch (e: Exception) {
            logger.error("❌ Error obteniendo detalle: {}", e.message, e)
            throw e
        }
    }

    /**
     * POST /api/exploracion/quiz/responder
     * Responder pregunta del quiz
     */
    @PostMapping("/quiz/responder")
    fun responderQuiz(
        @Valid @RequestBody request: ResponderQuizRequest
    ): ResponseEntity<ResultadoQuizResponse> {
        logger.info("🌐 POST /exploracion/quiz/responder")

        return try {
            val resultado = exploracionService.responderQuiz(request)
            logger.info("✅ Quiz respondido - Correcto: {}", resultado.correcto)
            ResponseEntity.ok(resultado)
        } catch (e: Exception) {
            logger.error("❌ Error respondiendo quiz: {}", e.message, e)
            throw e
        }
    }

    /**
     * POST /api/exploracion/artefacto/buscar
     * Buscar artefacto en un punto
     */
    @PostMapping("/artefacto/buscar")
    fun buscarArtefacto(
        @Valid @RequestBody request: BuscarArtefactoRequest
    ): ResponseEntity<ResultadoBusquedaResponse> {
        logger.info("🌐 POST /exploracion/artefacto/buscar")

        return try {
            val resultado = exploracionService.buscarArtefactoManual(request)
            logger.info("✅ Búsqueda completada - Encontrado: {}", resultado.encontrado)
            ResponseEntity.ok(resultado)
        } catch (e: Exception) {
            logger.error("❌ Error buscando artefacto: {}", e.message, e)
            throw e
        }
    }

    /**
     * GET /api/exploracion/coleccion/{usuarioId}
     * Obtener colección de artefactos del usuario
     */
    @GetMapping("/coleccion/{usuarioId}")
    fun obtenerColeccion(
        @PathVariable usuarioId: Long
    ): ResponseEntity<List<ArtefactoDTO>> {
        logger.info("🌐 GET /exploracion/coleccion/{}", usuarioId)

        return try {
            val coleccion = exploracionService.obtenerColeccionArtefactos(usuarioId)
            logger.info("✅ Colección obtenida - {} artefactos", coleccion.size)
            ResponseEntity.ok(coleccion)
        } catch (e: Exception) {
            logger.error("❌ Error obteniendo colección: {}", e.message, e)
            throw e
        }
    }

    /**
     * GET /api/exploracion/misiones/{usuarioId}
     * Obtener misiones disponibles para el usuario
     */
    @GetMapping("/misiones/{usuarioId}")
    fun obtenerMisiones(
        @PathVariable usuarioId: Long
    ): ResponseEntity<List<MisionDTO>> {
        logger.info("🌐 GET /exploracion/misiones/{}", usuarioId)

        return try {
            val misiones = exploracionService.obtenerMisionesDisponibles(usuarioId)
            logger.info("✅ Misiones obtenidas - {} disponibles", misiones.size)
            ResponseEntity.ok(misiones)
        } catch (e: Exception) {
            logger.error("❌ Error obteniendo misiones: {}", e.message, e)
            throw e
        }
    }

    /**
     * POST /api/exploracion/mision/{misionId}/aceptar
     * Aceptar una misión
     */
    @PostMapping("/mision/{misionId}/aceptar")
    fun aceptarMision(
        @PathVariable misionId: Long,
        @RequestParam usuarioId: Long
    ): ResponseEntity<MisionDTO> {
        logger.info("🌐 POST /exploracion/mision/{}/aceptar", misionId)

        return try {
            val mision = exploracionService.aceptarMision(usuarioId, misionId)
            logger.info("✅ Misión aceptada exitosamente")
            ResponseEntity.ok(mision)
        } catch (e: Exception) {
            logger.error("❌ Error aceptando misión: {}", e.message, e)
            throw e
        }
    }

    /**
     * GET /api/exploracion/estadisticas/{usuarioId}
     * Obtener estadísticas detalladas de exploración
     */
    @GetMapping("/estadisticas/{usuarioId}")
    fun obtenerEstadisticas(
        @PathVariable usuarioId: Long
    ): ResponseEntity<EstadisticasExploracionDTO> {
        logger.info("🌐 GET /exploracion/estadisticas/{}", usuarioId)

        return try {
            val estadisticas = exploracionService.obtenerEstadisticasDetalladas(usuarioId)
            logger.info("✅ Estadísticas obtenidas exitosamente")
            ResponseEntity.ok(estadisticas)
        } catch (e: Exception) {
            logger.error("❌ Error obteniendo estadísticas: {}", e.message, e)
            throw e
        }
    }
}