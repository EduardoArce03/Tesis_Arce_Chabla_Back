package com.tesis.gamificacion.model.responses

import com.tesis.gamificacion.model.entities.ImagenPuzzle

data class ImagenPuzzleDTO(
    val id: Long,
    val titulo: String,
    val nombreKichwa: String,
    val categoria: String,
    val imagenUrl: String,
    val desbloqueada: Boolean = true,
    val ordenDesbloqueo: Int,
    val dificultadMinima: Int,
    val dificultadMaxima: Int
) {
    companion object {
        fun fromEntity(imagen: ImagenPuzzle, desbloqueada: Boolean): ImagenPuzzleDTO {
            return ImagenPuzzleDTO(
                id = imagen.id!!,
                titulo = imagen.titulo,
                nombreKichwa = imagen.nombreKichwa,
                categoria = imagen.categoria.name,
                imagenUrl = imagen.imagenUrl,  // ⬅️ Directamente desde la entidad
                desbloqueada = desbloqueada,
                ordenDesbloqueo = imagen.ordenDesbloqueo,
                dificultadMinima = imagen.dificultadMinima,
                dificultadMaxima = imagen.dificultadMaxima
            )
        }
    }
}

data class IniciarPuzzleResponse(
    val partidaId: Long,
    val mensajeBienvenida: String,
    val tiempoLimiteSegundos: Int, // ⬅️ NUEVO: Enviar tiempo límite al frontend
    val gridSize: Int
)

data class FinalizarPuzzleResponse(
    val estrellas: Int,
    val mensaje: String,
    val puntosObtenidos: Int,
    val tiempoFinal: Int, // Tiempo que usó (límite - restante)
    val siguienteImagenDesbloqueada: ImagenPuzzleDTO?,
    val progresoActual: ProgresoJugadorDTO
)

data class ProgresoJugadorDTO(
    val jugadorId: String,
    val estrellasTotal: Int,
    val puntosTotal: Int,
    val puzzlesCompletados: Int,
    val mejorTiempo: Int,
    val imagenesDesbloqueadas: Int
)
