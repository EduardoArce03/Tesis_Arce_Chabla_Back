package com.tesis.gamificacion.config

import com.tesis.gamificacion.model.entities.*
import com.tesis.gamificacion.model.enums.*
import com.tesis.gamificacion.repository.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MisionDataLoader {
    private val logger = LoggerFactory.getLogger(MisionDataLoader::class.java)
    private val objectMapper = jacksonObjectMapper()

    @Bean
    fun cargarDatosMisiones(
        misionRepository: MisionRepository,
        faseMisionRepository: FaseMisionRepository,
        preguntaFaseRepository: PreguntaFaseRepository,
        insigniaRepository: InsigniaRepository,
        misionInsigniaRepository: MisionInsigniaRepository
    ) = CommandLineRunner {

        if (misionRepository.count() > 0) {
            logger.info("✅ Datos de misiones ya cargados")
            return@CommandLineRunner
        }

        logger.info("📜 Cargando datos de Misiones Culturales...")

        // ========== INSIGNIAS ==========
        val insignias = listOf(
            Insignia(
                codigo = "primera_mision",
                nombre = "Primer Paso",
                nombreKichwa = "Ñawpaq Puriy",
                descripcion = "Completaste tu primera misión cultural",
                icono = "🎖️",
                rareza = RarezaInsignia.COMUN
            ),
            Insignia(
                codigo = "explorador_novato",
                nombre = "Explorador Novato",
                nombreKichwa = "Mushuk Purichik",
                descripcion = "Has comenzado tu viaje por Ingapirca",
                icono = "🗺️",
                rareza = RarezaInsignia.COMUN
            ),
            Insignia(
                codigo = "sabio_inca",
                nombre = "Sabio Inca",
                nombreKichwa = "Inka Yachak",
                descripcion = "Dominas el conocimiento ancestral",
                icono = "📚",
                rareza = RarezaInsignia.RARA
            ),
            Insignia(
                codigo = "guardian_templo",
                nombre = "Guardián del Templo",
                nombreKichwa = "Wasi Rikuk",
                descripcion = "Protector del conocimiento del Templo del Sol",
                icono = "⛩️",
                rareza = RarezaInsignia.EPICA
            ),
            Insignia(
                codigo = "maestro_cultural",
                nombre = "Maestro Cultural",
                nombreKichwa = "Kawsay Yachachik",
                descripcion = "Has dominado todas las enseñanzas culturales",
                icono = "👑",
                rareza = RarezaInsignia.LEGENDARIA
            )
        )

        val insigniasGuardadas = insigniaRepository.saveAll(insignias)
        logger.info("✅ Guardadas {} insignias", insigniasGuardadas.size)
        // ========== MISIÓN 1: BIENVENIDA A INGAPIRCA ==========
        val mision1 = misionRepository.save(
            Mision(
                titulo = "Bienvenida a Ingapirca",
                tituloKichwa = "Ingapirca-man shamushka",
                descripcionCorta = "Conoce el complejo arqueológico más importante del Ecuador y a tu guía espiritual.",
                descripcionLarga = """
                    El sitio arqueológico de Ingapirca representa la fusión entre dos grandes culturas: 
                    la Cañari y la Inca. En esta misión introductoria, conocerás a Amaru, tu guía espiritual, 
                    quien te enseñará los secretos de este lugar sagrado. Aprenderás sobre la historia, 
                    arquitectura y cosmovisión andina que hacen de Ingapirca un tesoro cultural invaluable.
                """.trimIndent(),
                imagenPortada = "/assets/misiones/bienvenida.jpg",
                dificultad = DificultadMision.FACIL,
                tiempoEstimado = 10,
                experienciaRecompensa = 500,
                puntosRecompensa = 100,
                npcNombre = "Amaru",
                npcNombreKichwa = "Amaru Yachak",
                npcAvatar = "/assets/npc/amaru.png",
                npcDialogoInicial = """
                    ¡Allin p'unchay, wayna! (¡Buenos días, joven!)
                    
                    Soy Amaru, guardián ancestral de Ingapirca. He esperado mucho tiempo para conocerte. 
                    Este lugar sagrado guarda secretos milenarios que solo los elegidos pueden descubrir. 
                    
                    ¿Estás listo para comenzar tu viaje hacia el conocimiento ancestral?
                """.trimIndent(),
                nivelMinimo = 1,
                orden = 1
            )
        )

        // Fases de Misión 1
        val fase1_1 = faseMisionRepository.save(
            FaseMision(
                misionId = mision1.id!!,
                numeroFase = 1,
                titulo = "Conoce a tu guía",
                descripcion = """
                    Amaru te da la bienvenida a Ingapirca. Escucha atentamente su historia y 
                    aprende sobre el propósito de este lugar sagrado.
                """.trimIndent(),
                tipoFase = TipoFase.DIALOGO,
                configuracion = objectMapper.writeValueAsString(
                    mapOf(
                        "npcNombre" to "Amaru",
                        "npcAvatar" to "/assets/npc/amaru.png"
                    )
                ),
                experienciaFase = 100
            )
        )

        val fase1_2 = faseMisionRepository.save(
            FaseMision(
                misionId = mision1.id!!,
                numeroFase = 2,
                titulo = "Prueba de Conocimiento",
                descripcion = "Demuestra que has prestado atención a las enseñanzas de Amaru.",
                tipoFase = TipoFase.QUIZ,
                experienciaFase = 200
            )
        )

        // Preguntas para Fase 1.2
        val preguntasF1_2 = listOf(
            PreguntaFase(
                faseId = fase1_2.id!!,
                pregunta = "¿Qué dos culturas se fusionaron en Ingapirca?",
                opcionA = "Inca y Maya",
                opcionB = "Cañari e Inca",
                opcionC = "Azteca e Inca",
                opcionD = "Cañari y Maya",
                respuestaCorrecta = "B",
                retroalimentacionCorrecta = "¡Correcto! Ingapirca es el resultado de la fusión entre la cultura Cañari (originaria) y la Inca (conquistadora).",
                retroalimentacionIncorrecta = "No es correcto. Ingapirca fue construido por los Cañaris y luego ocupado por los Incas, creando una fusión única de estilos arquitectónicos.",
                puntos = 100,
                orden = 1
            ),
            PreguntaFase(
                faseId = fase1_2.id!!,
                pregunta = "¿Cuál es la estructura más emblemática de Ingapirca?",
                opcionA = "La Plaza Ceremonial",
                opcionB = "El Camino del Inca",
                opcionC = "El Templo del Sol",
                opcionD = "Los Depósitos",
                respuestaCorrecta = "C",
                retroalimentacionCorrecta = "¡Excelente! El Templo del Sol (Inti Wasi) es la construcción más icónica de Ingapirca, con su característica forma elíptica.",
                retroalimentacionIncorrecta = "Aunque todas son estructuras importantes, el Templo del Sol es la más emblemática por su arquitectura única y significado religioso.",
                puntos = 100,
                orden = 2
            ),
            PreguntaFase(
                faseId = fase1_2.id!!,
                pregunta = "¿Qué significa 'Ingapirca' en kichwa?",
                opcionA = "Templo del Sol",
                opcionB = "Muro del Inca",
                opcionC = "Casa Sagrada",
                opcionD = "Montaña Dorada",
                respuestaCorrecta = "B",
                retroalimentacionCorrecta = "¡Perfecto! 'Ingapirca' significa 'Muro del Inca' en kichwa, haciendo referencia a las impresionantes construcciones incas.",
                retroalimentacionIncorrecta = "No es correcto. 'Ingapirca' proviene de 'Inga' (Inca) y 'Pirca' (muro), significando 'Muro del Inca'.",
                puntos = 100,
                orden = 3
            )
        )

        preguntaFaseRepository.saveAll(preguntasF1_2)

        val fase1_3 = faseMisionRepository.save(
            FaseMision(
                misionId = mision1.id!!,
                numeroFase = 3,
                titulo = "Visita el Templo del Sol",
                descripcion = "Dirígete al Templo del Sol y explora sus alrededores. Observa la arquitectura y siente la energía del lugar.",
                tipoFase = TipoFase.VISITAR_PUNTO,
                puntoInteresId = 1, // ID del Templo del Sol
                experienciaFase = 200
            )
        )

        logger.info("✅ Misión 1 creada con {} fases", 3)

        // ========== MISIÓN 2: SECRETOS DEL TEMPLO ==========
        val mision2 = misionRepository.save(
            Mision(
                titulo = "Secretos del Templo",
                tituloKichwa = "Inti Wasi Pakasqa",
                descripcionCorta = "Descubre los misterios arquitectónicos y astronómicos del Templo del Sol.",
                descripcionLarga = """
                    El Templo del Sol guarda secretos que van más allá de su impresionante arquitectura. 
                    En esta misión, aprenderás sobre las alineaciones astronómicas, el significado ritual 
                    de cada elemento y buscarás artefactos ceremoniales escondidos en sus alrededores.
                """.trimIndent(),
                imagenPortada = "/assets/misiones/templo-secretos.jpg",
                dificultad = DificultadMision.MEDIO,
                tiempoEstimado = 15,
                experienciaRecompensa = 800,
                puntosRecompensa = 200,
                npcNombre = "Amaru",
                npcNombreKichwa = "Amaru Yachak",
                npcAvatar = "/assets/npc/amaru.png",
                npcDialogoInicial = """
                    El Templo del Sol no es solo piedra y argamasa, joven explorador. 
                    Es un calendario viviente, un observatorio astronómico y un portal espiritual. 
                    Hoy aprenderás a leer las señales que los antiguos dejaron grabadas en cada piedra.
                """.trimIndent(),
                nivelMinimo = 2,
                misionesPrevias = objectMapper.writeValueAsString(listOf(mision1.id)),
                orden = 2
            )
        )

        val fase2_1 = faseMisionRepository.save(
            FaseMision(
                misionId = mision2.id!!,
                numeroFase = 1,
                titulo = "Lección de Astronomía Inca",
                descripcion = "Aprende sobre cómo los incas utilizaban el templo para observar los astros.",
                tipoFase = TipoFase.QUIZ,
                experienciaFase = 200
            )
        )

        val preguntasF2_1 = listOf(
            PreguntaFase(
                faseId = fase2_1.id!!,
                pregunta = "¿Qué fenómeno astronómico se puede observar desde el Templo del Sol?",
                opcionA = "Eclipses lunares",
                opcionB = "Solsticios",
                opcionC = "Lluvia de meteoros",
                opcionD = "Auroras boreales",
                respuestaCorrecta = "B",
                retroalimentacionCorrecta = "¡Correcto! El templo está alineado para observar los solsticios, eventos cruciales en el calendario agrícola inca.",
                retroalimentacionIncorrecta = "No es correcto. El diseño elíptico del templo permite observar los solsticios de verano e invierno.",
                puntos = 150,
                orden = 1
            ),
            PreguntaFase(
                faseId = fase2_1.id!!,
                pregunta = "¿Cómo llamaban los incas a la Vía Láctea?",
                opcionA = "Inti Ñan",
                opcionB = "Mayu",
                opcionC = "Pachamama",
                opcionD = "Illapa",
                respuestaCorrecta = "B",
                retroalimentacionCorrecta = "¡Excelente! 'Mayu' significa 'río celestial', así llamaban a la Vía Láctea, viéndola como contraparte de los ríos terrestres.",
                retroalimentacionIncorrecta = "La respuesta correcta es Mayu (río celestial). Los incas veían la Vía Láctea como un río cósmico.",
                puntos = 150,
                orden = 2
            )
        )

        preguntaFaseRepository.saveAll(preguntasF2_1)

        val fase2_2 = faseMisionRepository.save(
            FaseMision(
                misionId = mision2.id!!,
                numeroFase = 2,
                titulo = "Busca el Tumi Ceremonial",
                descripcion = "Se dice que hay un antiguo Tumi escondido cerca del templo. Usa tus habilidades de exploración para encontrarlo.",
                tipoFase = TipoFase.BUSCAR_ARTEFACTO,
                puntoInteresId = 1,
                configuracion = objectMapper.writeValueAsString(
                    mapOf(
                        "artefactoId" to 1,
                        "artefactoNombre" to "Tumi Ceremonial"
                    )
                ),
                experienciaFase = 300
            )
        )

        val fase2_3 = faseMisionRepository.save(
            FaseMision(
                misionId = mision2.id!!,
                numeroFase = 3,
                titulo = "Decisión del Guardián",
                descripcion = """
                    Amaru te plantea un dilema: Has encontrado un artefacto sagrado. 
                    ¿Qué harás con él?
                """.trimIndent(),
                tipoFase = TipoFase.DECISION,
                configuracion = objectMapper.writeValueAsString(
                    mapOf(
                        "opciones" to listOf(
                            mapOf(
                                "id" to "donar",
                                "texto" to "Donarlo al museo local para preservar la cultura",
                                "consecuencia" to "Tu sabiduría honra a los ancestros. +100 XP extra"
                            ),
                            mapOf(
                                "id" to "estudiar",
                                "texto" to "Estudiarlo personalmente para aprender más",
                                "consecuencia" to "Tu sed de conocimiento es admirable. +50 XP extra"
                            ),
                            mapOf(
                                "id" to "guardar",
                                "texto" to "Devolverlo al lugar donde lo encontraste",
                                "consecuencia" to "Respetas el lugar sagrado. Amaru sonríe."
                            )
                        )
                    )
                ),
                experienciaFase = 300
            )
        )

        logger.info("✅ Misión 2 creada con {} fases", 3)

        // ========== MISIÓN 3: CAMINO DEL CONOCIMIENTO ==========
        val mision3 = misionRepository.save(
            Mision(
                titulo = "Camino del Conocimiento",
                tituloKichwa = "Yachay Ñan",
                descripcionCorta = "Recorre el Qhapaq Ñan y aprende sobre el sistema vial inca.",
                descripcionLarga = """
                    El Qhapaq Ñan, el Gran Camino del Inca, conectaba todo el Tahuantinsuyu. 
                    Esta misión te llevará a explorar este antiguo camino, aprender sobre los chasquis 
                    (mensajeros imperiales) y entender la importancia del sistema vial en el imperio inca.
                """.trimIndent(),
                imagenPortada = "/assets/misiones/qhapaq-nan.jpg",
                dificultad = DificultadMision.MEDIO,
                tiempoEstimado = 20,
                experienciaRecompensa = 1000,
                puntosRecompensa = 250,
                npcNombre = "Chasqui Waman",
                npcNombreKichwa = "Waman Chasqui",
                npcAvatar = "/assets/npc/chasqui.png",
                npcDialogoInicial = """
                    ¡Saludos, viajero! Soy Waman, descendiente de los chasquis imperiales. 
                    Mis ancestros corrían por estos caminos llevando mensajes que podían cambiar el destino del imperio. 
                    Hoy te enseñaré los secretos del Qhapaq Ñan.
                """.trimIndent(),
                nivelMinimo = 3,
                misionesPrevias = objectMapper.writeValueAsString(listOf(mision2.id)),
                orden = 3
            )
        )

        val fase3_1 = faseMisionRepository.save(
            FaseMision(
                misionId = mision3.id!!,
                numeroFase = 1,
                titulo = "Explora el Camino",
                descripcion = "Recorre una sección del Qhapaq Ñan durante al menos 5 minutos. Observa la ingeniería inca.",
                tipoFase = TipoFase.EXPLORACION_LIBRE,
                puntoInteresId = 7, // Camino del Inca
                experienciaFase = 300
            )
        )

        val fase3_2 = faseMisionRepository.save(
            FaseMision(
                misionId = mision3.id!!,
                numeroFase = 2,
                titulo = "Quiz: Los Chasquis",
                descripcion = "Demuestra tu conocimiento sobre los mensajeros imperiales.",
                tipoFase = TipoFase.QUIZ,
                experienciaFase = 400
            )
        )

        val preguntasF3_2 = listOf(
            PreguntaFase(
                faseId = fase3_2.id!!,
                pregunta = "¿Cada cuántos kilómetros había tambos (posadas) en el Qhapaq Ñan?",
                opcionA = "5-10 km",
                opcionB = "20-30 km",
                opcionC = "50-60 km",
                opcionD = "100 km",
                respuestaCorrecta = "B",
                retroalimentacionCorrecta = "¡Perfecto! Los tambos estaban estratégicamente ubicados cada 20-30 km, permitiendo a los chasquis descansar y cambiar relevos.",
                retroalimentacionIncorrecta = "Los tambos se ubicaban cada 20-30 km, la distancia ideal para que los chasquis pudieran mantener velocidad y resistencia.",
                puntos = 200,
                orden = 1
            ),
            PreguntaFase(
                faseId = fase3_2.id!!,
                pregunta = "¿Cuánto medía aproximadamente toda la red del Qhapaq Ñan?",
                opcionA = "10,000 km",
                opcionB = "25,000 km",
                opcionC = "40,000 km",
                opcionD = "60,000 km",
                respuestaCorrecta = "C",
                retroalimentacionCorrecta = "¡Correcto! El Qhapaq Ñan tenía más de 40,000 km, conectando desde Colombia hasta Chile y Argentina.",
                retroalimentacionIncorrecta = "La red completa del Qhapaq Ñan superaba los 40,000 km de caminos pavimentados y puentes.",
                puntos = 200,
                orden = 2
            )
        )

        preguntaFaseRepository.saveAll(preguntasF3_2)

        logger.info("✅ Misión 3 creada con {} fases", 2)

        // ========== RELACIONES MISIÓN-INSIGNIA ==========
        val relacionesInsignias = listOf(
            MisionInsignia(misionId = mision1.id!!, insigniaId = insigniasGuardadas[0].id!!), // Primera misión
            MisionInsignia(misionId = mision1.id!!, insigniaId = insigniasGuardadas[1].id!!), // Explorador novato
            MisionInsignia(misionId = mision2.id!!, insigniaId = insigniasGuardadas[3].id!!), // Guardián del templo
            MisionInsignia(misionId = mision3.id!!, insigniaId = insigniasGuardadas[2].id!!)  // Sabio inca
        )

        misionInsigniaRepository.saveAll(relacionesInsignias)
        logger.info("✅ Guardadas {} relaciones misión-insignia", relacionesInsignias.size)

        logger.info("🎉 Datos de Misiones cargados exitosamente!")
        logger.info("📊 Resumen: 3 misiones, {} insignias", insigniasGuardadas.size)
    }
}