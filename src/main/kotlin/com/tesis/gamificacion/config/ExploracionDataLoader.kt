package com.tesis.gamificacion.config

import com.tesis.gamificacion.model.entities.*
import com.tesis.gamificacion.model.enums.*
import com.tesis.gamificacion.repository.*
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ExploracionDataLoader {
    private val logger = LoggerFactory.getLogger(ExploracionDataLoader::class.java)

    @Bean
    fun cargarDatosExploracion(
        puntoInteresRepository: PuntoInteresRepository,
        artefactoRepository: ArtefactoRepository,
        preguntaQuizRepository: PreguntaQuizRepository,
        misionRepository: MisionExploracionRepository
    ) = CommandLineRunner {

        if (puntoInteresRepository.count() > 0) {
            logger.info("✅ Datos de exploración ya cargados")
            return@CommandLineRunner
        }

        logger.info("🏛️ Cargando datos de Exploración Ingapirca...")

        // ========== PUNTOS DE INTERÉS ==========
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
                imagenUrl = "/assets/exploracion/templo-sol.jpg",
                coordenadaX = 50.0,
                coordenadaY = 35.0,
                categoria = CategoriaPunto.TEMPLO,
                nivelRequerido = 1,
                puntosPorDescubrir = 200
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
                imagenUrl = "/assets/exploracion/plaza.jpg",
                coordenadaX = 45.0,
                coordenadaY = 50.0,
                categoria = CategoriaPunto.PLAZA,
                nivelRequerido = 1,
                puntosPorDescubrir = 150
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
                imagenUrl = "/assets/exploracion/aposento.jpg",
                coordenadaX = 60.0,
                coordenadaY = 30.0,
                categoria = CategoriaPunto.VIVIENDA,
                nivelRequerido = 2,
                puntosPorDescubrir = 180
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
                    mediante quipus (sistema de cordeles con nudos). Se estima que Ingapirca podía almacenar alimentos para 
                    varios miles de personas durante meses.
                """.trimIndent(),
                imagenUrl = "/assets/exploracion/qolqas.jpg",
                coordenadaX = 70.0,
                coordenadaY = 55.0,
                categoria = CategoriaPunto.DEPOSITO,
                nivelRequerido = 2,
                puntosPorDescubrir = 120
            ),

            PuntoInteres(
                nombre = "Observatorio Astronómico",
                nombreKichwa = "Quyllur Wasi",
                descripcion = "Estructura diseñada para observación celestial y medición del tiempo. Los incas eran expertos astrónomos.",
                historiaDetallada = """
                    El Observatorio Astronómico (Quyllur Wasi - Casa de las Estrellas) demuestra el avanzado conocimiento 
                    astronómico inca-cañari. Desde este punto elevado, los astrónomos incas (kipukamayuq especializado en 
                    astronomía) observaban el movimiento del sol, la luna, las estrellas y las constelaciones. Utilizaban 
                    marcadores de piedra alineados con puntos específicos del horizonte para determinar solsticios, equinoccios 
                    y el inicio de temporadas agrícolas. La Vía Láctea (Mayu - río celestial) era especialmente importante en 
                    la cosmovisión andina. Las "constelaciones oscuras" formadas por nubes de polvo interestelar representaban 
                    animales sagrados. Este conocimiento astronómico era crucial para el calendario agrícola y ceremonial.
                """.trimIndent(),
                imagenUrl = "/assets/exploracion/observatorio.jpg",
                coordenadaX = 35.0,
                coordenadaY = 25.0,
                categoria = CategoriaPunto.OBSERVATORIO,
                nivelRequerido = 3,
                puntosPorDescubrir = 220
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
                    El sistema incluía piletas escalonadas, cada una con significado ceremonial específico. Los canales 
                    estaban diseñados para producir sonidos específicos al fluir el agua, creando una experiencia sonora 
                    ritual. Las ofrendas de chicha (bebida de maíz fermentado) y hojas de coca se realizaban regularmente 
                    para honrar a los apus (espíritus de las montañas) y asegurar agua abundante.
                """.trimIndent(),
                imagenUrl = "/assets/exploracion/fuente.jpg",
                coordenadaX = 40.0,
                coordenadaY = 60.0,
                categoria = CategoriaPunto.CEREMONIAL,
                nivelRequerido = 3,
                puntosPorDescubrir = 190
            ),

            PuntoInteres(
                nombre = "Camino del Inca",
                nombreKichwa = "Qhapaq Ñan",
                descripcion = "Sección del famoso sistema vial inca que conectaba todo el Tahuantinsuyu. Ingeniería y planificación excepcionales.",
                historiaDetallada = """
                    El Qhapaq Ñan (Camino Real del Inca) era una red vial de más de 40,000 km que conectaba todo el imperio 
                    desde Colombia hasta Chile y Argentina. Esta sección atraviesa Ingapirca como parte de la ruta que unía 
                    Quito con Cusco. Los caminos estaban construidos con piedra, tenían sistemas de drenaje y puentes colgantes. 
                    Cada 20-30 km había tambos (posadas estatales) donde chasquis (mensajeros imperiales) descansaban y 
                    cambiaban relevos. Los chasquis podían transmitir mensajes desde Quito a Cusco (más de 2000 km) en solo 
                    7 días corriendo en relevos. El camino también servía para movilizar ejércitos, transportar productos y 
                    realizar peregrinaciones religiosas. Viajar por el Qhapaq Ñan era un privilegio regulado por el estado inca.
                """.trimIndent(),
                imagenUrl = "/assets/exploracion/qhapaq-nan.jpg",
                coordenadaX = 25.0,
                coordenadaY = 45.0,
                categoria = CategoriaPunto.CAMINO,
                nivelRequerido = 1,
                puntosPorDescubrir = 100
            ),

            PuntoInteres(
                nombre = "Recinto Ceremonial Cañari",
                nombreKichwa = "Kañari Uku Wasi",
                descripcion = "Espacio sagrado cañari pre-inca. Evidencia de la cultura que habitaba antes de la llegada inca.",
                historiaDetallada = """
                    El Recinto Ceremonial Cañari representa la cultura original que habitó Ingapirca siglos antes de la 
                    conquista inca. Los cañaris eran excelentes orfebres, ceramistas y arquitectos. Este espacio ceremonial 
                    estaba dedicado a la Luna (Killa) y a las deidades femeninas del agua y la fertilidad. Las estructuras 
                    cañaris usaban piedra labrada más rudimentaria que la inca, pero igualmente efectiva. Enterramientos 
                    encontrados aquí incluyen cerámicas con iconografía serpentina y felina, metales preciosos y Spondylus 
                    (concha sagrada traída desde la costa). Los cañaris resistieron inicialmente la conquista inca pero 
                    eventualmente se integraron al imperio, manteniendo cierta autonomía. Sus descendientes aún habitan la 
                    región y preservan tradiciones ancestrales como la medicina con hierbas y ceremonias a la Pachamama.
                """.trimIndent(),
                imagenUrl = "/assets/exploracion/canari.jpg",
                coordenadaX = 55.0,
                coordenadaY = 65.0,
                categoria = CategoriaPunto.CEREMONIAL,
                nivelRequerido = 4,
                puntosPorDescubrir = 250
            )
        )

        val puntosGuardados = puntoInteresRepository.saveAll(puntos)
        logger.info("✅ Guardados ${puntosGuardados.size} puntos de interés")

        // ========== ARTEFACTOS ==========
        val artefactos = listOf(
            // Templo del Sol
            Artefacto(
                nombre = "Tumi Ceremonial",
                nombreKichwa = "Inti Tumi",
                descripcion = "Cuchillo ceremonial de bronce usado en rituales al Sol. Mango decorado con iconografía solar.",
                imagenUrl = "/assets/artefactos/tumi.jpg",
                categoria = CategoriaArtefacto.METAL,
                rareza = 5,
                puntoInteresId = puntosGuardados[0].id!!,
                probabilidadEncuentro = 0.15
            ),

            Artefacto(
                nombre = "Quero de Madera",
                nombreKichwa = "Qiru",
                descripcion = "Vaso ceremonial de madera tallada para beber chicha durante rituales. Decoración geométrica policroma.",
                imagenUrl = "/assets/artefactos/quero.jpg",
                categoria = CategoriaArtefacto.RITUAL,
                rareza = 4,
                puntoInteresId = puntosGuardados[0].id!!,
                probabilidadEncuentro = 0.25
            ),

            // Plaza Principal
            Artefacto(
                nombre = "Cerámica Polícroma",
                nombreKichwa = "Puyñu Mankha",
                descripcion = "Vasija ceremonial con diseños geométricos en rojo, negro y crema. Estilo característico inca-cañari.",
                imagenUrl = "/assets/artefactos/ceramica.jpg",
                categoria = CategoriaArtefacto.CERAMICA,
                rareza = 3,
                puntoInteresId = puntosGuardados[1].id!!,
                probabilidadEncuentro = 0.35
            ),

            Artefacto(
                nombre = "Spondylus (Mullu)",
                nombreKichwa = "Mullu",
                descripcion = "Concha sagrada traída desde la costa ecuatoriana. Usada en ofrendas a los apus y en rituales de fertilidad.",
                imagenUrl = "/assets/artefactos/spondylus.jpg",
                categoria = CategoriaArtefacto.RITUAL,
                rareza = 5,
                puntoInteresId = puntosGuardados[1].id!!,
                probabilidadEncuentro = 0.10
            ),

            // Aposento Real
            Artefacto(
                nombre = "Alfiler de Plata (Tupu)",
                nombreKichwa = "Qullqi Tupu",
                descripcion = "Alfiler de plata usado para sujetar el manto de la nobleza. Cabeza decorada con diseño solar.",
                imagenUrl = "/assets/artefactos/tupu.jpg",
                categoria = CategoriaArtefacto.ORNAMENTO,
                rareza = 5,
                puntoInteresId = puntosGuardados[2].id!!,
                probabilidadEncuentro = 0.12
            ),

            Artefacto(
                nombre = "Textil Cumbi",
                nombreKichwa = "Qumpi",
                descripcion = "Fragmento de textil fino de lana de vicuña. Tejido exclusivo de la nobleza inca con tocapus (diseños geométricos).",
                imagenUrl = "/assets/artefactos/textil.jpg",
                categoria = CategoriaArtefacto.TEXTIL,
                rareza = 4,
                puntoInteresId = puntosGuardados[2].id!!,
                probabilidadEncuentro = 0.20
            ),

            // Depósitos (Qolqas)
            Artefacto(
                nombre = "Aríbalo Inca",
                nombreKichwa = "Urpu",
                descripcion = "Jarra de cerámica con base cónica para transportar chicha. Asas laterales y decoración característica.",
                imagenUrl = "/assets/artefactos/aribalo.jpg",
                categoria = CategoriaArtefacto.CERAMICA,
                rareza = 3,
                puntoInteresId = puntosGuardados[3].id!!,
                probabilidadEncuentro = 0.40
            ),

            Artefacto(
                nombre = "Pala de Chakitaqlla",
                nombreKichwa = "Chakitaqlla",
                descripcion = "Herramienta agrícola andina para arar la tierra. Punta de madera endurecida con pisadera de piedra.",
                imagenUrl = "/assets/artefactos/chakitaqlla.jpg",
                categoria = CategoriaArtefacto.HERRAMIENTA,
                rareza = 2,
                puntoInteresId = puntosGuardados[3].id!!,
                probabilidadEncuentro = 0.45
            ),

            // Observatorio
            Artefacto(
                nombre = "Quipu Astronómico",
                nombreKichwa = "Quyllur Khipu",
                descripcion = "Sistema de cuerdas con nudos para registrar observaciones celestes y calendario agrícola.",
                imagenUrl = "/assets/artefactos/quipu.jpg",
                categoria = CategoriaArtefacto.HERRAMIENTA,
                rareza = 5,
                puntoInteresId = puntosGuardados[4].id!!,
                probabilidadEncuentro = 0.08
            ),

            // Fuente Ceremonial
            Artefacto(
                nombre = "Conopa de Piedra",
                nombreKichwa = "Kunupa",
                descripcion = "Figura votiva tallada en piedra con forma de llama. Usada en rituales de fertilidad del ganado.",
                imagenUrl = "/assets/artefactos/conopa.jpg",
                categoria = CategoriaArtefacto.PIEDRA,
                rareza = 4,
                puntoInteresId = puntosGuardados[5].id!!,
                probabilidadEncuentro = 0.22
            ),

            // Recinto Cañari
            Artefacto(
                nombre = "Hacha Ceremonial Cañari",
                nombreKichwa = "Kañari Kuti",
                descripcion = "Hacha de cobre arsenical con iconografía serpentina. Estilo pre-inca exclusivamente cañari.",
                imagenUrl = "/assets/artefactos/hacha-canari.jpg",
                categoria = CategoriaArtefacto.METAL,
                rareza = 5,
                puntoInteresId = puntosGuardados[7].id!!,
                probabilidadEncuentro = 0.10
            )
        )

        val artefactosGuardados = artefactoRepository.saveAll(artefactos)
        logger.info("✅ Guardados ${artefactosGuardados.size} artefactos")
        // ========== PREGUNTAS DEL QUIZ ==========
        val preguntas = listOf(
            // Templo del Sol
            PreguntaQuiz(
                puntoInteresId = puntosGuardados[0].id!!,
                pregunta = "¿Qué forma arquitectónica única tiene el Templo del Sol de Ingapirca?",
                opcionA = "Rectangular",
                opcionB = "Circular",
                opcionC = "Elíptica",
                opcionD = "Triangular",
                respuestaCorrecta = "C",
                explicacion = "El Templo del Sol tiene forma elíptica, una característica arquitectónica única que combina elementos cañaris e incas. Esta forma permitía alineaciones astronómicas precisas.",
                dificultad = 2
            ),

            PreguntaQuiz(
                puntoInteresId = puntosGuardados[0].id!!,
                pregunta = "¿Durante qué reinado inca se construyó principalmente el Templo del Sol?",
                opcionA = "Pachacútec",
                opcionB = "Huayna Cápac",
                opcionC = "Atahualpa",
                opcionD = "Tupac Yupanqui",
                respuestaCorrecta = "B",
                explicacion = "El Templo del Sol fue construido durante el reinado de Huayna Cápac, el último gran emperador inca antes de la conquista española.",
                dificultad = 3
            ),

            PreguntaQuiz(
                puntoInteresId = puntosGuardados[0].id!!,
                pregunta = "¿Qué técnica de construcción caracteriza los muros del Templo del Sol?",
                opcionA = "Piedras unidas con argamasa",
                opcionB = "Adobe",
                opcionC = "Piedras labradas sin argamasa",
                opcionD = "Madera tallada",
                respuestaCorrecta = "C",
                explicacion = "Los muros están construidos con piedras perfectamente labradas que encajan sin necesidad de argamasa, demostrando la maestría arquitectónica inca.",
                dificultad = 1
            ),

            // Plaza Principal
            PreguntaQuiz(
                puntoInteresId = puntosGuardados[1].id!!,
                pregunta = "¿Qué significa 'Hatun Pampa' en kichwa?",
                opcionA = "Plaza Sagrada",
                opcionB = "Gran Plaza",
                opcionC = "Plaza del Rey",
                opcionD = "Plaza Ceremonial",
                respuestaCorrecta = "B",
                explicacion = "'Hatun Pampa' significa 'Gran Plaza' en kichwa. Era el espacio central para eventos comunitarios y ceremonias importantes.",
                dificultad = 1
            ),

            PreguntaQuiz(
                puntoInteresId = puntosGuardados[1].id!!,
                pregunta = "¿Qué tipo de estructuras rodean la Plaza Principal?",
                opcionA = "Templos pequeños",
                opcionB = "Kallankas (edificios largos)",
                opcionC = "Torres de vigilancia",
                opcionD = "Mercados",
                respuestaCorrecta = "B",
                explicacion = "Las kallankas son edificios largos que servían como alojamiento temporal durante festivales y ceremonias. Rodeaban la plaza principal.",
                dificultad = 2
            ),

            PreguntaQuiz(
                puntoInteresId = puntosGuardados[1].id!!,
                pregunta = "¿Durante qué festival importante se llenaba completamente la plaza?",
                opcionA = "Pawkar Raymi",
                opcionB = "Inti Raymi",
                opcionC = "Killa Raymi",
                opcionD = "Kapak Raymi",
                respuestaCorrecta = "B",
                explicacion = "El Inti Raymi (Fiesta del Sol) era el festival más importante del calendario inca. Miles de personas se congregaban en la plaza durante esta celebración.",
                dificultad = 2
            ),

            // Aposento Real
            PreguntaQuiz(
                puntoInteresId = puntosGuardados[2].id!!,
                pregunta = "¿Qué característica distinguía el Aposento Real de otras construcciones?",
                opcionA = "Era más grande",
                opcionB = "Calidad superior de mampostería",
                opcionC = "Tenía jardines",
                opcionD = "Estaba pintado",
                respuestaCorrecta = "B",
                explicacion = "El Aposento Real se distinguía por la calidad excepcional de su mampostería, con bloques perfectamente tallados y ensamblados, superior a otras construcciones.",
                dificultad = 2
            ),

            PreguntaQuiz(
                puntoInteresId = puntosGuardados[2].id!!,
                pregunta = "¿Qué forma tienen las hornacinas características de la arquitectura inca?",
                opcionA = "Rectangulares",
                opcionB = "Circulares",
                opcionC = "Trapezoidales",
                opcionD = "Triangulares",
                respuestaCorrecta = "C",
                explicacion = "Las hornacinas trapezoidales son características distintivas de la arquitectura inca. Servían para almacenamiento y también como nichos ceremoniales.",
                dificultad = 1
            ),

            // Depósitos (Qolqas)
            PreguntaQuiz(
                puntoInteresId = puntosGuardados[3].id!!,
                pregunta = "¿Qué es el 'chuño' que se almacenaba en los qolqas?",
                opcionA = "Maíz fermentado",
                opcionB = "Papa deshidratada",
                opcionC = "Carne salada",
                opcionD = "Quinua tostada",
                respuestaCorrecta = "B",
                explicacion = "El chuño es papa deshidratada mediante un proceso de congelación y secado. Podía almacenarse por años y era fundamental en la dieta andina.",
                dificultad = 2
            ),

            PreguntaQuiz(
                puntoInteresId = puntosGuardados[3].id!!,
                pregunta = "¿Cómo registraban los incas las entradas y salidas de los depósitos?",
                opcionA = "Con escritura",
                opcionB = "Con dibujos",
                opcionC = "Con quipus",
                opcionD = "Con marcas en piedra",
                respuestaCorrecta = "C",
                explicacion = "Los quipus eran sistemas de cuerdas con nudos usados para registrar información numérica. Los kipukamayuq eran especialistas en su uso y lectura.",
                dificultad = 1
            ),

            PreguntaQuiz(
                puntoInteresId = puntosGuardados[3].id!!,
                pregunta = "¿Para qué servía principalmente el sistema de qolqas?",
                opcionA = "Comercio internacional",
                opcionB = "Redistribución estatal",
                opcionC = "Venta privada",
                opcionD = "Tributo religioso",
                respuestaCorrecta = "B",
                explicacion = "Los qolqas eran parte del sistema de redistribución estatal inca. El estado almacenaba recursos y los distribuía durante emergencias, festivales o para obras públicas.",
                dificultad = 2
            ),

            // Observatorio
            PreguntaQuiz(
                puntoInteresId = puntosGuardados[4].id!!,
                pregunta = "¿Qué nombre daban los incas a la Vía Láctea?",
                opcionA = "Inti Ñan (Camino del Sol)",
                opcionB = "Mayu (Río Celestial)",
                opcionC = "Quyllur (Estrella)",
                opcionD = "Illapa (Rayo)",
                respuestaCorrecta = "B",
                explicacion = "Los incas llamaban Mayu (río celestial) a la Vía Láctea, viéndola como la contraparte celestial de los ríos terrestres. Era fundamental en su cosmovisión.",
                dificultad = 3
            ),

            PreguntaQuiz(
                puntoInteresId = puntosGuardados[4].id!!,
                pregunta = "¿Para qué servía principalmente el conocimiento astronómico inca?",
                opcionA = "Navegación marítima",
                opcionB = "Predecir eclipses",
                opcionC = "Calendario agrícola y ceremonial",
                opcionD = "Astrología predictiva",
                respuestaCorrecta = "C",
                explicacion = "El conocimiento astronómico era crucial para determinar las épocas de siembra y cosecha, así como las fechas de ceremonias religiosas importantes.",
                dificultad = 1
            ),

            // Fuente Ceremonial
            PreguntaQuiz(
                puntoInteresId = puntosGuardados[5].id!!,
                pregunta = "¿Qué significa 'yaku mama' en la cosmovisión andina?",
                opcionA = "Dios del agua",
                opcionB = "Madre agua",
                opcionC = "Agua sagrada",
                opcionD = "Río grande",
                respuestaCorrecta = "B",
                explicacion = "'Yaku mama' significa 'madre agua'. El agua era considerada un elemento sagrado y viviente en la cosmovisión andina.",
                dificultad = 2
            ),

            PreguntaQuiz(
                puntoInteresId = puntosGuardados[5].id!!,
                pregunta = "¿Qué bebida se ofrendaba comúnmente en la fuente ceremonial?",
                opcionA = "Agua pura",
                opcionB = "Vino",
                opcionC = "Chicha",
                opcionD = "Leche",
                respuestaCorrecta = "C",
                explicacion = "La chicha (bebida de maíz fermentado) era la ofrenda más común en ceremonias. Se ofrecía a los apus y deidades para pedir bendiciones.",
                dificultad = 1
            ),

            // Camino del Inca
            PreguntaQuiz(
                puntoInteresId = puntosGuardados[6].id!!,
                pregunta = "¿Aproximadamente cuántos kilómetros medía toda la red del Qhapaq Ñan?",
                opcionA = "10,000 km",
                opcionB = "25,000 km",
                opcionC = "40,000 km",
                opcionD = "60,000 km",
                respuestaCorrecta = "C",
                explicacion = "El Qhapaq Ñan era una red vial de más de 40,000 km que conectaba todo el imperio inca desde Colombia hasta Chile y Argentina.",
                dificultad = 3
            ),

            PreguntaQuiz(
                puntoInteresId = puntosGuardados[6].id!!,
                pregunta = "¿Quiénes eran los chasquis?",
                opcionA = "Guerreros de elite",
                opcionB = "Mensajeros imperiales",
                opcionC = "Sacerdotes del Sol",
                opcionD = "Constructores de caminos",
                respuestaCorrecta = "B",
                explicacion = "Los chasquis eran mensajeros imperiales que corrían en relevos por el Qhapaq Ñan, transmitiendo información rápidamente a través del imperio.",
                dificultad = 1
            ),

            PreguntaQuiz(
                puntoInteresId = puntosGuardados[6].id!!,
                pregunta = "¿Cada cuántos kilómetros había tambos (posadas) en el Qhapaq Ñan?",
                opcionA = "5-10 km",
                opcionB = "20-30 km",
                opcionC = "50-60 km",
                opcionD = "100 km",
                respuestaCorrecta = "B",
                explicacion = "Los tambos estaban ubicados cada 20-30 km a lo largo del camino, permitiendo a los chasquis descansar y cambiar relevos.",
                dificultad = 2
            ),

            // Recinto Cañari
            PreguntaQuiz(
                puntoInteresId = puntosGuardados[7].id!!,
                pregunta = "¿A qué deidad principal estaba dedicado el recinto ceremonial cañari?",
                opcionA = "El Sol (Inti)",
                opcionB = "La Luna (Killa)",
                opcionC = "El Rayo (Illapa)",
                opcionD = "La Tierra (Pachamama)",
                respuestaCorrecta = "B",
                explicacion = "El recinto cañari estaba dedicado principalmente a la Luna (Killa) y a deidades femeninas del agua y la fertilidad.",
                dificultad = 2
            ),

            PreguntaQuiz(
                puntoInteresId = puntosGuardados[7].id!!,
                pregunta = "¿Qué es el Spondylus en la cultura cañari-inca?",
                opcionA = "Un tipo de metal precioso",
                opcionB = "Una piedra sagrada",
                opcionC = "Una concha marina sagrada",
                opcionD = "Un textil ceremonial",
                respuestaCorrecta = "C",
                explicacion = "El Spondylus (mullu) es una concha marina traída desde la costa ecuatoriana, considerada sagrada y usada en ofrendas importantes.",
                dificultad = 2
            ),

            PreguntaQuiz(
                puntoInteresId = puntosGuardados[7].id!!,
                pregunta = "¿En qué se destacaban especialmente los cañaris?",
                opcionA = "Agricultura",
                opcionB = "orfebrería y cerámica",
                opcionC = "Guerra",
                opcionD = "Navegación",
                respuestaCorrecta = "B",
                explicacion = "Los cañaris eran reconocidos por su excelencia en orfebrería, creando objetos de oro y plata, así como en la elaboración de cerámica fina.",
                dificultad = 1
            )
        )

        val preguntasGuardadas = preguntaQuizRepository.saveAll(preguntas)
        logger.info("✅ Guardadas ${preguntasGuardadas.size} preguntas del quiz")

        // ========== MISIONES ==========
        val misiones = listOf(
            MisionExploracion(
                titulo = "Descubridor Novato",
                descripcion = "Visita tus primeros 3 puntos de interés en Ingapirca",
                tipo = TipoMision.DESCUBRIR_PUNTOS,
                objetivo = """{"puntos": [], "cantidad": 3}""",
                recompensaXP = 300,
                recompensaPuntos = 100,
                nivelRequerido = 1
            ),

            MisionExploracion(
                titulo = "Ruta del Templo",
                descripcion = "Visita el Templo del Sol, la Plaza Principal y el Aposento Real",
                tipo = TipoMision.DESCUBRIR_PUNTOS,
                objetivo = """{"puntos": [${puntosGuardados[0].id}, ${puntosGuardados[1].id}, ${puntosGuardados[2].id}], "cantidad": 3}""",
                recompensaXP = 500,
                recompensaPuntos = 200,
                nivelRequerido = 1
            ),

            MisionExploracion(
                titulo = "Coleccionista de Artefactos",
                descripcion = "Encuentra 5 artefactos diferentes en tu exploración",
                tipo = TipoMision.ENCONTRAR_ARTEFACTOS,
                objetivo = """{"cantidad": 5}""",
                recompensaXP = 800,
                recompensaPuntos = 300,
                nivelRequerido = 2
            ),

            MisionExploracion(
                titulo = "Sabio Cultural",
                descripcion = "Completa correctamente 10 preguntas del quiz",
                tipo = TipoMision.COMPLETAR_QUIZ,
                objetivo = """{"cantidad": 10}""",
                recompensaXP = 600,
                recompensaPuntos = 250,
                nivelRequerido = 2
            ),

            MisionExploracion(
                titulo = "Explorador Dedicado",
                descripcion = "Pasa al menos 30 minutos explorando diferentes puntos",
                tipo = TipoMision.TIEMPO_EXPLORACION,
                objetivo = """{"tiempoRequerido": 1800}""",
                recompensaXP = 400,
                recompensaPuntos = 150,
                nivelRequerido = 1
            ),

            MisionExploracion(
                titulo = "Camino del Inca Completo",
                descripcion = "Sigue la ruta histórica visitando puntos en orden específico",
                tipo = TipoMision.SECUENCIAL,
                objetivo = """{"puntos": [${puntosGuardados[6].id}, ${puntosGuardados[1].id}, ${puntosGuardados[0].id}, ${puntosGuardados[2].id}], "cantidad": 4}""",
                recompensaXP = 1000,
                recompensaPuntos = 500,
                nivelRequerido = 3
            ),

            MisionExploracion(
                titulo = "Maestro Arqueólogo",
                descripcion = "Alcanza nivel ORO en todos los puntos disponibles",
                tipo = TipoMision.DESCUBRIR_PUNTOS,
                objetivo = """{"puntos": [], "cantidad": 8}""",
                recompensaXP = 2000,
                recompensaPuntos = 1000,
                nivelRequerido = 4
            )
        )

        val misionesGuardadas = misionRepository.saveAll(misiones)
        logger.info("✅ Guardadas ${misionesGuardadas.size} misiones")

        logger.info("🎉 Datos de Exploración Ingapirca cargados exitosamente!")
        logger.info("📊 Resumen: ${puntosGuardados.size} puntos, ${artefactosGuardados.size} artefactos, ${preguntasGuardadas.size} preguntas, ${misionesGuardadas.size} misiones")
    }
}
