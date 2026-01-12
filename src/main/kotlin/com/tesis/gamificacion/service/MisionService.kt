package com.tesis.gamificacion.service

import com.tesis.gamificacion.dto.request.*
import com.tesis.gamificacion.dto.response.*
import com.tesis.gamificacion.model.entities.*
import com.tesis.gamificacion.model.enums.*
import com.tesis.gamificacion.repository.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class MisionService(
    private val misionRepository: MisionRepository,
    private val faseMisionRepository: FaseMisionRepository,
    private val preguntaFaseRepository: PreguntaFaseRepository,
    private val usuarioMisionRepository: UsuarioMisionRepository,
    private val insigniaRepository: InsigniaRepository,
    private val usuarioInsigniaRepository: UsuarioInsigniaRepository,
    private val misionInsigniaRepository: MisionInsigniaRepository,
    private val progresoExploracionRepository: ProgresoExploracionRepository
) {
    private val logger = LoggerFactory.getLogger(MisionService::class.java)
    private val objectMapper = jacksonObjectMapper()

    // ========== LISTADO DE MISIONES ==========

    @Transactional(readOnly = true)
    fun obtenerListadoMisiones(usuarioId: Long): ListaMisionesResponse {
        logger.info("📋 Obteniendo listado de misiones para usuario: {}", usuarioId)

        val todasMisiones = misionRepository.findByActivaTrueOrderByOrden()
        val progresoUsuario = usuarioMisionRepository.findByUsuarioId(usuarioId)
        val insigniasUsuario = usuarioInsigniaRepository.findByUsuarioId(usuarioId)
        val progreso = progresoExploracionRepository.findByUsuarioId(usuarioId)

        val nivelUsuario = progreso?.nivelArqueologo ?: 1

        val disponibles = mutableListOf<MisionCardDTO>()
        val enProgreso = mutableListOf<MisionCardDTO>()
        val completadas = mutableListOf<MisionCardDTO>()
        val bloqueadas = mutableListOf<MisionCardDTO>()

        todasMisiones.forEach { mision ->
            val progresoMision = progresoUsuario.find { it.misionId == mision.id }
            val estado = determinarEstadoMision(mision, progresoMision, nivelUsuario, completadas.map { it.id })

            val misionCard = convertirAMisionCard(mision, estado, progresoMision)

            when (estado) {
                EstadoMision.DISPONIBLE -> disponibles.add(misionCard)
                EstadoMision.EN_PROGRESO -> enProgreso.add(misionCard)
                EstadoMision.COMPLETADA -> completadas.add(misionCard)
                EstadoMision.BLOQUEADA -> bloqueadas.add(misionCard)
            }
        }

        val estadisticas = EstadisticasMisionesDTO(
            completadas = completadas.size,
            enProgreso = enProgreso.size,
            insigniasObtenidas = insigniasUsuario.size.toInt(),
            totalMisiones = todasMisiones.size,
            porcentajeCompletado = if (todasMisiones.isNotEmpty())
                (completadas.size.toDouble() / todasMisiones.size) * 100
            else 0.0
        )

        return ListaMisionesResponse(
            disponibles = disponibles,
            enProgreso = enProgreso,
            completadas = completadas,
            bloqueadas = bloqueadas,
            estadisticas = estadisticas
        )
    }

    private fun determinarEstadoMision(
        mision: Mision,
        progresoMision: UsuarioMision?,
        nivelUsuario: Int,
        misionesCompletadas: List<Long>
    ): EstadoMision {
        // Si ya tiene progreso, usar ese estado
        if (progresoMision != null) {
            return progresoMision.estado
        }

        // Verificar nivel mínimo
        if (nivelUsuario < mision.nivelMinimo) {
            return EstadoMision.BLOQUEADA
        }

        // Verificar misiones previas
        if (!mision.misionesPrevias.isNullOrEmpty()) {
            val previas: List<Long> = objectMapper.readValue(mision.misionesPrevias!!)
            if (!previas.all { it in misionesCompletadas }) {
                return EstadoMision.BLOQUEADA
            }
        }

        return EstadoMision.DISPONIBLE
    }

    private fun convertirAMisionCard(
        mision: Mision,
        estado: EstadoMision,
        progresoMision: UsuarioMision?
    ): MisionCardDTO {
        val fases = faseMisionRepository.findByMisionIdOrderByNumeroFase(mision.id!!)
        val insignias = obtenerInsigniasMision(mision.id!!)

        return MisionCardDTO(
            id = mision.id!!,
            titulo = mision.titulo,
            tituloKichwa = mision.tituloKichwa,
            descripcionCorta = mision.descripcionCorta,
            imagenPortada = mision.imagenPortada,
            dificultad = mision.dificultad,
            tiempoEstimado = mision.tiempoEstimado,
            estado = estado,
            npcGuia = NPCGuiaDTO(
                nombre = mision.npcNombre,
                nombreKichwa = mision.npcNombreKichwa,
                avatar = mision.npcAvatar
            ),
            recompensas = RecompensasDTO(
                experiencia = mision.experienciaRecompensa,
                puntos = mision.puntosRecompensa,
                insignias = insignias
            ),
            requisitos = RequisitosDTO(
                nivelMinimo = mision.nivelMinimo,
                misionesPrevias = if (mision.misionesPrevias != null)
                    objectMapper.readValue(mision.misionesPrevias!!) else null,
                insignias = if (mision.insigniasRequeridas != null)
                    objectMapper.readValue(mision.insigniasRequeridas!!) else null
            ),
            progreso = (if (progresoMision != null) {
                com.tesis.gamificacion.dto.misiones.ProgresoMisionDTO(
                    faseActual = progresoMision.faseActual,
                    totalFases = fases.size,
                    puntuacion = progresoMision.puntuacion,
                    intentos = progresoMision.intentos,
                    respuestasCorrectas = progresoMision.respuestasCorrectas,
                    respuestasIncorrectas = progresoMision.respuestasIncorrectas,
                    porcentajeCompletado = (progresoMision.faseActual.toDouble() / fases.size) * 100
                )
            } else null) as ProgresoMisionDTO?
        )
    }

    private fun obtenerInsigniasMision(misionId: Long): List<InsigniaDTO> {
        val relacionInsignias = misionInsigniaRepository.findByMisionId(misionId)
        return relacionInsignias.mapNotNull { relacion ->
            insigniaRepository.findById(relacion.insigniaId).orElse(null)?.let { insignia ->
                InsigniaDTO(
                    id = insignia.id!!,
                    codigo = insignia.codigo,
                    nombre = insignia.nombre,
                    nombreKichwa = insignia.nombreKichwa,
                    descripcion = insignia.descripcion,
                    icono = insignia.icono,
                    rareza = insignia.rareza,
                    fechaObtencion = null,
                    obtenida = false
                )
            }
        }
    }

    // ========== DETALLE DE MISIÓN ==========

    @Transactional(readOnly = true)
    fun obtenerDetalleMision(misionId: Long, usuarioId: Long): DetalleMisionResponse {
        logger.info("📖 Obteniendo detalle de misión {} para usuario {}", misionId, usuarioId)

        val mision = misionRepository.findById(misionId)
            .orElseThrow { IllegalArgumentException("Misión no encontrada") }

        val fases = faseMisionRepository.findByMisionIdOrderByNumeroFase(misionId)
        val progresoMision = usuarioMisionRepository.findByUsuarioIdAndMisionId(usuarioId, misionId)
        val progreso = progresoExploracionRepository.findByUsuarioId(usuarioId)

        val nivelUsuario = progreso?.nivelArqueologo ?: 1
        val misionesCompletadas = usuarioMisionRepository
            .findByUsuarioIdAndEstado(usuarioId, EstadoMision.COMPLETADA)
            .map { it.misionId }

        val estado = determinarEstadoMision(mision, progresoMision, nivelUsuario, misionesCompletadas)
        val puedeIniciar = estado == EstadoMision.DISPONIBLE || estado == EstadoMision.EN_PROGRESO

        val motivoBloqueo = if (!puedeIniciar) {
            when {
                nivelUsuario < mision.nivelMinimo ->
                    "Necesitas nivel ${mision.nivelMinimo} de arqueólogo"
                !mision.misionesPrevias.isNullOrEmpty() ->
                    "Debes completar misiones previas"
                else -> "Requisitos no cumplidos"
            }
        } else null

        return DetalleMisionResponse(
            mision = convertirAMisionDetalle(mision),
            fases = fases.map { convertirAFaseDTO(it, progresoMision) },
            progreso = (if (progresoMision != null) {
                com.tesis.gamificacion.dto.misiones.ProgresoMisionDTO(
                    faseActual = progresoMision.faseActual,
                    totalFases = fases.size,
                    puntuacion = progresoMision.puntuacion,
                    intentos = progresoMision.intentos,
                    respuestasCorrectas = progresoMision.respuestasCorrectas,
                    respuestasIncorrectas = progresoMision.respuestasIncorrectas,
                    porcentajeCompletado = (progresoMision.faseActual.toDouble() / fases.size) * 100
                )
            } else null) as ProgresoMisionDTO?,
            puedeIniciar = puedeIniciar,
            motivoBloqueo = motivoBloqueo
        )
    }

    private fun convertirAMisionDetalle(mision: Mision): MisionDetalleDTO {
        val insignias = obtenerInsigniasMision(mision.id!!)

        return MisionDetalleDTO(
            id = mision.id!!,
            titulo = mision.titulo,
            tituloKichwa = mision.tituloKichwa,
            descripcionCorta = mision.descripcionCorta,
            descripcionLarga = mision.descripcionLarga,
            imagenPortada = mision.imagenPortada,
            dificultad = mision.dificultad,
            tiempoEstimado = mision.tiempoEstimado,
            npcGuia = NPCGuiaDTO(
                nombre = mision.npcNombre,
                nombreKichwa = mision.npcNombreKichwa,
                avatar = mision.npcAvatar
            ),
            npcDialogoInicial = mision.npcDialogoInicial,
            recompensas = RecompensasDTO(
                experiencia = mision.experienciaRecompensa,
                puntos = mision.puntosRecompensa,
                insignias = insignias
            ),
            requisitos = RequisitosDTO(
                nivelMinimo = mision.nivelMinimo,
                misionesPrevias = if (mision.misionesPrevias != null)
                    objectMapper.readValue(mision.misionesPrevias!!) else null,
                insignias = if (mision.insigniasRequeridas != null)
                    objectMapper.readValue(mision.insigniasRequeridas!!) else null
            )
        )
    }

    private fun convertirAFaseDTO(fase: FaseMision, progreso: UsuarioMision?): FaseDTO {
        val completada = if (progreso != null && progreso.progresoFases != null) {
            val fases: Map<String, String> = objectMapper.readValue(progreso.progresoFases!!)
            fases[fase.numeroFase.toString()] == "completada"
        } else false

        return FaseDTO(
            id = fase.id!!,
            numeroFase = fase.numeroFase,
            titulo = fase.titulo,
            descripcion = fase.descripcion,
            tipoFase = fase.tipoFase,
            puntoInteresId = fase.puntoInteresId,
            experienciaFase = fase.experienciaFase,
            completada = completada
        )
    }

    // ========== INICIAR MISIÓN ==========

    @Transactional
    fun iniciarMision(misionId: Long, usuarioId: Long): IniciarMisionResponse {
        logger.info("🎬 Usuario {} iniciando misión {}", usuarioId, misionId)

        val mision = misionRepository.findById(misionId)
            .orElseThrow { IllegalArgumentException("Misión no encontrada") }

        // Verificar que no esté ya iniciada
        val progresoExistente = usuarioMisionRepository.findByUsuarioIdAndMisionId(usuarioId, misionId)
        if (progresoExistente != null && progresoExistente.estado == EstadoMision.EN_PROGRESO) {
            throw IllegalArgumentException("Ya tienes esta misión en progreso")
        }

        // Verificar requisitos
        val progreso = progresoExploracionRepository.findByUsuarioId(usuarioId)
        val nivelUsuario = progreso?.nivelArqueologo ?: 1

        if (nivelUsuario < mision.nivelMinimo) {
            throw IllegalArgumentException("Nivel insuficiente. Necesitas nivel ${mision.nivelMinimo}")
        }

        // Crear nuevo progreso
        val usuarioMision = usuarioMisionRepository.save(
            UsuarioMision(
                usuarioId = usuarioId,
                misionId = misionId,
                estado = EstadoMision.EN_PROGRESO,
                faseActual = 1,
                progresoFases = "{}"
            )
        )

        // Obtener primera fase
        val primeraFase = faseMisionRepository.findByMisionIdAndNumeroFase(misionId, 1)
            ?: throw IllegalStateException("La misión no tiene fases configuradas")

        val faseEjecucion = construirFaseEjecucion(primeraFase)

        logger.info("✅ Misión {} iniciada para usuario {}", misionId, usuarioId)

        return IniciarMisionResponse(
            usuarioMisionId = usuarioMision.id!!,
            misionId = misionId,
            faseActual = faseEjecucion,
            mensaje = "¡Misión iniciada! Buena suerte, explorador."
        )
    }

// ========== OBTENER FASE ACTUAL ==========

    @Transactional(readOnly = true)
    fun obtenerFaseActual(usuarioMisionId: Long): FaseEjecucionDTO {
        logger.info("📄 Obteniendo fase actual para usuarioMision: {}", usuarioMisionId)

        val usuarioMision = usuarioMisionRepository.findById(usuarioMisionId)
            .orElseThrow { IllegalArgumentException("Progreso de misión no encontrado") }

        val fase = faseMisionRepository.findByMisionIdAndNumeroFase(
            usuarioMision.misionId,
            usuarioMision.faseActual
        ) ?: throw IllegalStateException("Fase no encontrada")

        return construirFaseEjecucion(fase)
    }

// ========== RESPONDER FASE ==========

    @Transactional
    fun responderFase(request: ResponderFaseRequest): ResponderFaseResponse {
        logger.info("📝 Procesando respuesta de fase")

        val usuarioMision = usuarioMisionRepository.findById(request.usuarioMisionId)
            .orElseThrow { IllegalArgumentException("Progreso de misión no encontrado") }

        val fase = faseMisionRepository.findById(request.faseId)
            .orElseThrow { IllegalArgumentException("Fase no encontrada") }

        return when (fase.tipoFase) {
            TipoFase.QUIZ -> procesarQuiz(request, usuarioMision, fase)
            TipoFase.VISITAR_PUNTO -> procesarVisitaPunto(request, usuarioMision, fase)
            TipoFase.BUSCAR_ARTEFACTO -> procesarBuscarArtefacto(request, usuarioMision, fase)
            TipoFase.EXPLORACION_LIBRE -> procesarExploracionLibre(request, usuarioMision, fase)
            TipoFase.DIALOGO -> procesarDialogo(usuarioMision, fase)
            TipoFase.DECISION -> procesarDecision(request, usuarioMision, fase)
        }
    }

// ========== PROCESAR QUIZ ==========

    private fun procesarQuiz(
        request: ResponderFaseRequest,
        usuarioMision: UsuarioMision,
        fase: FaseMision
    ): ResponderFaseResponse {
        if (request.respuestas.isNullOrEmpty()) {
            throw IllegalArgumentException("Debes proporcionar respuestas")
        }

        val preguntas = preguntaFaseRepository.findByFaseIdOrderByOrden(fase.id!!)
        val retroalimentacion = mutableListOf<RetroalimentacionDTO>()

        var correctas = 0
        var puntuacionFase = 0

        request.respuestas.forEach { respuesta ->
            val pregunta = preguntas.find { it.id == respuesta.preguntaId }
                ?: throw IllegalArgumentException("Pregunta no encontrada")

            val esCorrecta = pregunta.respuestaCorrecta == respuesta.respuesta

            if (esCorrecta) {
                correctas++
                puntuacionFase += pregunta.puntos
            }

            retroalimentacion.add(
                RetroalimentacionDTO(
                    pregunta = pregunta.pregunta,
                    respuestaUsuario = respuesta.respuesta,
                    respuestaCorrecta = pregunta.respuestaCorrecta,
                    esCorrecta = esCorrecta,
                    explicacion = if (esCorrecta)
                        pregunta.retroalimentacionCorrecta
                    else
                        pregunta.retroalimentacionIncorrecta
                )
            )
        }

        val incorrectas = request.respuestas.size - correctas

        // Actualizar progreso
        val nuevoProgreso = usuarioMision.copy(
            puntuacion = usuarioMision.puntuacion + puntuacionFase,
            intentos = usuarioMision.intentos + 1,
            respuestasCorrectas = usuarioMision.respuestasCorrectas + correctas,
            respuestasIncorrectas = usuarioMision.respuestasIncorrectas + incorrectas
        )

        return finalizarFase(nuevoProgreso, fase, puntuacionFase, retroalimentacion)
    }

// ========== PROCESAR VISITA A PUNTO ==========

    private fun procesarVisitaPunto(
        request: ResponderFaseRequest,
        usuarioMision: UsuarioMision,
        fase: FaseMision
    ): ResponderFaseResponse {
        if (request.puntoVisitadoId == null) {
            throw IllegalArgumentException("Debes visitar el punto de interés")
        }

        if (request.puntoVisitadoId != fase.puntoInteresId) {
            throw IllegalArgumentException("Debes visitar el punto correcto")
        }

        val puntuacionFase = 100
        val nuevoProgreso = usuarioMision.copy(
            puntuacion = usuarioMision.puntuacion + puntuacionFase
        )

        return finalizarFase(
            nuevoProgreso,
            fase,
            puntuacionFase,
            listOf(
                RetroalimentacionDTO(
                    pregunta = "Visita al punto de interés",
                    respuestaUsuario = "Completado",
                    respuestaCorrecta = "Completado",
                    esCorrecta = true,
                    explicacion = "¡Has visitado el punto correctamente!"
                )
            )
        )
    }

// ========== PROCESAR BÚSQUEDA DE ARTEFACTO ==========

    private fun procesarBuscarArtefacto(
        request: ResponderFaseRequest,
        usuarioMision: UsuarioMision,
        fase: FaseMision
    ): ResponderFaseResponse {
        if (request.artefactoEncontradoId == null) {
            throw IllegalArgumentException("Debes encontrar el artefacto")
        }

        // Aquí podrías validar que es el artefacto correcto según la configuración
        val puntuacionFase = 150
        val nuevoProgreso = usuarioMision.copy(
            puntuacion = usuarioMision.puntuacion + puntuacionFase
        )

        return finalizarFase(
            nuevoProgreso,
            fase,
            puntuacionFase,
            listOf(
                RetroalimentacionDTO(
                    pregunta = "Búsqueda de artefacto",
                    respuestaUsuario = "Encontrado",
                    respuestaCorrecta = "Encontrado",
                    esCorrecta = true,
                    explicacion = "¡Has encontrado el artefacto!"
                )
            )
        )
    }

// ========== PROCESAR EXPLORACIÓN LIBRE ==========

    private fun procesarExploracionLibre(
        request: ResponderFaseRequest,
        usuarioMision: UsuarioMision,
        fase: FaseMision
    ): ResponderFaseResponse {
        if (request.tiempoExploracion == null || request.tiempoExploracion < 60) {
            throw IllegalArgumentException("Debes explorar por al menos 1 minuto")
        }

        val puntuacionFase = 80
        val nuevoProgreso = usuarioMision.copy(
            puntuacion = usuarioMision.puntuacion + puntuacionFase
        )

        return finalizarFase(
            nuevoProgreso,
            fase,
            puntuacionFase,
            listOf(
                RetroalimentacionDTO(
                    pregunta = "Exploración libre",
                    respuestaUsuario = "Completado",
                    respuestaCorrecta = "Completado",
                    esCorrecta = true,
                    explicacion = "¡Has completado la exploración!"
                )
            )
        )
    }

// ========== PROCESAR DIÁLOGO ==========

    private fun procesarDialogo(
        usuarioMision: UsuarioMision,
        fase: FaseMision
    ): ResponderFaseResponse {
        val puntuacionFase = 50
        val nuevoProgreso = usuarioMision.copy(
            puntuacion = usuarioMision.puntuacion + puntuacionFase
        )

        return finalizarFase(
            nuevoProgreso,
            fase,
            puntuacionFase,
            listOf(
                RetroalimentacionDTO(
                    pregunta = "Diálogo",
                    respuestaUsuario = "Completado",
                    respuestaCorrecta = "Completado",
                    esCorrecta = true,
                    explicacion = "Diálogo completado"
                )
            )
        )
    }

// ========== PROCESAR DECISIÓN ==========

    private fun procesarDecision(
        request: ResponderFaseRequest,
        usuarioMision: UsuarioMision,
        fase: FaseMision
    ): ResponderFaseResponse {
        if (request.decisionId.isNullOrEmpty()) {
            throw IllegalArgumentException("Debes tomar una decisión")
        }

        val puntuacionFase = 75
        val nuevoProgreso = usuarioMision.copy(
            puntuacion = usuarioMision.puntuacion + puntuacionFase
        )

        return finalizarFase(
            nuevoProgreso,
            fase,
            puntuacionFase,
            listOf(
                RetroalimentacionDTO(
                    pregunta = "Decisión",
                    respuestaUsuario = request.decisionId,
                    respuestaCorrecta = request.decisionId,
                    esCorrecta = true,
                    explicacion = "Decisión tomada"
                )
            )
        )
    }

// ========== FINALIZAR FASE ==========

    private fun finalizarFase(
        usuarioMision: UsuarioMision,
        fase: FaseMision,
        puntuacionFase: Int,
        retroalimentacion: List<RetroalimentacionDTO>
    ): ResponderFaseResponse {
        // Actualizar progreso de fases
        val progresoFases: MutableMap<String, String> = if (usuarioMision.progresoFases != null) {
            objectMapper.readValue(usuarioMision.progresoFases!!)
        } else {
            mutableMapOf()
        }

        progresoFases[fase.numeroFase.toString()] = "completada"

        // Obtener siguiente fase
        val todasFases = faseMisionRepository.findByMisionIdOrderByNumeroFase(usuarioMision.misionId)
        val siguienteFase = todasFases.find { it.numeroFase == fase.numeroFase + 1 }

        val misionCompletada = siguienteFase == null

        // Actualizar usuario misión
        val actualizado = usuarioMision.copy(
            faseActual = siguienteFase?.numeroFase ?: fase.numeroFase,
            puntuacion = usuarioMision.puntuacion,
            progresoFases = objectMapper.writeValueAsString(progresoFases),
            estado = if (misionCompletada) EstadoMision.COMPLETADA else EstadoMision.EN_PROGRESO,
            tiempoCompletado = if (misionCompletada) LocalDateTime.now() else null
        )

        usuarioMisionRepository.save(actualizado)

        // Dar experiencia al usuario
        val expGanada = fase.experienciaFase
        val progreso = progresoExploracionRepository.findByUsuarioId(usuarioMision.usuarioId)
        if (progreso != null) {
            progreso.experienciaTotal += expGanada
            progresoExploracionRepository.save(progreso)
        }

        // Otorgar insignias si completó la misión
        val insignias = if (misionCompletada) {
            otorgarInsigniasMision(usuarioMision.usuarioId, usuarioMision.misionId)
        } else {
            emptyList()
        }

        return ResponderFaseResponse(
            faseCompletada = true,
            correctas = retroalimentacion.count { it.esCorrecta },
            incorrectas = retroalimentacion.count { !it.esCorrecta },
            puntuacion = puntuacionFase,
            experienciaGanada = expGanada,
            retroalimentacion = retroalimentacion,
            siguienteFase = siguienteFase?.let { construirFaseEjecucion(it) },
            misionCompletada = misionCompletada,
            insigniasObtenidas = insignias
        )
    }

// ========== OTORGAR INSIGNIAS ==========

    private fun otorgarInsigniasMision(usuarioId: Long, misionId: Long): List<InsigniaDTO> {
        val relacionInsignias = misionInsigniaRepository.findByMisionId(misionId)
        val insigniasOtorgadas = mutableListOf<InsigniaDTO>()

        relacionInsignias.forEach { relacion ->
            val insignia = insigniaRepository.findById(relacion.insigniaId).orElse(null)
            if (insignia != null) {
                // Verificar que no la tenga ya
                val yaLaTiene = usuarioInsigniaRepository
                    .findByUsuarioIdAndInsigniaId(usuarioId, insignia.id!!) != null

                if (!yaLaTiene) {
                    usuarioInsigniaRepository.save(
                        UsuarioInsignia(
                            usuarioId = usuarioId,
                            insigniaId = insignia.id!!,
                            misionId = misionId
                        )
                    )

                    insigniasOtorgadas.add(
                        InsigniaDTO(
                            id = insignia.id!!,
                            codigo = insignia.codigo,
                            nombre = insignia.nombre,
                            nombreKichwa = insignia.nombreKichwa,
                            descripcion = insignia.descripcion,
                            icono = insignia.icono,
                            rareza = insignia.rareza,
                            fechaObtencion = LocalDateTime.now(),
                            obtenida = true
                        )
                    )
                }
            }
        }

        return insigniasOtorgadas
    }

// ========== CONSTRUIR FASE EJECUCIÓN ==========

    private fun construirFaseEjecucion(fase: FaseMision): FaseEjecucionDTO {
        val contenido = when (fase.tipoFase) {
            TipoFase.DIALOGO -> {
                val config: Map<String, String> = if (fase.configuracion != null)
                    objectMapper.readValue(fase.configuracion!!) else emptyMap()

                ContenidoFaseDTO.DialogoContenido(
                    npcNombre = config["npcNombre"] ?: "Guía",
                    npcAvatar = config["npcAvatar"] ?: "/assets/npc/default.png",
                    dialogo = fase.descripcion
                )
            }
            TipoFase.QUIZ -> {
                val preguntas = preguntaFaseRepository.findByFaseIdOrderByOrden(fase.id!!)
                ContenidoFaseDTO.QuizContenido(
                    preguntas = preguntas.map { pregunta ->
                        PreguntaDTO(
                            id = pregunta.id!!,
                            pregunta = pregunta.pregunta,
                            opciones = listOf(
                                OpcionDTO("A", pregunta.opcionA),
                                OpcionDTO("B", pregunta.opcionB),
                                OpcionDTO("C", pregunta.opcionC),
                                OpcionDTO("D", pregunta.opcionD)
                            ),
                            puntos = pregunta.puntos
                        )
                    }
                )
            }
            TipoFase.VISITAR_PUNTO -> {
                ContenidoFaseDTO.VisitarPuntoContenido(
                    puntoInteresId = fase.puntoInteresId!!,
                    puntoNombre = "Punto de Interés",
                    instrucciones = fase.descripcion
                )
            }
            TipoFase.BUSCAR_ARTEFACTO -> {
                val config: Map<String, Any> = if (fase.configuracion != null)
                    objectMapper.readValue(fase.configuracion!!) else emptyMap()

                ContenidoFaseDTO.BuscarArtefactoContenido(
                    artefactoId = (config["artefactoId"] as? Number)?.toLong() ?: 1L,
                    artefactoNombre = config["artefactoNombre"] as? String ?: "Artefacto",
                    puntoInteresId = fase.puntoInteresId!!,
                    pista = fase.descripcion
                )
            }
            TipoFase.EXPLORACION_LIBRE -> {
                ContenidoFaseDTO.ExploracionLibreContenido(
                    tiempoRequerido = 120,
                    areaSugerida = fase.descripcion
                )
            }
            TipoFase.DECISION -> {
                val config: Map<String, Any> = if (fase.configuracion != null)
                    objectMapper.readValue(fase.configuracion!!) else emptyMap()

                @Suppress("UNCHECKED_CAST")
                val opciones = (config["opciones"] as? List<Map<String, String>>) ?: emptyList()

                ContenidoFaseDTO.DecisionContenido(
                    situacion = fase.descripcion,
                    opciones = opciones.map {
                        OpcionDecisionDTO(
                            id = it["id"] ?: "",
                            texto = it["texto"] ?: "",
                            consecuencia = it["consecuencia"] ?: ""
                        )
                    }
                )
            }
        }

        return FaseEjecucionDTO(
            numeroFase = fase.numeroFase,
            titulo = fase.titulo,
            descripcion = fase.descripcion,
            tipoFase = fase.tipoFase,
            contenido = contenido
        )
    }

    // ========== COLECCIÓN DE INSIGNIAS ==========

    @Transactional(readOnly = true)
    fun obtenerColeccionInsignias(usuarioId: Long): ColeccionInsigniasResponse {
        logger.info("🏆 Obteniendo colección de insignias - Usuario: {}", usuarioId)

        val todasInsignias = insigniaRepository.findByActivaTrue()
        val insigniasUsuario = usuarioInsigniaRepository.findByUsuarioId(usuarioId)

        val insigniasDTO = todasInsignias.map { insignia ->
            val usuarioInsignia = insigniasUsuario.find { it.insigniaId == insignia.id }

            InsigniaDTO(
                id = insignia.id!!,
                codigo = insignia.codigo,
                nombre = insignia.nombre,
                nombreKichwa = insignia.nombreKichwa,
                descripcion = insignia.descripcion,
                icono = insignia.icono,
                rareza = insignia.rareza,
                fechaObtencion = usuarioInsignia?.fechaObtencion,
                obtenida = usuarioInsignia != null
            )
        }

        return ColeccionInsigniasResponse(
            insignias = insigniasDTO,
            totalObtenidas = insigniasUsuario.size,
            totalDisponibles = todasInsignias.size,
            porcentajeCompletado = if (todasInsignias.isNotEmpty())
                (insigniasUsuario.size.toDouble() / todasInsignias.size) * 100
            else 0.0
        )
    }

    // ========== OBTENER FASE ACTUAL PARA EJECUCIÓN ==========

    @Transactional(readOnly = true)
    fun obtenerFaseActualEjecucion(usuarioMisionId: Long): FaseEjecucionDTO {
        logger.info("📄 Obteniendo fase actual - UsuarioMision: {}", usuarioMisionId)

        val usuarioMision = usuarioMisionRepository.findById(usuarioMisionId)
            .orElseThrow { IllegalArgumentException("Progreso no encontrado") }

        if (usuarioMision.estado != EstadoMision.EN_PROGRESO) {
            throw IllegalStateException("La misión no está en progreso")
        }

        val fase = faseMisionRepository.findByMisionIdAndNumeroFase(
            usuarioMision.misionId,
            usuarioMision.faseActual
        ) ?: throw IllegalStateException("Fase no encontrada")

        return construirFaseEjecucion(fase)
    }

// ========== RESPONDER FASE QUIZ ==========

    @Transactional
    fun responderFaseQuiz(
        usuarioMisionId: Long,
        faseId: Long,
        respuestas: List<RespuestaDTO>
    ): ResponderFaseResponse {
        logger.info("📝 Respondiendo quiz - UsuarioMision: {}, Fase: {}", usuarioMisionId, faseId)

        val usuarioMision = usuarioMisionRepository.findById(usuarioMisionId)
            .orElseThrow { IllegalArgumentException("Progreso no encontrado") }

        val fase = faseMisionRepository.findById(faseId)
            .orElseThrow { IllegalArgumentException("Fase no encontrada") }

        if (fase.tipoFase != TipoFase.QUIZ) {
            throw IllegalArgumentException("Esta fase no es de tipo QUIZ")
        }

        val preguntas = preguntaFaseRepository.findByFaseIdOrderByOrden(faseId)
        val retroalimentacion = mutableListOf<RetroalimentacionDTO>()

        var correctas = 0
        var puntuacionFase = 0

        respuestas.forEach { respuesta ->
            val pregunta = preguntas.find { it.id == respuesta.preguntaId }
                ?: throw IllegalArgumentException("Pregunta no encontrada: ${respuesta.preguntaId}")

            val esCorrecta = pregunta.respuestaCorrecta == respuesta.respuesta

            if (esCorrecta) {
                correctas++
                puntuacionFase += pregunta.puntos
            }

            retroalimentacion.add(
                RetroalimentacionDTO(
                    pregunta = pregunta.pregunta,
                    respuestaUsuario = respuesta.respuesta,
                    respuestaCorrecta = pregunta.respuestaCorrecta,
                    esCorrecta = esCorrecta,
                    explicacion = if (esCorrecta)
                        pregunta.retroalimentacionCorrecta
                    else
                        pregunta.retroalimentacionIncorrecta
                )
            )
        }

        val incorrectas = respuestas.size - correctas

        // Actualizar progreso
        val nuevoProgreso = usuarioMision.copy(
            puntuacion = usuarioMision.puntuacion + puntuacionFase,
            intentos = usuarioMision.intentos + 1,
            respuestasCorrectas = usuarioMision.respuestasCorrectas + correctas,
            respuestasIncorrectas = usuarioMision.respuestasIncorrectas + incorrectas
        )

        return finalizarFase(nuevoProgreso, fase, puntuacionFase, retroalimentacion)
    }

// ========== AVANZAR A SIGUIENTE FASE ==========

    @Transactional
    fun avanzarFase(usuarioMisionId: Long): FaseEjecucionDTO? {
        logger.info("➡️ Avanzando fase - UsuarioMision: {}", usuarioMisionId)

        val usuarioMision = usuarioMisionRepository.findById(usuarioMisionId)
            .orElseThrow { IllegalArgumentException("Progreso no encontrado") }

        val todasFases = faseMisionRepository.findByMisionIdOrderByNumeroFase(usuarioMision.misionId)
        val siguienteNumero = usuarioMision.faseActual + 1
        val siguienteFase = todasFases.find { it.numeroFase == siguienteNumero }

        if (siguienteFase == null) {
            // Misión completada
            completarMision(usuarioMision)
            return null
        }

        // Actualizar a siguiente fase
        val actualizado = usuarioMision.copy(
            faseActual = siguienteNumero
        )

        usuarioMisionRepository.save(actualizado)

        return construirFaseEjecucion(siguienteFase)
    }

// ========== COMPLETAR MISIÓN ==========

    @Transactional
    fun completarMision(usuarioMision: UsuarioMision) {
        logger.info("🎉 Completando misión - UsuarioMision: {}", usuarioMision.id)

        val mision = misionRepository.findById(usuarioMision.misionId)
            .orElseThrow { IllegalArgumentException("Misión no encontrada") }

        // Actualizar estado
        val completada = usuarioMision.copy(
            estado = EstadoMision.COMPLETADA,
            tiempoCompletado = LocalDateTime.now()
        )

        usuarioMisionRepository.save(completada)

        // Dar experiencia al usuario
        val progreso = progresoExploracionRepository.findByUsuarioId(usuarioMision.usuarioId)
        if (progreso != null) {
            progreso.experienciaTotal += mision.experienciaRecompensa

            // Calcular nuevo nivel
            val nuevoNivel = (progreso.experienciaTotal / 1000) + 1
            progreso.nivelArqueologo = nuevoNivel

            progresoExploracionRepository.save(progreso)
        }

        // Otorgar insignias
        otorgarInsigniasMision(usuarioMision.usuarioId, usuarioMision.misionId)

        logger.info("✅ Misión completada - Usuario: {}, XP: {}",
            usuarioMision.usuarioId, mision.experienciaRecompensa)
    }

    // ========== RESPONDER PREGUNTA ÚNICA (TU ESTRUCTURA) ==========

    @Transactional
    fun responderFaseQuizUnico(
        usuarioMisionId: Long,
        preguntaId: Long,
        respuesta: String
    ): ResponderFaseResponse {
        logger.info("📝 Respondiendo pregunta única - UsuarioMision: {}, Pregunta: {}", usuarioMisionId, preguntaId)

        val usuarioMision = usuarioMisionRepository.findById(usuarioMisionId)
            .orElseThrow { IllegalArgumentException("Progreso no encontrado") }

        val pregunta = preguntaFaseRepository.findById(preguntaId)
            .orElseThrow { IllegalArgumentException("Pregunta no encontrada") }

        val fase = faseMisionRepository.findById(pregunta.faseId)
            .orElseThrow { IllegalArgumentException("Fase no encontrada") }

        if (fase.tipoFase != TipoFase.QUIZ) {
            throw IllegalArgumentException("Esta fase no es de tipo QUIZ")
        }

        // Verificar respuesta
        val esCorrecta = pregunta.respuestaCorrecta == respuesta
        val puntuacionFase = if (esCorrecta) pregunta.puntos else 0

        val retroalimentacion = listOf(
            RetroalimentacionDTO(
                pregunta = pregunta.pregunta,
                respuestaUsuario = respuesta,
                respuestaCorrecta = pregunta.respuestaCorrecta,
                esCorrecta = esCorrecta,
                explicacion = if (esCorrecta)
                    pregunta.retroalimentacionCorrecta
                else
                    pregunta.retroalimentacionIncorrecta
            )
        )

        // Actualizar progreso
        val nuevoProgreso = usuarioMision.copy(
            puntuacion = usuarioMision.puntuacion + puntuacionFase,
            intentos = usuarioMision.intentos + 1,
            respuestasCorrectas = if (esCorrecta) usuarioMision.respuestasCorrectas + 1 else usuarioMision.respuestasCorrectas,
            respuestasIncorrectas = if (!esCorrecta) usuarioMision.respuestasIncorrectas + 1 else usuarioMision.respuestasIncorrectas
        )

        return finalizarFase(nuevoProgreso, fase, puntuacionFase, retroalimentacion)
    }
}