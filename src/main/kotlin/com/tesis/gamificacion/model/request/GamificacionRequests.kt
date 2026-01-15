package com.tesis.gamificacion.model.request

import com.tesis.gamificacion.model.enums.TipoHint

data class ProcesarErrorRequest(
    val partidaId: Long,
    val elementoId: Long
)

data class ProcesarParejaRequest(
    val partidaId: Long,
    val elementoId: Long
)

data class SolicitarHintRequest(
    val partidaId: Long,
    val tipoHint: TipoHint = TipoHint.DESCRIPCION_CONTEXTUAL
)

data class ResponderPreguntaRequest(
    val partidaId: Long,
    val elementoId: Long,
    val respuestaSeleccionada: Int
)