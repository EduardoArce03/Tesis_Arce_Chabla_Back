package com.tesis.gamificacion.model.data

import com.tesis.gamificacion.model.enums.CapaNivel
import com.tesis.gamificacion.model.enums.PuntoInteres

object FotografiasConfig {

    private val objetivos = mapOf(
        // TEMPLO DEL SOL - ACTUAL
        Pair(PuntoInteres.TEMPLO_SOL, CapaNivel.ACTUAL) to listOf(
            ObjetivoFoto(
                id = 101,
                descripcion = "Captura la fachada principal del Templo del Sol",
                criterio = "Debe verse la estructura elíptica completa"
            ),
            ObjetivoFoto(
                id = 102,
                descripcion = "Fotografía las piedras talladas con precisión inca",
                criterio = "Enfoque en las junturas sin argamasa"
            )
        ),

        // TEMPLO DEL SOL - CANARI
        Pair(PuntoInteres.TEMPLO_SOL, CapaNivel.CANARI) to listOf(
            ObjetivoFoto(
                id = 103,
                descripcion = "Captura los cimientos Cañaris bajo la estructura Inca",
                criterio = "Debe verse la diferencia en el tallado de piedras"
            ),
            ObjetivoFoto(
                id = 104,
                descripcion = "Fotografía las terrazas agrícolas circundantes",
                criterio = "Vista panorámica del sistema de terrazas"
            )
        ),

        // PLAZA CEREMONIAL - ACTUAL
        Pair(PuntoInteres.PLAZA_CEREMONIAL, CapaNivel.ACTUAL) to listOf(
            ObjetivoFoto(
                id = 201,
                descripcion = "Captura la vista general de la plaza",
                criterio = "Debe abarcar todo el espacio central"
            ),
            ObjetivoFoto(
                id = 202,
                descripcion = "Fotografía los canales de agua",
                criterio = "Enfoque en el sistema hidráulico"
            )
        ),

        // PLAZA CEREMONIAL - CANARI
        Pair(PuntoInteres.PLAZA_CEREMONIAL, CapaNivel.CANARI) to listOf(
            ObjetivoFoto(
                id = 203,
                descripcion = "Captura las estructuras ceremoniales originales",
                criterio = "Debe verse la arquitectura pre-Inca"
            ),
            ObjetivoFoto(
                id = 204,
                descripcion = "Fotografía las ofrendas ceremoniales (recreación)",
                criterio = "Enfoque en el altar ceremonial"
            )
        ),

        // OBSERVATORIO - ACTUAL
        Pair(PuntoInteres.OBSERVATORIO, CapaNivel.ACTUAL) to listOf(
            ObjetivoFoto(
                id = 301,
                descripcion = "Captura la alineación solar durante el solsticio",
                criterio = "Debe verse la sombra alineada"
            ),
            ObjetivoFoto(
                id = 302,
                descripcion = "Fotografía las marcas astronómicas en las piedras",
                criterio = "Enfoque en los símbolos tallados"
            )
        ),

        // OBSERVATORIO - CANARI
        Pair(PuntoInteres.OBSERVATORIO, CapaNivel.CANARI) to listOf(
            ObjetivoFoto(
                id = 303,
                descripcion = "Captura las orientaciones lunares originales",
                criterio = "Debe verse la orientación hacia la Luna"
            ),
            ObjetivoFoto(
                id = 304,
                descripcion = "Fotografía los petroglifos astronómicos Cañaris",
                criterio = "Enfoque en símbolos lunares y estelares"
            )
        )
    )

    fun obtenerObjetivos(punto: PuntoInteres, capa: CapaNivel): List<ObjetivoFoto> {
        return objetivos[Pair(punto, capa)] ?: emptyList()
    }
}

data class ObjetivoFoto(
    val id: Int,
    val descripcion: String,
    val criterio: String
)