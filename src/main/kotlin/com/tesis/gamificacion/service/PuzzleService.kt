// src/main/kotlin/com/tesis/gamificacion/service/PuzzleService.kt
package com.tesis.gamificacion.service

import com.tesis.gamificacion.model.entities.*
import com.tesis.gamificacion.model.request.FinalizarPuzzleRequest
import com.tesis.gamificacion.model.request.IniciarPuzzleRequest
import com.tesis.gamificacion.model.responses.FinalizarPuzzleResponse
import com.tesis.gamificacion.model.responses.ImagenPuzzleDTO
import com.tesis.gamificacion.model.responses.IniciarPuzzleResponse
import com.tesis.gamificacion.model.responses.ProgresoJugadorDTO
import com.tesis.gamificacion.repository.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.min

@Service
class PuzzleService(
    private val partidaPuzzleRepository: PartidaPuzzleRepository,
    private val imagenPuzzleRepository: ImagenPuzzleRepository,
    private val progresoPuzzleRepository: ProgresoPuzzleRepository,
    @Value("\${app.base-url:http://localhost:8080}") private val baseUrl: String
) {

    companion object {
        // Tiempos ideales por dificultad (en segundos)
        private val TIEMPOS_IDEALES = mapOf(
            3 to 60,   // 1 minuto para 3x3
            4 to 120,  // 2 minutos para 4x4
            5 to 180,  // 3 minutos para 5x5
            6 to 300   // 5 minutos para 6x6
        )
    }

    /**
     * Obtiene todas las imágenes disponibles con su estado de desbloqueo
     */
    fun obtenerImagenesDisponibles(jugadorId: String): List<ImagenPuzzleDTO> {
        val todasLasImagenes = imagenPuzzleRepository.findAllByOrderByOrdenDesbloqueoAsc()
        val progreso = progresoPuzzleRepository.findByJugadorId(jugadorId)

        return todasLasImagenes.map { imagen ->
            val desbloqueada = imagen.ordenDesbloqueo == 1 ||
                    (progreso?.imagenesDesbloqueadas?.contains(imagen.id) == true)

            ImagenPuzzleDTO.fromEntity(imagen, desbloqueada)
        }
    }

    /**
     * Obtiene el progreso del jugador
     */
    fun obtenerProgreso(jugadorId: String): ProgresoJugadorDTO {
        val progreso = progresoPuzzleRepository.findByJugadorId(jugadorId)
            ?: return ProgresoJugadorDTO(
                jugadorId = jugadorId,
                estrellasTotal = 0,
                puzzlesCompletados = 0,
                mejorTiempo = 0,
                imagenesDesbloqueadas = 1 // La primera siempre desbloqueada
            )

        return ProgresoJugadorDTO(
            jugadorId = progreso.jugadorId,
            estrellasTotal = progreso.estrellasTotal,
            puzzlesCompletados = progreso.puzzlesCompletados,
            mejorTiempo = if (progreso.mejorTiempo == Int.MAX_VALUE) 0 else progreso.mejorTiempo,
            imagenesDesbloqueadas = progreso.imagenesDesbloqueadas.size + 1 // +1 por la primera
        )
    }

    /**
     * Inicia una nueva partida de puzzle
     */
    @Transactional
    fun iniciarPuzzle(request: IniciarPuzzleRequest): IniciarPuzzleResponse {
        val imagen = imagenPuzzleRepository.findById(request.imagenId)
            .orElseThrow { IllegalArgumentException("Imagen no encontrada: ${request.imagenId}") }

        // Verificar que esté desbloqueada
        val progreso = progresoPuzzleRepository.findByJugadorId(request.jugadorId)
        val desbloqueada = imagen.ordenDesbloqueo == 1 ||
                progreso?.imagenesDesbloqueadas?.contains(imagen.id) == true

        if (!desbloqueada) {
            throw IllegalStateException("Esta imagen aún no está desbloqueada")
        }

        // Validar gridSize
        if (request.gridSize < imagen.dificultadMinima || request.gridSize > imagen.dificultadMaxima) {
            throw IllegalArgumentException(
                "Grid size debe estar entre ${imagen.dificultadMinima} y ${imagen.dificultadMaxima}"
            )
        }

        // Crear partida
        val partida = PartidaPuzzle(
            jugadorId = request.jugadorId,
            imagen = imagen,
            gridSize = request.gridSize
        )
        val partidaGuardada = partidaPuzzleRepository.save(partida)

        println("✅ Partida iniciada: ID=${partidaGuardada.id}, Imagen=${imagen.titulo}")

        return IniciarPuzzleResponse(
            partidaId = partidaGuardada.id!!,
            imagen = ImagenPuzzleDTO.fromEntity(imagen, true),
            mensajeBienvenida = "¡Arma este rompecabezas de ${imagen.titulo}!"
        )
    }

    /**
     * Finaliza una partida y calcula resultados
     */
    @Transactional
    fun finalizarPuzzle(request: FinalizarPuzzleRequest): FinalizarPuzzleResponse {
        val partida = partidaPuzzleRepository.findById(request.partidaId)
            .orElseThrow { IllegalArgumentException("Partida no encontrada: ${request.partidaId}") }

        if (partida.completada) {
            throw IllegalStateException("Esta partida ya fue completada")
        }

        // Calcular estrellas
        val estrellas = calcularEstrellas(
            gridSize = partida.gridSize,
            tiempoSegundos = request.tiempoSegundos,
            hintsUsados = request.hintsUsados
        )

        // Actualizar partida
        partida.movimientos = request.movimientos
        partida.tiempoSegundos = request.tiempoSegundos
        partida.hintsUsados = request.hintsUsados
        partida.estrellas = estrellas
        partida.completada = true
        partida.fechaFin = java.time.LocalDateTime.now()
        partidaPuzzleRepository.save(partida)

        // Actualizar progreso
        val progreso = obtenerOCrearProgreso(partida.jugadorId)
        progreso.estrellasTotal += estrellas
        progreso.puzzlesCompletados++
        progreso.mejorTiempo = min(progreso.mejorTiempo, request.tiempoSegundos)

        // Desbloquear siguiente
        val siguienteImagen = desbloquearSiguienteImagen(progreso, partida.imagen)

        progresoPuzzleRepository.save(progreso)

        println("✅ Puzzle completado: ⭐x$estrellas, ${request.tiempoSegundos}s")

        return FinalizarPuzzleResponse(
            estrellas = estrellas,
            tiempoTotal = request.tiempoSegundos,
            movimientosTotal = request.movimientos,
            hintsUsados = request.hintsUsados,
            mensaje = generarMensajeEstrellas(estrellas),
            siguienteImagenDesbloqueada = siguienteImagen?.let {
                ImagenPuzzleDTO.fromEntity(it, true)
            },
            progresoActual = ProgresoJugadorDTO(
                jugadorId = progreso.jugadorId,
                estrellasTotal = progreso.estrellasTotal,
                puzzlesCompletados = progreso.puzzlesCompletados,
                mejorTiempo = progreso.mejorTiempo,
                imagenesDesbloqueadas = progreso.imagenesDesbloqueadas.size + 1
            )
        )
    }

    /**
     * Calcula las estrellas obtenidas según rendimiento
     */
    private fun calcularEstrellas(gridSize: Int, tiempoSegundos: Int, hintsUsados: Int): Int {
        val tiempoIdeal = TIEMPOS_IDEALES[gridSize] ?: 120

        var estrellas = 3

        // Criterio 1: Tiempo
        if (tiempoSegundos > tiempoIdeal * 1.5) {
            estrellas = 2  // Pasó 50% del tiempo ideal
        } else if (tiempoSegundos > tiempoIdeal * 2) {
            estrellas = 1  // Pasó el doble del tiempo ideal
        }

        // Criterio 2: Hints (reduce 1 estrella si usó hints)
        if (hintsUsados > 0 && estrellas > 1) {
            estrellas--
        }

        return kotlin.math.max(1, estrellas)
    }

    /**
     * Desbloquea la siguiente imagen en secuencia
     */
    private fun desbloquearSiguienteImagen(
        progreso: ProgresoPuzzle,
        imagenActual: ImagenPuzzle
    ): ImagenPuzzle? {
        val siguienteOrden = imagenActual.ordenDesbloqueo + 1
        val siguiente = imagenPuzzleRepository.findByOrdenDesbloqueo(siguienteOrden)

        if (siguiente != null && siguiente.id!! !in progreso.imagenesDesbloqueadas) {
            progreso.imagenesDesbloqueadas.add(siguiente.id!!)
            println("🔓 Imagen desbloqueada: ${siguiente.titulo}")
            return siguiente
        }

        return null
    }

    /**
     * Obtiene o crea el progreso del jugador
     */
    private fun obtenerOCrearProgreso(jugadorId: String): ProgresoPuzzle {
        return progresoPuzzleRepository.findByJugadorId(jugadorId)
            ?: progresoPuzzleRepository.save(
                ProgresoPuzzle(jugadorId = jugadorId)
            )
    }

    /**
     * Genera mensaje según estrellas obtenidas
     */
    private fun generarMensajeEstrellas(estrellas: Int): String {
        return when (estrellas) {
            3 -> "¡Increíble! ⭐⭐⭐ ¡Eres un maestro del puzzle!"
            2 -> "¡Muy bien! ⭐⭐ Buen trabajo completando el puzzle"
            1 -> "¡Completado! ⭐ Sigue practicando para mejorar"
            else -> "Puzzle completado"
        }
    }
}