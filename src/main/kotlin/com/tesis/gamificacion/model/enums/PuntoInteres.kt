package com.tesis.gamificacion.model.enums

enum class PuntoInteres(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val coordenadaX: Double,
    val coordenadaY: Double,
    val imagenUrl: String
) {
    TEMPLO_SOL(
        id = 1,
        nombre = "Templo del Sol",
        descripcion = "El edificio más emblemático de Ingapirca",
        coordenadaX = -2.5466,
        coordenadaY = -78.8766,
        imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/templo.jpg"
    ),

    PLAZA_CEREMONIAL(
        id = 2,
        nombre = "Plaza Ceremonial",
        descripcion = "Centro de actividades rituales",
        coordenadaX = -2.5470,
        coordenadaY = -78.8770,
        imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/plaza.jpg"
    ),

    OBSERVATORIO(
        id = 3,
        nombre = "Observatorio Astronómico",
        descripcion = "Usado para observar solsticios",
        coordenadaX = -2.5468,
        coordenadaY = -78.8768,
        imagenUrl = "https://tesis-edu.s3.us-east-1.amazonaws.com/observatorio.png"
    );

    companion object {
        fun fromId(id: Int) = entries.find { it.id == id }
    }
}