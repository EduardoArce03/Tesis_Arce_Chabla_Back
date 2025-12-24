package com.tesis.gamificacion.dto.response

import com.tesis.gamificacion.model.enums.CategoriasCultural
import com.tesis.gamificacion.model.enums.NivelDificultad
import java.time.LocalDateTime

data class PartidaResponse(
    val id: Long,
    val jugadorId: String,
    val nivel: NivelDificultad,
    val categoria: CategoriasCultural,
    val intentos: Int,
    val tiempoSegundos: Int,
    val puntuacion: Int,
    val completada: Boolean,
    val fechaInicio: LocalDateTime,
    val fechaFin: LocalDateTime?
)