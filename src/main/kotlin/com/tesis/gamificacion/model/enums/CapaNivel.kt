package com.tesis.gamificacion.model.enums

enum class CapaNivel(
    val orden: Int,
    val nombre: String,
    val descripcion: String
) {
    ACTUAL(
        orden = 1,
        nombre = "Ingapirca Actual",
        descripcion = "Las ruinas como las conocemos hoy"
    ),

    CANARI(
        orden = 2,
        nombre = "Era Cañari",
        descripcion = "El sitio durante la civilización Cañari (500-1450 d.C.)"
    );

    fun esDesbloqueadaInicial() = this == ACTUAL
}