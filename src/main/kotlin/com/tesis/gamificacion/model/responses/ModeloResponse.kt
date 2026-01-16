package com.tesis.gamificacion.model.responses

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class ModeloResponse (
    val status: String = "",

    @JsonProperty("descripcion")
    val descripcion: String = "",

    @JsonProperty("audio_64")
    val audio64: String? = null,

    val cultura: String? = "Cañari"
)

// NUEVAS respuestas para los diferentes tipos de narrativa
data class NarrativaEducativaResponse(
    val titulo: String,
    val descripcion: String,
    val nombreKichwa: String,
    val nombreEspanol: String,
    val preguntaRecuperacion: PreguntaRecuperacionResponse?
)

data class PreguntaRecuperacionResponse(
    val pregunta: String,
    val opciones: List<String>,
    val respuestaCorrecta: Int,
    val explicacion: String
)

data class DialogoCulturalResponse(
    val textoKichwa: String,
    val textoEspanol: String,
    val tipo: String
)

data class HintResponse(
    val mensaje: String,
    val tipoHint: String,
    val costoPuntos: Int,
    val usosRestantes: Int
)

data class DatoCuriosoResponse(
    val titulo: String,
    val descripcion: String
)

data class PreguntaRapidaResponse(
    val pregunta: String,
    val opciones: List<String>,
    val respuestaCorrecta: String
)