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
import java.time.LocalDateTime
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
    /**
     * Obtiene el progreso del jugador
     */
    fun obtenerProgreso(jugadorId: String): ProgresoJugadorDTO {
        val progreso = progresoPuzzleRepository.findByJugadorId(jugadorId)
            ?: return ProgresoJugadorDTO(
                jugadorId = jugadorId,
                estrellasTotal = 0,
                puntosTotal = 0, // ⬅️ AGREGADO
                puzzlesCompletados = 0,
                mejorTiempo = 0,
                imagenesDesbloqueadas = 1 // La primera siempre desbloqueada
            )

        return ProgresoJugadorDTO(
            jugadorId = progreso.jugadorId,
            estrellasTotal = progreso.estrellasTotal,
            puntosTotal = progreso.puntosTotal, // ⬅️ AGREGADO
            puzzlesCompletados = progreso.puzzlesCompletados,
            mejorTiempo = if (progreso.mejorTiempo == Int.MAX_VALUE) 0 else progreso.mejorTiempo,
            imagenesDesbloqueadas = progreso.imagenesDesbloqueadas.size + 1 // +1 por la primera
        )
    }

    @Transactional
    fun iniciarPuzzle(request: IniciarPuzzleRequest): IniciarPuzzleResponse {
        val imagen = imagenPuzzleRepository.findById(request.imagenId)
            .orElseThrow { IllegalArgumentException("Imagen no encontrada") }

        if (!imagen.desbloqueada) {
            throw IllegalStateException("Esta imagen está bloqueada")
        }

        // ⬇️ CALCULAR TIEMPO LÍMITE SEGÚN DIFICULTAD
        val tiempoLimite = calcularTiempoLimite(request.gridSize)

        val partida = PartidaPuzzle(
            jugadorId = request.jugadorId,
            imagen = imagen,
            gridSize = request.gridSize,
            tiempoLimiteSegundos = tiempoLimite // ⬅️ NUEVO
        )

        val partidaGuardada = partidaPuzzleRepository.save(partida)

        println("🎮 Partida iniciada: ID=${partidaGuardada.id}, Tiempo límite: ${tiempoLimite}s")

        return IniciarPuzzleResponse(
            partidaId = partidaGuardada.id!!,
            mensajeBienvenida = "¡Descubre ${imagen.titulo}! Tienes ${formatearTiempo(tiempoLimite)}",
            tiempoLimiteSegundos = tiempoLimite, // ⬅️ NUEVO
            gridSize = request.gridSize
        )
    }

    // ⬇️ NUEVA FUNCIÓN: Calcular tiempo según dificultad
    private fun calcularTiempoLimite(gridSize: Int): Int {
        return when (gridSize) {
            3 -> 300  // 3x3 = 5 minutos
            4 -> 480  // 4x4 = 8 minutos
            5 -> 600  // 5x5 = 10 minutos
            6 -> 720  // 6x6 = 12 minutos
            else -> 480
        }
    }

    // ⬇️ NUEVA FUNCIÓN: Formatear tiempo para mensaje
    private fun formatearTiempo(segundos: Int): String {
        val minutos = segundos / 60
        val segs = segundos % 60
        return if (segs > 0) {
            "${minutos}m ${segs}s"
        } else {
            "${minutos} minutos"
        }
    }

    @Transactional
    fun finalizarPuzzle(request: FinalizarPuzzleRequest): FinalizarPuzzleResponse {
        val partida = partidaPuzzleRepository.findById(request.partidaId)
            .orElseThrow { IllegalArgumentException("Partida no encontrada") }

        if (partida.completada) {
            throw IllegalStateException("Esta partida ya fue completada")
        }

        // Calcular estrellas y puntos
        val estrellas = calcularEstrellas(
            tiempoRestante = request.tiempoRestante,
            tiempoLimite = partida.tiempoLimiteSegundos,
            movimientos = request.movimientos,
            gridSize = partida.gridSize
        )

        val puntos = calcularPuntos(
            estrellas = estrellas,
            tiempoRestante = request.tiempoRestante,
            movimientos = request.movimientos,
            gridSize = partida.gridSize
        )

        // Actualizar partida
        partida.completada = true
        partida.movimientos = request.movimientos
        partida.tiempoRestanteSegundos = request.tiempoRestante
        partida.hintsUsados = request.hintsUsados
        partida.estrellas = estrellas
        partida.puntosObtenidos = puntos
        partida.fechaFin = LocalDateTime.now()

        partidaPuzzleRepository.save(partida)

        println("✅ Puzzle completado: ${partida.imagen.titulo} | Estrellas: $estrellas | Puntos: $puntos")

        // Obtener o crear progreso
        val progreso = obtenerOCrearProgreso(partida.jugadorId)

        // Actualizar estadísticas
        progreso.estrellasTotal += estrellas
        progreso.puntosTotal += puntos
        progreso.puzzlesCompletados += 1

        // Actualizar mejor tiempo (ahora es tiempo RESTANTE, mayor es mejor)
        val tiempoUsado = calcularTiempoUsado(partida.tiempoLimiteSegundos, request.tiempoRestante)
        if (progreso.mejorTiempo == Int.MAX_VALUE || tiempoUsado < progreso.mejorTiempo) {
            progreso.mejorTiempo = tiempoUsado
        }

        // Desbloquear siguiente imagen si aplica
        val siguienteImagen = desbloquearSiguienteImagen(progreso, partida.imagen)

        // Guardar progreso
        progresoPuzzleRepository.save(progreso)

        // Generar mensaje
        val mensaje = when (estrellas) {
            3 -> "¡Excelente! Completaste el puzzle con ${formatearTiempo(request.tiempoRestante)} restantes ⭐⭐⭐"
            2 -> "¡Muy bien! Buen tiempo y eficiencia ⭐⭐"
            1 -> "¡Completado! Pudiste terminarlo a tiempo ⭐"
            else -> "Puzzle completado"
        }

        println("📊 Progreso actualizado: ${progreso.estrellasTotal} estrellas | ${progreso.puzzlesCompletados} puzzles")

        return FinalizarPuzzleResponse(
            estrellas = estrellas,
            mensaje = mensaje,
            puntosObtenidos = puntos,
            tiempoFinal = tiempoUsado,
            siguienteImagenDesbloqueada = siguienteImagen?.let {
                ImagenPuzzleDTO(
                    id = it.id!!,
                    titulo = it.titulo,
                    nombreKichwa = it.nombreKichwa,
                    imagenUrl = it.imagenUrl,
                    categoria = it.categoria.name, // ⬅️ AGREGADO .name
                    dificultadMinima = it.dificultadMinima,
                    dificultadMaxima = it.dificultadMaxima,
                    ordenDesbloqueo = it.ordenDesbloqueo,
                    desbloqueada = true // Ahora está desbloqueada
                )
            },
            progresoActual = ProgresoJugadorDTO(
                jugadorId = progreso.jugadorId,
                estrellasTotal = progreso.estrellasTotal,
                puntosTotal = progreso.puntosTotal,
                puzzlesCompletados = progreso.puzzlesCompletados,
                mejorTiempo = if (progreso.mejorTiempo == Int.MAX_VALUE) 0 else progreso.mejorTiempo,
                imagenesDesbloqueadas = progreso.imagenesDesbloqueadas.size + 1
            )
        )
    }

    // ⬇️ NUEVA FUNCIÓN: Calcular tiempo usado (para estadísticas)
    private fun calcularTiempoUsado(tiempoLimite: Int, tiempoRestante: Int): Int {
        return tiempoLimite - tiempoRestante
    }

    // ⬇️ NUEVA LÓGICA: Calcular estrellas por tiempo RESTANTE
    private fun calcularEstrellas(
        tiempoRestante: Int,
        tiempoLimite: Int,
        movimientos: Int,
        gridSize: Int
    ): Int {
        val porcentajeTiempoRestante = (tiempoRestante.toDouble() / tiempoLimite) * 100
        val movimientosOptimos = gridSize * gridSize * 3

        return when {
            // ⭐⭐⭐ 3 estrellas: Más del 50% tiempo restante Y pocos movimientos
            porcentajeTiempoRestante >= 50 && movimientos < movimientosOptimos -> 3

            // ⭐⭐ 2 estrellas: Más del 25% tiempo restante Y movimientos moderados
            porcentajeTiempoRestante >= 25 && movimientos < movimientosOptimos * 1.5 -> 2

            // ⭐ 1 estrella: Completó antes de que se acabe el tiempo
            else -> 1
        }
    }

    // ⬇️ NUEVA LÓGICA: Calcular puntos por tiempo RESTANTE
    private fun calcularPuntos(
        estrellas: Int,
        tiempoRestante: Int,
        movimientos: Int,
        gridSize: Int
    ): Int {
        var puntos = 1000 // Base

        // Bonus por estrellas
        puntos += estrellas * 500

        // Bonus por tiempo restante
        puntos += when {
            tiempoRestante >= 300 -> 500 // Más de 5 min restantes
            tiempoRestante >= 180 -> 300 // Más de 3 min restantes
            tiempoRestante >= 60 -> 100  // Más de 1 min restante
            else -> 0
        }

        // Bonus por eficiencia en movimientos
        val movimientosOptimos = gridSize * gridSize * 3
        puntos += when {
            movimientos < movimientosOptimos -> 400
            movimientos < movimientosOptimos * 1.3 -> 200
            movimientos < movimientosOptimos * 1.6 -> 100
            else -> 0
        }

        return puntos
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