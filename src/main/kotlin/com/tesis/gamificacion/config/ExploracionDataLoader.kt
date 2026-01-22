package com.tesis.gamificacion.config

import com.tesis.gamificacion.model.entities.*
import com.tesis.gamificacion.model.enums.*
import com.tesis.gamificacion.repository.*
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order

@Configuration
class ExploracionDataLoader {
    private val logger = LoggerFactory.getLogger(ExploracionDataLoader::class.java)

    @Bean
    @Order(4)
    fun cargarDatosExploracion(
        puntoInteresRepository: PuntoInteresRepository,
        artefactoRepository: ArtefactoRepository,
        preguntaQuizRepository: PreguntaQuizRepository
    ) = CommandLineRunner {

        if (puntoInteresRepository.count() > 0) {
            logger.info("✅ Datos de exploración ya cargados")
            return@CommandLineRunner
        }

        logger.info("🏛️ Cargando datos de Exploración Ingapirca...")

        // ========== PUNTOS DE INTERÉS (URLs REALES) ==========
        val puntos = listOf(
            PuntoInteres(
                nombre = "Templo del Sol",
                nombreKichwa = "Inti Wasi",
                descripcion = "El Templo del Sol es la estructura más emblemática de Ingapirca. Construido con la precisión característica de la arquitectura inca, este edificio elíptico se eleva majestuosamente sobre el complejo.",
                historiaDetallada = """
                    El Templo del Sol (Inti Wasi) representa la fusión arquitectónica cañari-inca más importante del Ecuador.
                    Construido durante el reinado del Inca Huayna Cápac, este templo ceremonial combina técnicas de construcción
                    cañaris con la perfección lítica inca. Sus muros perfectamente labrados sin argamasa demuestran el dominio
                    arquitectónico inca. El edificio elíptico se alinea astronómicamente con los solsticios, funcionando también
                    como observatorio solar. Aquí se realizaban ceremonias al Inti (Sol) y se ofrecían sacrificios rituales.
                """.trimIndent(),
                imagenUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8f/Ingapirca_Ruins.jpg/1200px-Ingapirca_Ruins.jpg",
                latitud = 50.0,
                longitud = 35.0,
                categoria = CategoriaPunto.TEMPLO,
                nivelMinimo = 1,
                ordenDesbloqueo = 1
            ),

            PuntoInteres(
                nombre = "Plaza Principal",
                nombreKichwa = "Hatun Pampa",
                descripcion = "Amplio espacio ceremonial donde se realizaban festividades, asambleas y rituales comunitarios. Centro neurálgico de la vida social cañari-inca.",
                historiaDetallada = """
                    La Plaza Principal (Hatun Pampa) era el corazón palpitante de Ingapirca. Este espacio rectangular servía
                    como centro de reunión para ceremonias estatales, festivales religiosos y eventos comunitarios. Durante
                    el Inti Raymi y otras festividades importantes, miles de personas se congregaban aquí. La plaza está
                    rodeada de kallankas (edificios largos) que servían como alojamiento temporal durante las celebraciones.
                    Los arqueólogos han encontrado evidencia de ofrendas ceremoniales enterradas bajo el piso de la plaza.
                """.trimIndent(),
                imagenUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3e/Ingapirca_Ecuador_03.jpg/1200px-Ingapirca_Ecuador_03.jpg",
                latitud = 45.0,
                longitud = 50.0,
                categoria = CategoriaPunto.PLAZA,
                nivelMinimo = 1,
                ordenDesbloqueo = 1
            ),

            PuntoInteres(
                nombre = "Aposento Real",
                nombreKichwa = "Inka Wasi",
                descripcion = "Residencia destinada a la nobleza inca cuando visitaban el complejo. Construcción de alto prestigio con detalles arquitectónicos refinados.",
                historiaDetallada = """
                    El Aposento Real (Inka Wasi) era la residencia exclusiva del Inca y su séquito durante sus visitas a Ingapirca.
                    Este edificio se distingue por la calidad superior de su mampostería, con bloques perfectamente tallados y
                    ensamblados. Contaba con sistemas avanzados de drenaje y nichos trapezoidales para almacenamiento y decoración.
                    Las habitaciones estaban decoradas con textiles finos y objetos de oro y plata. Los muros presentan hornacinas
                    ceremoniales donde se colocaban ídolos y ofrendas. Este espacio era sagrado y solo accesible para la elite inca.
                """.trimIndent(),
                imagenUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a7/Ingapirca_wall.jpg/1200px-Ingapirca_wall.jpg",
                latitud = 60.0,
                longitud = 30.0,
                categoria = CategoriaPunto.VIVIENDA,
                nivelMinimo = 2,
                ordenDesbloqueo = 2
            ),

            PuntoInteres(
                nombre = "Depósitos (Qolqas)",
                nombreKichwa = "Qolqa Wasi",
                descripcion = "Almacenes estatales donde se guardaban alimentos, textiles y otros recursos. Sistema de redistribución inca.",
                historiaDetallada = """
                    Los Qolqas eran almacenes estatales fundamentales para el sistema económico inca de redistribución.
                    Estas estructuras circulares con techos cónicos almacenaban maíz, quinua, chuño (papa deshidratada), charqui
                    (carne seca), textiles y herramientas. El diseño permitía ventilación natural para preservar los alimentos.
                    Los productos almacenados se distribuían durante festivales, emergencias o para alimentar a los trabajadores
                    de obras públicas. Los kipukamayuq (contadores incas) registraban meticulosamente todas las entradas y salidas
                    mediante quipus (sistema de cordeles con nudos).
                """.trimIndent(),
                imagenUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/9/94/Qolqas_Inca.jpg/800px-Qolqas_Inca.jpg",
                latitud = 70.0,
                longitud = 55.0,
                categoria = CategoriaPunto.DEPOSITO,
                nivelMinimo = 2,
                ordenDesbloqueo = 3
            ),

            PuntoInteres(
                nombre = "Observatorio Astronómico",
                nombreKichwa = "Quyllur Wasi",
                descripcion = "Estructura diseñada para observación celestial y medición del tiempo. Los incas eran expertos astrónomos.",
                historiaDetallada = """
                    El Observatorio Astronómico (Quyllur Wasi - Casa de las Estrellas) demuestra el avanzado conocimiento
                    astronómico inca-cañari. Desde este punto elevado, los astrónomos incas observaban el movimiento del sol,
                    la luna, las estrellas y las constelaciones. Utilizaban marcadores de piedra alineados con puntos específicos
                    del horizonte para determinar solsticios, equinoccios y el inicio de temporadas agrícolas.
                """.trimIndent(),
                imagenUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/52/Ingapirca_panorama.jpg/1200px-Ingapirca_panorama.jpg",
                latitud = 35.0,
                longitud = 25.0,
                categoria = CategoriaPunto.OBSERVATORIO,
                nivelMinimo = 3,
                ordenDesbloqueo = 4
            ),

            PuntoInteres(
                nombre = "Fuente Ceremonial",
                nombreKichwa = "Pukyu Wasi",
                descripcion = "Sistema hidráulico ritual donde se realizaban ceremonias de purificación. El agua era sagrada para los incas.",
                historiaDetallada = """
                    La Fuente Ceremonial (Pukyu Wasi) es una obra maestra de ingeniería hidráulica ritual. El agua fluía
                    desde manantiales naturales a través de canales de piedra finamente tallados. Los incas consideraban el
                    agua como elemento sagrado (yaku mama - madre agua) y realizaban aquí ceremonias de purificación ritual.
                    Antes de grandes festivales o eventos importantes, sacerdotes y nobles se purificaban en estas aguas.
                """.trimIndent(),
                imagenUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/2/28/Inca_water_channel.jpg/800px-Inca_water_channel.jpg",
                latitud = 40.0,
                longitud = 60.0,
                categoria = CategoriaPunto.CEREMONIAL,
                nivelMinimo = 3,
                ordenDesbloqueo = 5
            ),

            PuntoInteres(
                nombre = "Camino del Inca",
                nombreKichwa = "Qhapaq Ñan",
                descripcion = "Sección del famoso sistema vial inca que conectaba todo el Tahuantinsuyu. Ingeniería excepcional.",
                historiaDetallada = """
                    El Qhapaq Ñan (Camino Real del Inca) era una red vial de más de 40,000 km que conectaba todo el imperio
                    desde Colombia hasta Chile y Argentina. Esta sección atraviesa Ingapirca como parte de la ruta que unía
                    Quito con Cusco. Los caminos estaban construidos con piedra, tenían sistemas de drenaje y puentes colgantes.
                    Cada 20-30 km había tambos (posadas estatales) donde chasquis (mensajeros) descansaban y cambiaban relevos.
                """.trimIndent(),
                imagenUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/a/ab/Qhapaq_%C3%91an.jpg/1200px-Qhapaq_%C3%91an.jpg",
                latitud = 25.0,
                longitud = 45.0,
                categoria = CategoriaPunto.CAMINO,
                nivelMinimo = 1,
                ordenDesbloqueo = 1
            ),

            PuntoInteres(
                nombre = "Recinto Ceremonial Cañari",
                nombreKichwa = "Kañari Uku Wasi",
                descripcion = "Espacio sagrado cañari pre-inca. Evidencia de la cultura que habitaba antes de la llegada inca.",
                historiaDetallada = """
                    El Recinto Ceremonial Cañari representa la cultura original que habitó Ingapirca siglos antes de la
                    conquista inca. Los cañaris eran excelentes orfebres, ceramistas y arquitectos. Este espacio ceremonial
                    estaba dedicado a la Luna (Killa) y a las deidades femeninas del agua y la fertilidad. Las estructuras
                    cañaris usaban piedra labrada más rudimentaria que la inca, pero igualmente efectiva.
                """.trimIndent(),
                imagenUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b4/Ingapirca_ruins_Ecuador.jpg/1200px-Ingapirca_ruins_Ecuador.jpg",
                latitud = 55.0,
                longitud = 65.0,
                categoria = CategoriaPunto.CEREMONIAL,
                nivelMinimo = 4,
                ordenDesbloqueo = 6
            )
        )

        val puntosGuardados = puntoInteresRepository.saveAll(puntos)
        logger.info("✅ Guardados ${puntosGuardados.size} puntos de interés")

        // ========== ARTEFACTOS (URLs REALES) ==========
        val artefactos = listOf(
            // Templo del Sol
            Artefacto(
                nombre = "Tumi Ceremonial",
                nombreKichwa = "Inti Tumi",
                descripcion = "Cuchillo ceremonial de bronce usado en rituales al Sol. Mango decorado con iconografía solar.",
                imagenUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/9/9d/Tumi_knife_Sican_culture.jpg/800px-Tumi_knife_Sican_culture.jpg",
                categoria = CategoriaArtefacto.METAL,
                rareza = 5,
                puntoInteres = puntosGuardados[0],
                probabilidadEncuentro = 0.15
            ),

            Artefacto(
                nombre = "Quero de Madera",
                nombreKichwa = "Qiru",
                descripcion = "Vaso ceremonial de madera tallada para beber chicha durante rituales.",
                imagenUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e0/Quero_Inca.jpg/800px-Quero_Inca.jpg",
                categoria = CategoriaArtefacto.RITUAL,
                rareza = 4,
                puntoInteres = puntosGuardados[0],
                probabilidadEncuentro = 0.25
            ),

            // Plaza Principal
            Artefacto(
                nombre = "Cerámica Polícroma",
                nombreKichwa = "Puyñu Mankha",
                descripcion = "Vasija ceremonial con diseños geométricos en rojo, negro y crema.",
                imagenUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a5/Inca_pottery.jpg/800px-Inca_pottery.jpg",
                categoria = CategoriaArtefacto.CERAMICA,
                rareza = 3,
                puntoInteres = puntosGuardados[1],
                probabilidadEncuentro = 0.35
            ),

            Artefacto(
                nombre = "Spondylus (Mullu)",
                nombreKichwa = "Mullu",
                descripcion = "Concha sagrada traída desde la costa ecuatoriana. Usada en ofrendas a los apus.",
                imagenUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/1f/Spondylus_shell.jpg/800px-Spondylus_shell.jpg",
                categoria = CategoriaArtefacto.RITUAL,
                rareza = 5,
                puntoInteres = puntosGuardados[1],
                probabilidadEncuentro = 0.10
            ),

            // Aposento Real
            Artefacto(
                nombre = "Alfiler de Plata (Tupu)",
                nombreKichwa = "Qullqi Tupu",
                descripcion = "Alfiler de plata usado para sujetar el manto de la nobleza.",
                imagenUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/2/2f/Tupu_Inca.jpg/600px-Tupu_Inca.jpg",
                categoria = CategoriaArtefacto.ORNAMENTO,
                rareza = 5,
                puntoInteres = puntosGuardados[2],
                probabilidadEncuentro = 0.12
            ),

            Artefacto(
                nombre = "Textil Cumbi",
                nombreKichwa = "Qumpi",
                descripcion = "Fragmento de textil fino de lana de vicuña. Tejido exclusivo de la nobleza inca.",
                imagenUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4f/Inca_textile.jpg/800px-Inca_textile.jpg",
                categoria = CategoriaArtefacto.TEXTIL,
                rareza = 4,
                puntoInteres = puntosGuardados[2],
                probabilidadEncuentro = 0.20
            ),

            // Depósitos (Qolqas)
            Artefacto(
                nombre = "Aríbalo Inca",
                nombreKichwa = "Urpu",
                descripcion = "Jarra de cerámica con base cónica para transportar chicha.",
                imagenUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3c/Aribalo_Inca.jpg/600px-Aribalo_Inca.jpg",
                categoria = CategoriaArtefacto.CERAMICA,
                rareza = 3,
                puntoInteres = puntosGuardados[3],
                probabilidadEncuentro = 0.40
            ),

            Artefacto(
                nombre = "Pala de Chakitaqlla",
                nombreKichwa = "Chakitaqlla",
                descripcion = "Herramienta agrícola andina para arar la tierra.",
                imagenUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/7/7e/Chakitaklla.jpg/600px-Chakitaklla.jpg",
                categoria = CategoriaArtefacto.HERRAMIENTA,
                rareza = 2,
                puntoInteres = puntosGuardados[3],
                probabilidadEncuentro = 0.45
            ),

            // Observatorio
            Artefacto(
                nombre = "Quipu Astronómico",
                nombreKichwa = "Quyllur Khipu",
                descripcion = "Sistema de cuerdas con nudos para registrar observaciones celestes.",
                imagenUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/52/Inca_Quipu.jpg/800px-Inca_Quipu.jpg",
                categoria = CategoriaArtefacto.HERRAMIENTA,
                rareza = 5,
                puntoInteres = puntosGuardados[4],
                probabilidadEncuentro = 0.08
            ),

            // Fuente Ceremonial
            Artefacto(
                nombre = "Conopa de Piedra",
                nombreKichwa = "Kunupa",
                descripcion = "Figura votiva tallada en piedra con forma de llama.",
                imagenUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d1/Conopa_llama.jpg/600px-Conopa_llama.jpg",
                categoria = CategoriaArtefacto.PIEDRA,
                rareza = 4,
                puntoInteres = puntosGuardados[5],
                probabilidadEncuentro = 0.22
            ),

            // Recinto Cañari
            Artefacto(
                nombre = "Hacha Ceremonial Cañari",
                nombreKichwa = "Kañari Kuti",
                descripcion = "Hacha de cobre arsenical con iconografía serpentina. Estilo pre-inca.",
                imagenUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/8/89/Ceremonial_axe_pre-Inca.jpg/600px-Ceremonial_axe_pre-Inca.jpg",
                categoria = CategoriaArtefacto.METAL,
                rareza = 5,
                puntoInteres = puntosGuardados[7],
                probabilidadEncuentro = 0.10
            )
        )

        val artefactosGuardados = artefactoRepository.saveAll(artefactos)
        logger.info("✅ Guardados ${artefactosGuardados.size} artefactos")

        // ========== PREGUNTAS DEL QUIZ ==========
        val preguntas = listOf(
            // Templo del Sol
            PreguntaQuiz(
                puntoInteres = puntosGuardados[0],
                nivelCapa = NivelCapa.INCA,
                pregunta = "¿Qué forma arquitectónica única tiene el Templo del Sol de Ingapirca?",
                opcionA = "Rectangular",
                opcionB = "Circular",
                opcionC = "Elíptica",
                opcionD = "Triangular",
                respuestaCorrecta = "C",
                explicacion = "El Templo del Sol tiene forma elíptica, una característica arquitectónica única que combina elementos cañaris e incas.",
                dificultad = 2,
                puntos = 20
            ),

            PreguntaQuiz(
                puntoInteres = puntosGuardados[0],
                nivelCapa = NivelCapa.INCA,
                pregunta = "¿Durante qué reinado inca se construyó principalmente el Templo del Sol?",
                opcionA = "Pachacútec",
                opcionB = "Huayna Cápac",
                opcionC = "Atahualpa",
                opcionD = "Tupac Yupanqui",
                respuestaCorrecta = "B",
                explicacion = "El Templo del Sol fue construido durante el reinado de Huayna Cápac, el último gran emperador inca antes de la conquista española.",
                dificultad = 3,
                puntos = 30
            ),

            // Plaza Principal
            PreguntaQuiz(
                puntoInteres = puntosGuardados[1],
                nivelCapa = NivelCapa.INCA,
                pregunta = "¿Qué significa 'Hatun Pampa' en kichwa?",
                opcionA = "Plaza Sagrada",
                opcionB = "Gran Plaza",
                opcionC = "Plaza del Rey",
                opcionD = "Plaza Ceremonial",
                respuestaCorrecta = "B",
                explicacion = "'Hatun Pampa' significa 'Gran Plaza' en kichwa. Era el espacio central para eventos comunitarios.",
                dificultad = 1,
                puntos = 10
            ),

            // Depósitos (Qolqas)
            PreguntaQuiz(
                puntoInteres = puntosGuardados[3],
                nivelCapa = NivelCapa.INCA,
                pregunta = "¿Qué es el 'chuño' que se almacenaba en los qolqas?",
                opcionA = "Maíz fermentado",
                opcionB = "Papa deshidratada",
                opcionC = "Carne salada",
                opcionD = "Quinua tostada",
                respuestaCorrecta = "B",
                explicacion = "El chuño es papa deshidratada mediante un proceso de congelación y secado. Podía almacenarse por años.",
                dificultad = 2,
                puntos = 20
            ),

            // Observatorio
            PreguntaQuiz(
                puntoInteres = puntosGuardados[4],
                nivelCapa = NivelCapa.INCA,
                pregunta = "¿Qué nombre daban los incas a la Vía Láctea?",
                opcionA = "Inti Ñan (Camino del Sol)",
                opcionB = "Mayu (Río Celestial)",
                opcionC = "Quyllur (Estrella)",
                opcionD = "Illapa (Rayo)",
                respuestaCorrecta = "B",
                explicacion = "Los incas llamaban Mayu (río celestial) a la Vía Láctea, viéndola como la contraparte celestial de los ríos terrestres.",
                dificultad = 3,
                puntos = 30
            ),

            // Recinto Cañari
            PreguntaQuiz(
                puntoInteres = puntosGuardados[7],
                nivelCapa = NivelCapa.CANARI,
                pregunta = "¿A qué deidad principal estaba dedicado el recinto ceremonial cañari?",
                opcionA = "El Sol (Inti)",
                opcionB = "La Luna (Killa)",
                opcionC = "El Rayo (Illapa)",
                opcionD = "La Tierra (Pachamama)",
                respuestaCorrecta = "B",
                explicacion = "El recinto cañari estaba dedicado principalmente a la Luna (Killa) y a deidades femeninas del agua y la fertilidad.",
                dificultad = 2,
                puntos = 20
            )
        )

        val preguntasGuardadas = preguntaQuizRepository.saveAll(preguntas)
        logger.info("✅ Guardadas ${preguntasGuardadas.size} preguntas del quiz")

        logger.info("🎉 Datos de Exploración Ingapirca cargados exitosamente!")
        logger.info("📊 Resumen: ${puntosGuardados.size} puntos, ${artefactosGuardados.size} artefactos, ${preguntasGuardadas.size} preguntas")
    }
}