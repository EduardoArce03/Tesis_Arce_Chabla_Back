package com.tesis.gamificacion.controller

import com.tesis.gamificacion.dto.request.*
import com.tesis.gamificacion.dto.response.*
import com.tesis.gamificacion.service.MisionService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/misiones")
class MisionController(
    private val misionService: MisionService
) {
    private val logger = LoggerFactory.getLogger(MisionController::class.java)

    /**
     * GET /api/misiones/{usuarioId}
     * Obtener listado de misiones categorizado por estado
     */
    @GetMapping("/{usuarioId}")
    fun obtenerListadoMisiones(
        @PathVariable usuarioId: Long
    ): ResponseEntity<ListaMisionesResponse> {
        logger.info("🌐 GET /misiones/{}", usuarioId)

        return try {
            val listado = misionService.obtenerListadoMisiones(usuarioId)
            logger.info("✅ Listado de misiones obtenido - {} disponibles", listado.disponibles.size)
            ResponseEntity.ok(listado)
        } catch (e: Exception) {
            logger.error("❌ Error obteniendo listado: {}", e.message, e)
            throw e
        }
    }

    /**
     * GET /api/misiones/{misionId}/detalle
     * Obtener detalle completo de una misión
     */
    @GetMapping("/{misionId}/detalle")
    fun obtenerDetalleMision(
        @PathVariable misionId: Long,
        @RequestParam usuarioId: Long
    ): ResponseEntity<DetalleMisionResponse> {
        logger.info("🌐 GET /misiones/{}/detalle?usuarioId={}", misionId, usuarioId)

        return try {
            val detalle = misionService.obtenerDetalleMision(misionId, usuarioId)
            logger.info("✅ Detalle de misión obtenido - {} fases", detalle.fases.size)
            ResponseEntity.ok(detalle)
        } catch (e: IllegalArgumentException) {
            logger.error("❌ Misión no encontrada: {}", e.message)
            throw e
        } catch (e: Exception) {
            logger.error("❌ Error obteniendo detalle: {}", e.message, e)
            throw e
        }
    }

    /**
     * POST /api/misiones/{misionId}/iniciar
     * Iniciar una misión
     */
    @PostMapping("/{misionId}/iniciar")
    fun iniciarMision(
        @PathVariable misionId: Long,
        @RequestParam usuarioId: Long
    ): ResponseEntity<IniciarMisionResponse> {
        logger.info("🌐 POST /misiones/{}/iniciar?usuarioId={}", misionId, usuarioId)

        return try {
            val resultado = misionService.iniciarMision(misionId, usuarioId)
            logger.info("✅ Misión {} iniciada para usuario {}", misionId, usuarioId)
            ResponseEntity.ok(resultado)
        } catch (e: IllegalArgumentException) {
            logger.error("❌ Error validación: {}", e.message)
            throw e
        } catch (e: Exception) {
            logger.error("❌ Error iniciando misión: {}", e.message, e)
            throw e
        }
    }

    /**
     * GET /api/misiones/progreso/{usuarioMisionId}/fase-actual
     * Obtener la fase actual en ejecución
     */
    @GetMapping("/progreso/{usuarioMisionId}/fase-actual")
    fun obtenerFaseActual(
        @PathVariable usuarioMisionId: Long
    ): ResponseEntity<FaseEjecucionDTO> {
        logger.info("🌐 GET /misiones/progreso/{}/fase-actual", usuarioMisionId)

        return try {
            val fase = misionService.obtenerFaseActual(usuarioMisionId)
            logger.info("✅ Fase actual obtenida - Fase {}", fase.numeroFase)
            ResponseEntity.ok(fase)
        } catch (e: Exception) {
            logger.error("❌ Error obteniendo fase actual: {}", e.message, e)
            throw e
        }
    }

    /**
     * POST /api/misiones/responder-fase
     * Responder/completar una fase de la misión
     */
    @PostMapping("/responder-fase")
    fun responderFase(
        @Valid @RequestBody request: ResponderFaseRequest
    ): ResponseEntity<ResponderFaseResponse> {
        logger.info("🌐 POST /misiones/responder-fase")

        return try {
            val resultado = misionService.responderFase(request)
            logger.info("✅ Fase respondida - Correctas: {}, Puntuación: {}",
                resultado.correctas, resultado.puntuacion)

            if (resultado.misionCompletada) {
                logger.info("🎉 ¡Misión completada! Insignias: {}", resultado.insigniasObtenidas.size)
            }

            ResponseEntity.ok(resultado)
        } catch (e: IllegalArgumentException) {
            logger.error("❌ Error validación: {}", e.message)
            throw e
        } catch (e: Exception) {
            logger.error("❌ Error respondiendo fase: {}", e.message, e)
            throw e
        }
    }

    /**
     * GET /api/misiones/estadisticas/{usuarioId}
     * Obtener estadísticas generales de misiones del usuario
     */
    @GetMapping("/estadisticas/{usuarioId}")
    fun obtenerEstadisticas(
        @PathVariable usuarioId: Long
    ): ResponseEntity<EstadisticasMisionesDTO> {
        logger.info("🌐 GET /misiones/estadisticas/{}", usuarioId)

        return try {
            val listado = misionService.obtenerListadoMisiones(usuarioId)
            logger.info("✅ Estadísticas obtenidas")
            ResponseEntity.ok(listado.estadisticas)
        } catch (e: Exception) {
            logger.error("❌ Error obteniendo estadísticas: {}", e.message, e)
            throw e
        }
    }

    /**
     * GET /api/misiones/insignias/{usuarioId}
     * Obtener colección de insignias del usuario
     */
    @GetMapping("/insignias/{usuarioId}")
    fun obtenerInsignias(
        @PathVariable usuarioId: Long
    ): ResponseEntity<ColeccionInsigniasResponse> {
        logger.info("🌐 GET /misiones/insignias/{}", usuarioId)

        return try {
            val coleccion = misionService.obtenerColeccionInsignias(usuarioId)
            logger.info("✅ Colección de insignias obtenida - {}/{}",
                coleccion.totalObtenidas, coleccion.totalDisponibles)
            ResponseEntity.ok(coleccion)
        } catch (e: Exception) {
            logger.error("❌ Error obteniendo insignias: {}", e.message, e)
            throw e
        }
    }

    /**
     * GET /api/misiones/progreso/{usuarioMisionId}/fase-actual
     * Obtener la fase actual en ejecución
     */

    /**
     * POST /api/misiones/progreso/{usuarioMisionId}/responder-quiz
     * Responder un quiz de una fase
     */
    @PostMapping("/progreso/{usuarioMisionId}/responder-quiz")
    fun responderQuiz(
        @PathVariable usuarioMisionId: Long,
        @RequestBody request: ResponderQuizRequest
    ): ResponseEntity<ResponderFaseResponse> {
        logger.info("🌐 POST /misiones/progreso/{}/responder-quiz", usuarioMisionId)

        return try {
            val resultado = misionService.responderFaseQuizUnico(
                usuarioMisionId,
                request.preguntaId,
                request.respuesta
            )

            logger.info("✅ Quiz respondido - Correcto: {}, Puntuación: {}",
                resultado.correctas > 0, resultado.puntuacion)

            if (resultado.misionCompletada) {
                logger.info("🎉 ¡Misión completada! Insignias: {}", resultado.insigniasObtenidas.size)
            }

            ResponseEntity.ok(resultado)
        } catch (e: IllegalArgumentException) {
            logger.error("❌ Error validación: {}", e.message)
            throw e
        } catch (e: Exception) {
            logger.error("❌ Error respondiendo quiz: {}", e.message, e)
            throw e
        }
    }

    /**
     * POST /api/misiones/progreso/{usuarioMisionId}/avanzar
     * Avanzar a la siguiente fase
     */
    @PostMapping("/progreso/{usuarioMisionId}/avanzar")
    fun avanzarFase(
        @PathVariable usuarioMisionId: Long
    ): ResponseEntity<FaseEjecucionDTO?> {
        logger.info("🌐 POST /misiones/progreso/{}/avanzar", usuarioMisionId)

        return try {
            val siguienteFase = misionService.avanzarFase(usuarioMisionId)

            if (siguienteFase != null) {
                logger.info("✅ Avanzado a fase {}", siguienteFase.numeroFase)
            } else {
                logger.info("🎉 Misión completada!")
            }

            ResponseEntity.ok(siguienteFase)
        } catch (e: IllegalArgumentException) {
            logger.error("❌ Error: {}", e.message)
            throw e
        } catch (e: Exception) {
            logger.error("❌ Error avanzando fase: {}", e.message, e)
            throw e
        }
    }
}