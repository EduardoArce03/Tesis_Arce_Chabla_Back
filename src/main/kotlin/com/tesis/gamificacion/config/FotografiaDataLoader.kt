package com.tesis.gamificacion.config

import com.tesis.gamificacion.model.entities.CapaTemporal
import com.tesis.gamificacion.model.entities.FotografiaObjetivo
import com.tesis.gamificacion.model.entities.PuntoInteres
import com.tesis.gamificacion.model.enums.NivelCapa
import com.tesis.gamificacion.model.enums.RarezaFoto
import com.tesis.gamificacion.repository.CapaTemporalRepository
import com.tesis.gamificacion.repository.FotografiaObjetivoRepository
import com.tesis.gamificacion.repository.PuntoInteresRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class InicializadorCompleto(
    private val capaTemporalRepository: CapaTemporalRepository,
    private val fotografiaObjetivoRepository: FotografiaObjetivoRepository,
    private val puntoInteresRepository: PuntoInteresRepository
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (capaTemporalRepository.count() == 0L) {
            println("🎮 Inicializando estructura completa del juego...")

            val puntos = puntoInteresRepository.findByActivoTrue()
            var capasCreadas = 0
            var objetivosCreados = 0

            puntos.forEach { punto ->
                println("   📍 Procesando: ${punto.nombre}")

                NivelCapa.entries.forEach { nivel ->
                    // 1. Crear capa temporal
                    val capa = capaTemporalRepository.save(
                        CapaTemporal(
                            puntoInteres = punto,
                            nivel = nivel,
                            narrativaBase = generarNarrativaBase(punto, nivel),
                            promptNarrativa = generarPromptNarrativa(punto, nivel),
                            nombreEspiritu = generarNombreEspiritu(nivel),
                            nombreEspirituKichwa = generarNombreEspirituKichwa(nivel),
                            epocaEspiritu = generarEpocaEspiritu(nivel),
                            personalidadEspiritu = generarPersonalidadEspiritu(nivel),
                            promptEspiritu = generarPromptEspiritu(punto, nivel),
                            avatarEspiritu = null
                        )
                    )
                    capasCreadas++

                    // 2. Crear objetivos fotográficos para esta capa
                    val objetivos = crearObjetivosParaCapa(capa, punto, nivel)
                    fotografiaObjetivoRepository.saveAll(objetivos)
                    objetivosCreados += objetivos.size
                }
            }

            println("✅ Inicialización completada:")
            println("   - $capasCreadas capas temporales creadas")
            println("   - $objetivosCreados objetivos fotográficos creados")
        }
    }

    private fun crearObjetivosParaCapa(
        capa: CapaTemporal,
        punto: PuntoInteres,
        nivel: NivelCapa
    ): List<FotografiaObjetivo> {
        return when (nivel) {
            NivelCapa.SUPERFICIE -> listOf(
                FotografiaObjetivo(
                    capaTemporal = capa,
                    puntoInteres = punto,
                    nivelRequerido = nivel,
                    descripcion = "Vista general de ${punto.nombre}",
                    rareza = RarezaFoto.COMUN,
                    criteriosValidacion = "Captura completa del sitio",
                    esBonus = false,
                    puntosRecompensa = 15,
                    activo = true
                ),
                FotografiaObjetivo(
                    capaTemporal = capa,
                    puntoInteres = punto,
                    nivelRequerido = nivel,
                    descripcion = "Detalle arquitectónico de ${punto.nombre}",
                    rareza = RarezaFoto.POCO_COMUN,
                    criteriosValidacion = "Enfoque en detalles constructivos",
                    esBonus = false,
                    puntosRecompensa = 30,
                    activo = true
                )
            )

            NivelCapa.INCA -> listOf(
                FotografiaObjetivo(
                    capaTemporal = capa,
                    puntoInteres = punto,
                    nivelRequerido = nivel,
                    descripcion = "Construcción inca en ${punto.nombre}",
                    rareza = RarezaFoto.RARA,
                    criteriosValidacion = "Técnicas incas visibles",
                    esBonus = false,
                    puntosRecompensa = 60,
                    activo = true
                )
            )

            NivelCapa.CANARI -> listOf(
                FotografiaObjetivo(
                    capaTemporal = capa,
                    puntoInteres = punto,
                    nivelRequerido = nivel,
                    descripcion = "Elementos Cañari en ${punto}",
                    rareza = RarezaFoto.EPICA,
                    criteriosValidacion = "Estructuras pre-incas",
                    esBonus = false,
                    puntosRecompensa = 120,
                    activo = true
                )
            )

            NivelCapa.ANCESTRAL -> listOf(
                FotografiaObjetivo(
                    capaTemporal = capa,
                    puntoInteres = punto,
                    nivelRequerido = nivel,
                    descripcion = "Esencia ancestral de ${punto}",
                    rareza = RarezaFoto.LEGENDARIA,
                    criteriosValidacion = "Captura única y excepcional",
                    esBonus = true,
                    puntosRecompensa = 250,
                    activo = true
                )
            )
        }
    }

    private fun generarNarrativaBase(punto: PuntoInteres, nivel: NivelCapa): String {
        return when (nivel) {
            NivelCapa.SUPERFICIE -> "En la actualidad, ${punto.nombre} se presenta como un testimonio visible de la grandeza de Ingapirca. ${punto.descripcion}"
            NivelCapa.INCA -> "Durante el Imperio Inca (1470-1532), ${punto.nombre} era un espacio fundamental en el complejo ceremonial."
            NivelCapa.CANARI -> "Antes de la llegada de los Incas, el pueblo Cañari había establecido ${punto.nombre} como parte de su cosmovisión ancestral."
            NivelCapa.ANCESTRAL -> "En tiempos ancestrales, ${punto.nombre} guardaba los secretos más profundos de la espiritualidad andina."
        }
    }

    private fun generarPromptNarrativa(punto: PuntoInteres, nivel: NivelCapa): String {
        return "Genera una narrativa educativa sobre ${punto.nombre} en el contexto de ${nivel.nombre}. " +
                "Incluye detalles históricos, culturales y su significado para la comunidad Cañari."
    }

    private fun generarNombreEspiritu(nivel: NivelCapa): String {
        return when (nivel) {
            NivelCapa.SUPERFICIE -> "Guardián del Presente"
            NivelCapa.INCA -> "Willka Kamayuq"
            NivelCapa.CANARI -> "Tayta Cañari"
            NivelCapa.ANCESTRAL -> "Espíritu Ancestral"
        }
    }

    private fun generarNombreEspirituKichwa(nivel: NivelCapa): String {
        return when (nivel) {
            NivelCapa.SUPERFICIE -> "Kunan Kuidaq"
            NivelCapa.INCA -> "Willka Kamayuq"
            NivelCapa.CANARI -> "Tayta Kañari"
            NivelCapa.ANCESTRAL -> "Ñawpa Yaya"
        }
    }

    private fun generarEpocaEspiritu(nivel: NivelCapa): String {
        return when (nivel) {
            NivelCapa.SUPERFICIE -> "Presente - 2025"
            NivelCapa.INCA -> "1470-1532 - Imperio Inca"
            NivelCapa.CANARI -> "500-1470 - Cultura Cañari"
            NivelCapa.ANCESTRAL -> "Tiempos Ancestrales"
        }
    }

    private fun generarPersonalidadEspiritu(nivel: NivelCapa): String {
        return when (nivel) {
            NivelCapa.SUPERFICIE -> "Educador y guía moderno"
            NivelCapa.INCA -> "Sabio y ceremonioso"
            NivelCapa.CANARI -> "Místico y protector"
            NivelCapa.ANCESTRAL -> "Enigmático y profundo"
        }
    }

    private fun generarPromptEspiritu(punto: PuntoInteres, nivel: NivelCapa): String {
        return "Eres el espíritu ancestral de ${nivel.nombre} en ${punto.nombre}. " +
                "Respondes preguntas desde la perspectiva de esta época, compartiendo conocimientos " +
                "sobre la cultura, costumbres y significado espiritual del lugar. " +
                "Usa un tono ${generarPersonalidadEspiritu(nivel).lowercase()} y menciona detalles históricos auténticos."
    }
}