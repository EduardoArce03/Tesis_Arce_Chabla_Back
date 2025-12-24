package com.tesis.gamificacion.dto.response

data class IniciarPartidaResponse(
    val partidaId: Long,
    val elementos: List<ElementoCulturalResponse>
)