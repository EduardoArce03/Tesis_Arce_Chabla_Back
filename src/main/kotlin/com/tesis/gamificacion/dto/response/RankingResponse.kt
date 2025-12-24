package com.tesis.gamificacion.dto.response

import com.tesis.gamificacion.model.enums.CategoriasCultural
import com.tesis.gamificacion.model.enums.NivelDificultad
import java.time.LocalDateTime

data class RankingResponse(
    val posicion: Int,
    val jugadorId: String,
    val puntuacion: Int,
    val nivel: NivelDificultad,
    val categoria: CategoriasCultural,
    val tiempoSegundos: Int,
    val fecha: LocalDateTime
)