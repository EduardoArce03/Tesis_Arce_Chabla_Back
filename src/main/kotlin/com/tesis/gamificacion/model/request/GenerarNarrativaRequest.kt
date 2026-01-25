package com.tesis.gamificacion.model.request

import com.tesis.gamificacion.model.enums.NivelCapa
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

data class GenerarNarrativaRequest(
    val jugadorId: String,
    val puntoInteresId: Long,
    val nivel: String // "SUPERFICIE", "INCA", "CANARI", "ANCESTRAL"
)

data class DialogarEspirituRequest(
    val jugadorId: String,
    val capaId: Long,
    val pregunta: String,
    val partidaId: Long,
    val nivelCapa: NivelCapa,
    val puntoInteresId: Long
)


data class AnalizarFotoRequest(
    val jugadorId: String,
    val objetivoId: Long,
    val imagenBase64: String, // Imagen en base64
    val descripcionUsuario: String? = null // Opcional: lo que el usuario cree que encontró
)

data class CompletarMisionRequest(
    val jugadorId: String,
    val misionId: Long,
    val respuestas: Map<String, Any>,
    val partidaId: Long,
)

data class IniciarExploracionRequest(
    val jugadorId: String
)

// En ExploracionDTOs.kt o similar

data class MarcarObjetivoManualRequest(
    @field:NotNull @field:Positive
    val partidaId: Long,

    @field:NotNull @field:Positive
    val objetivoId: Long
)

data class MarcarObjetivoManualResponse(
    val exito: Boolean,
    val mensaje: String,
    val recompensas: List<RecompensaDTO>
)