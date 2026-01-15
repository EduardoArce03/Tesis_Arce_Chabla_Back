package com.tesis.gamificacion.model.responses

import com.tesis.gamificacion.model.entities.EstadoVidasDTO
import com.tesis.gamificacion.model.entities.EstadoComboDTO
import com.tesis.gamificacion.model.entities.HintDisponibleDTO

data class EstadoPartidaResponse(
    val vidas: EstadoVidasDTO,
    val combo: EstadoComboDTO,
    val hints: HintDisponibleDTO
)

data class ProcesarErrorResponse(
    val vidasRestantes: Int,
    val comboRoto: Boolean,
    val narrativa: NarrativaEducativaResponse,
    val estadoActualizado: EstadoPartidaResponse,
    val mostrarPregunta: Boolean  // ⬅️ NUEVO
)

data class ProcesarParejaResponse(
    val comboActual: Int,
    val multiplicador: Double,
    val dialogo: DialogoCulturalResponse?,
    val esPrimerDescubrimiento: Boolean,
    val estadoActualizado: EstadoPartidaResponse
)

data class SolicitarHintResponse(
    val mensaje: String,
    val costoPuntos: Int,
    val usosRestantes: Int,
    val estadoActualizado: EstadoPartidaResponse
)

data class ResponderPreguntaResponse(
    val esCorrecta: Boolean,
    val vidaRecuperada: Boolean,
    val vidasActuales: Int,
    val explicacion: String,
    val estadoActualizado: EstadoPartidaResponse
)

data class FinalizarPartidaResponse(
    val puntuacion: Int,
    val insignias: List<InsigniaDTO>,
    val estadisticas: EstadisticasDetalladasDTO
)

data class InsigniaDTO(
    val nombre: String,
    val nombreKichwa: String,
    val icono: String,
    val descripcion: String
)

data class EstadisticasDetalladasDTO(
    val precision: Double,
    val mejorCombo: Int,
    val vidasRestantes: Int,
    val hintsUsados: Int,
    val tiempoTotal: Int,
    val nuevosDescubrimientos: Int
)

// Extender IniciarPartidaResponse existente
data class IniciarPartidaResponse(
    val partidaId: Long,
    val elementos: List<Any>, // Tu ElementoCulturalDTO
    val estadoInicial: EstadoPartidaResponse
)