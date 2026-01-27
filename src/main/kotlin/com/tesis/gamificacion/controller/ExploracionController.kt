package com.tesis.gamificacion.controller

import com.tesis.gamificacion.model.data.CapturarFotoRequest
import com.tesis.gamificacion.model.data.CapturarFotoResponse
import com.tesis.gamificacion.model.data.DialogarRequest
import com.tesis.gamificacion.model.data.DialogarResponse
import com.tesis.gamificacion.model.data.ExplorarCapaRequest
import com.tesis.gamificacion.model.data.ExplorarCapaResponse
import com.tesis.gamificacion.model.data.MapaDTO
import com.tesis.gamificacion.model.data.PartidaDTO
import com.tesis.gamificacion.service.ExploracionService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/exploracion")
@CrossOrigin(origins = ["http://localhost:4200"]) // Ajusta según tu frontend
class ExploracionController(
    private val exploracionService: ExploracionService
) {

    private val log = LoggerFactory.getLogger(ExploracionController::class.java)

    // ==================== INICIAR PARTIDA ====================

    /**
     * POST /api/exploracion/iniciar?jugadorId={id}
     * Crea una nueva partida para el jugador
     */
    @PostMapping("/iniciar")
    fun iniciarPartida(
        @RequestParam jugadorId: Long
    ): ResponseEntity<PartidaDTO> {
        log.info("🎮 POST /iniciar - Jugador: $jugadorId")

        return try {
            val partida = exploracionService.iniciarPartida(jugadorId)

            log.info("✅ Partida ${partida.id} creada exitosamente")
            ResponseEntity.ok(partida)

        } catch (e: Exception) {
            log.error("❌ Error iniciando partida: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    // ==================== OBTENER MAPA ====================

    /**
     * GET /api/exploracion/mapa/{partidaId}
     * Obtiene el estado completo del mapa con todos los puntos y capas
     */
    @GetMapping("/mapa/{partidaId}")
    fun obtenerMapa(
        @PathVariable partidaId: Long
    ): ResponseEntity<MapaDTO> {
        log.info("🗺️ GET /mapa/$partidaId")

        return try {
            val mapa = exploracionService.obtenerMapa(partidaId)

            log.info("✅ Mapa obtenido - ${mapa.puntos.size} puntos")
            ResponseEntity.ok(mapa)

        } catch (e: IllegalArgumentException) {
            log.warn("⚠️ Partida no encontrada: ${e.message}")
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()

        } catch (e: Exception) {
            log.error("❌ Error obteniendo mapa: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    // ==================== EXPLORAR CAPA ====================

    /**
     * POST /api/exploracion/explorar-capa
     * Entra a una capa específica de un punto
     * Body: { "partidaId": 1, "puntoId": 1, "capaNivel": "ACTUAL" }
     */
    @PostMapping("/explorar-capa")
    fun explorarCapa(
        @RequestBody request: ExplorarCapaRequest
    ): ResponseEntity<ExplorarCapaResponse> {
        log.info("📍 POST /explorar-capa - Partida: ${request.partidaId}, Punto: ${request.puntoId}, Capa: ${request.capaNivel}")

        return try {
            val response = exploracionService.explorarCapa(request)

            log.info("✅ Capa explorada - Primer descubrimiento: ${response.primerDescubrimiento}")
            ResponseEntity.ok(response)

        } catch (e: IllegalArgumentException) {
            log.warn("⚠️ Datos inválidos: ${e.message}")
            ResponseEntity.badRequest().body(
                ExplorarCapaResponse(
                    exito = false,
                    capa = null, // Se manejará en el catch
                    narrativa = null ,
                    objetivosFotograficos = emptyList(),
                    primerDescubrimiento = false,
                    mensaje = e.message
                )
            )

        } catch (e: IllegalStateException) {
            log.warn("🔒 Capa bloqueada: ${e.message}")
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ExplorarCapaResponse(
                    exito = false,
                    capa = null,
                    narrativa = null,
                    objetivosFotograficos = emptyList(),
                    primerDescubrimiento = false,
                    mensaje = e.message
                )
            )

        } catch (e: Exception) {
            log.error("❌ Error explorando capa: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    // ==================== CAPTURAR FOTOGRAFÍA ====================

    /**
     * POST /api/exploracion/capturar-foto
     * Captura una fotografía de un objetivo
     * Body: { "partidaId": 1, "progresoCapaId": 5, "objetivoId": 101, "imagenBase64": "..." }
     */
    @PostMapping("/capturar-foto")
    fun capturarFotografia(
        @RequestBody request: CapturarFotoRequest
    ): ResponseEntity<CapturarFotoResponse> {
        log.info("📸 POST /capturar-foto - Objetivo: ${request.objetivoId}")

        return try {
            val response = exploracionService.capturarFotografia(request)

            if (response.exito) {
                log.info("✅ Fotografía capturada - ${response.fotografiasCompletadas}/${response.fotografiasRequeridas}")
                ResponseEntity.ok(response)
            } else {
                log.info("⚠️ Fotografía rechazada: ${response.mensaje}")
                ResponseEntity.ok(response) // 200 pero con exito=false
            }

        } catch (e: IllegalArgumentException) {
            log.warn("⚠️ Datos inválidos: ${e.message}")
            ResponseEntity.badRequest().body(
                CapturarFotoResponse(
                    exito = false,
                    mensaje = e.message ?: "Datos inválidos"
                )
            )

        } catch (e: Exception) {
            log.error("❌ Error capturando foto: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                CapturarFotoResponse(
                    exito = false,
                    mensaje = "Error interno del servidor"
                )
            )
        }
    }

    // ==================== DIALOGAR CON ESPÍRITU ====================

    /**
     * POST /api/exploracion/dialogar
     * Envía una pregunta al espíritu de la capa
     * Body: { "partidaId": 1, "progresoCapaId": 5, "pregunta": "¿Qué rituales se hacían aquí?" }
     */
    @PostMapping("/dialogar")
    fun dialogar(
        @RequestBody request: DialogarRequest
    ): ResponseEntity<DialogarResponse> {
        log.info("💬 POST /dialogar - Pregunta: ${request.pregunta.take(50)}...")

        return try {
            val response = exploracionService.dialogar(request)

            log.info("✅ Diálogo completado - Total: ${response.dialogosRealizados}")
            ResponseEntity.ok(response)

        } catch (e: IllegalArgumentException) {
            log.warn("⚠️ Datos inválidos: ${e.message}")
            ResponseEntity.badRequest().body(
                DialogarResponse(
                    exito = false,
                    respuesta = "",
                    nombreEspiritu = "",
                    dialogosRealizados = 0,
                    mensaje = e.message
                )
            )

        } catch (e: Exception) {
            log.error("❌ Error en diálogo: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                DialogarResponse(
                    exito = false,
                    respuesta = "",
                    nombreEspiritu = "",
                    dialogosRealizados = 0,
                    mensaje = "Error interno del servidor"
                )
            )
        }
    }

    // ==================== ENDPOINTS ADICIONALES ÚTILES ====================

    /**
     * GET /api/exploracion/partida/{partidaId}
     * Obtiene información básica de la partida
     */
    @GetMapping("/partida/{partidaId}")
    fun obtenerPartida(
        @PathVariable partidaId: Long
    ): ResponseEntity<PartidaDTO> {
        log.info("📊 GET /partida/$partidaId")

        return try {
            val partida = exploracionService.obtenerPartidaDTO(partidaId)
            ResponseEntity.ok(partida)

        } catch (e: IllegalArgumentException) {
            log.warn("⚠️ Partida no encontrada")
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()

        } catch (e: Exception) {
            log.error("❌ Error obteniendo partida: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    /**
     * DELETE /api/exploracion/partida/{partidaId}
     * Elimina una partida (para testing o reset)
     */
    @DeleteMapping("/partida/{partidaId}")
    fun eliminarPartida(
        @PathVariable partidaId: Long
    ): ResponseEntity<Void> {
        log.info("🗑️ DELETE /partida/$partidaId")

        return try {
            exploracionService.eliminarPartida(partidaId)

            log.info("✅ Partida eliminada")
            ResponseEntity.noContent().build()

        } catch (e: IllegalArgumentException) {
            log.warn("⚠️ Partida no encontrada")
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()

        } catch (e: Exception) {
            log.error("❌ Error eliminando partida: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    /**
     * GET /api/exploracion/jugador/{jugadorId}/partidas
     * Obtiene todas las partidas de un jugador
     */
    @GetMapping("/jugador/{jugadorId}/partidas")
    fun obtenerPartidasJugador(
        @PathVariable jugadorId: Long
    ): ResponseEntity<List<PartidaDTO>> {
        log.info("📋 GET /jugador/$jugadorId/partidas")

        return try {
            val partidas = exploracionService.obtenerPartidasJugador(jugadorId)

            log.info("✅ ${partidas.size} partidas encontradas")
            ResponseEntity.ok(partidas)

        } catch (e: Exception) {
            log.error("❌ Error obteniendo partidas: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
}