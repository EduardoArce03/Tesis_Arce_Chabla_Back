package com.tesis.gamificacion.model.data

import com.tesis.gamificacion.model.enums.CapaNivel
import com.tesis.gamificacion.model.enums.PuntoInteres

object NarrativasConfig {

    private val narrativas = mapOf(
        // TEMPLO DEL SOL
        Pair(PuntoInteres.TEMPLO_SOL, CapaNivel.ACTUAL) to Narrativa(
            titulo = "El Templo del Sol Hoy",
            texto = """
                Ante ti se alza el Templo del Sol, la estructura más emblemática de Ingapirca.
                Sus piedras perfectamente talladas resistieron 500 años desde la conquista Inca.
                Los Incas construyeron este templo sobre los cimientos Cañaris, fusionando dos culturas.
            """.trimIndent(),
            nombreEspiritu = "Guardián de Ingapirca",
            personalidad = "Amigable y didáctico"
        ),

        Pair(PuntoInteres.TEMPLO_SOL, CapaNivel.CANARI) to Narrativa(
            titulo = "Los Orígenes Cañaris",
            texto = """
                Viaja 600 años atrás. Aquí, los Cañaris erigieron un centro ceremonial
                dedicado a la Luna (Quilla). El lugar era conocido como "Hatun Cañar" - 
                Gran Lugar de los Cañaris. Este fue un espacio sagrado mucho antes de los Incas.
            """.trimIndent(),
            nombreEspiritu = "Amawta Cañari",
            personalidad = "Sabio y ancestral"
        ),

        // PLAZA CEREMONIAL
        Pair(PuntoInteres.PLAZA_CEREMONIAL, CapaNivel.ACTUAL) to Narrativa(
            titulo = "La Plaza Hoy",
            texto = """
                Esta plaza fue el corazón social de Ingapirca. Aquí se realizaban ceremonias,
                mercados y reuniones. Las excavaciones arqueológicas han revelado canales
                de agua y estructuras de almacenamiento que muestran la complejidad del sitio.
            """.trimIndent(),
            nombreEspiritu = "Guardián de Ingapirca",
            personalidad = "Amigable y didáctico"
        ),

        Pair(PuntoInteres.PLAZA_CEREMONIAL, CapaNivel.CANARI) to Narrativa(
            titulo = "Centro Ceremonial Cañari",
            texto = """
                Los Cañaris usaban esta plaza para celebraciones del Inti Raymi y ceremonias
                lunares. Danzas rituales honraban a la Pachamama. Los shamanes realizaban
                ofrendas de chicha y maíz para asegurar buenas cosechas.
            """.trimIndent(),
            nombreEspiritu = "Amawta Cañari",
            personalidad = "Sabio y ancestral"
        ),

        // OBSERVATORIO
        Pair(PuntoInteres.OBSERVATORIO, CapaNivel.ACTUAL) to Narrativa(
            titulo = "Observatorio Astronómico",
            texto = """
                Este punto estratégico se alinea con el solsticio de junio. Los Incas
                eran astrónomos avanzados y usaban estas estructuras para marcar
                fechas importantes del calendario agrícola y ceremonial.
            """.trimIndent(),
            nombreEspiritu = "Guardián de Ingapirca",
            personalidad = "Amigable y didáctico"
        ),

        Pair(PuntoInteres.OBSERVATORIO, CapaNivel.CANARI) to Narrativa(
            titulo = "Observación Cañari",
            texto = """
                Los Cañaris ya observaban los astros siglos antes de los Incas.
                Conocían los ciclos lunares y solares. Este conocimiento guiaba
                sus cultivos de maíz, quinua y papas en las terrazas andinas.
            """.trimIndent(),
            nombreEspiritu = "Amawta Cañari",
            personalidad = "Sabio y ancestral"
        )
    )

    fun obtener(punto: PuntoInteres, capa: CapaNivel): Narrativa {
        return narrativas[Pair(punto, capa)]
            ?: throw IllegalArgumentException("Narrativa no encontrada")
    }
}

data class Narrativa(
    val titulo: String,
    val texto: String,
    val nombreEspiritu: String,
    val personalidad: String
)