package com.tesis.gamificacion.exception

import com.tesis.gamificacion.model.enums.Severity


class UPSGamificacionCustomException (
    val title: String,
    override val message: String,
    val severity: Severity
): RuntimeException() {}