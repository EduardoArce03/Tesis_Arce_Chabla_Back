package com.tesis.gamificacion.dto

import com.tesis.gamificacion.model.enums.Severity

data class ErrorDTO (
    val title: String,
    val message: String,
    val severity: Severity
){
}