package com.tesis.gamificacion.model.enums

enum class RarezaFoto(val puntos: Int, val probabilidad: Double) {
    COMUN(25, 0.70),
    RARO(75, 0.25),
    LEGENDARIO(200, 0.05),
    POCO_COMUN(150, 0.05),
    RARA(250, 0.05),
    EPICA(250, 0.05),
    LEGENDARIA(250, 0.05),
}
