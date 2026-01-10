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