package com.tesis.gamificacion.service

import com.tesis.gamificacion.dto.request.*
import com.tesis.gamificacion.dto.response.*
import com.tesis.gamificacion.model.entities.*
import com.tesis.gamificacion.model.enums.*
import com.tesis.gamificacion.repository.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.random.Random

@Service
class ExploracionService(
    private val puntoInteresRepository: PuntoInteresRepository,
    private val progresoRepository: ProgresoExploracionRepository,
    private val descubrimientoRepository: DescubrimientoRepository,
    private val artefactoRepository: ArtefactoRepository,
    private val usuarioArtefactoRepository: UsuarioArtefactoRepository,
    private val misionRepository: MisionExploracionRepository,
    private val usuarioMisionRepository: UsuarioMisionRepository,
    private val preguntaQuizRepository: PreguntaQuizRepository
) {
    private val logger = LoggerFactory.getLogger(ExploracionService::class.java)

    @Transactional
    fun obtenerDashboard(usuarioId: Long): DashboardExploracionResponse {
        logger.info("📊 Obteniendo dashboard de exploración para usuario: {}", usuarioId)

        val progreso = obtenerOCrearProgreso(usuarioId)
        val todosPuntos = puntoInteresRepository.findByActivoTrue()
        val descubrimientos = descubrimientoRepository.findByUsuarioId(usuarioId)
        val artefactosUsuario = usuarioArtefactoRepository.findByUsuarioId(usuarioId)
        val misiones = obtenerMisionesActivas(usuarioId)

        val puntosDescubiertos = todosPuntos.filter { punto ->
            descubrimientos.any { it.puntoId == punto.id }
        }.map { convertirAPuntoDTO(it, descubrimientos, artefactosUsuario, progreso) }

        val puntosDisponibles = todosPuntos.filter { punto ->
            punto.nivelRequerido <= progreso.nivelArqueologo &&
                    descubrimientos.none { it.puntoId == punto.id }
        }.map { convertirAPuntoDTO(it, descubrimientos, artefactosUsuario, progreso) }

        val artefactosRecientes = obtenerArtefactosRecientes(usuarioId, 5)
        val estadisticas = calcularEstadisticas(usuarioId, descubrimientos)

        return DashboardExploracionResponse(
            progreso = convertirAProgresoDTO(progreso, todosPuntos.size),
            puntosDescubiertos = puntosDescubiertos,
            puntosDisponibles = puntosDisponibles,
            misionesActivas = misiones,
            artefactosRecientes = artefactosRecientes,
            estadisticas = estadisticas
        )
    }

    @Transactional
    fun visitarPunto(request: VisitarPuntoRequest): VisitaPuntoResponse {
        logger.info("🗿 Usuario {} visitando punto {}", request.usuarioId, request.puntoId)

        val punto = puntoInteresRepository.findById(request.puntoId)
            .orElseThrow { IllegalArgumentException("Punto no encontrado") }

        val progreso = obtenerOCrearProgreso(request.usuarioId)

        // Verificar nivel requerido
        if (punto.nivelRequerido > progreso.nivelArqueologo) {
            throw IllegalArgumentException("Nivel de arqueólogo insuficiente")
        }

        // Verificar si es primera visita
        val descubrimientoExistente = descubrimientoRepository
            .findByUsuarioIdAndPuntoId(request.usuarioId, request.puntoId)

        val esPrimeraVisita = descubrimientoExistente == null

        // Crear o actualizar descubrimiento
        val descubrimiento = if (esPrimeraVisita) {
            crearNuevoDescubrimiento(request.usuarioId, request.puntoId, request.tiempoSegundos)
        } else {
            actualizarDescubrimiento(descubrimientoExistente!!, request.tiempoSegundos)
        }

        // Buscar artefacto (probabilidad)
        val artefactoEncontrado = buscarArtefactoAleatorio(request.usuarioId, request.puntoId)

        // Calcular experiencia
        var expGanada = punto.puntosPorDescubrir
        if (artefactoEncontrado != null) {
            expGanada += artefactoEncontrado.rareza * 50
        }

        // Actualizar progreso
        val nivelAnterior = progreso.nivelArqueologo
        progreso.experienciaTotal += expGanada
        progreso.ultimaVisita = LocalDateTime.now()

        // 👇 ARREGLADO: Solo incrementar si es primera visita
        if (esPrimeraVisita) {
            progreso.puntosDescubiertos += 1
        }

        // 👇 NUEVO: Incrementar artefactos encontrados si encontró uno
        if (artefactoEncontrado != null) {
            progreso.artefactosEncontrados += 1
        }

        val nuevoNivel = calcularNivel(progreso.experienciaTotal)
        val nivelSubido = nuevoNivel > nivelAnterior

        if (nivelSubido) {
            progreso.nivelArqueologo = nuevoNivel
            logger.info("🎉 Usuario {} subió al nivel {}", request.usuarioId, nuevoNivel)
        }

        progresoRepository.save(progreso)

        // Actualizar misiones
        val misionesActualizadas = actualizarMisiones(request.usuarioId, request.puntoId)

        return VisitaPuntoResponse(
            descubrimiento = convertirADescubrimientoDTO(descubrimiento, punto, esPrimeraVisita),
            artefactoEncontrado = artefactoEncontrado?.let {
                convertirAArtefactoDTO(it, true, LocalDateTime.now(), 1)
            },
            experienciaGanada = expGanada,
            nivelSubido = nivelSubido,
            nuevoNivel = if (nivelSubido) nuevoNivel else null,
            misionesActualizadas = misionesActualizadas
        )
    }

    // 👇 ACTUALIZAR este método también
    private fun convertirADescubrimientoDTO(
        descubrimiento: Descubrimiento,
        punto: PuntoInteres,
        esPrimeraVisita: Boolean
    ): DescubrimientoDTO {
        val nivelAnterior = when {
            esPrimeraVisita -> NivelDescubrimiento.NO_VISITADO
            descubrimiento.nivelDescubrimiento == NivelDescubrimiento.PLATA -> NivelDescubrimiento.BRONCE
            descubrimiento.nivelDescubrimiento == NivelDescubrimiento.ORO -> NivelDescubrimiento.PLATA
            else -> NivelDescubrimiento.NO_VISITADO
        }

        return DescubrimientoDTO(
            puntoId = punto.id!!,
            nombrePunto = punto.nombre,
            nivelDescubrimiento = descubrimiento.nivelDescubrimiento,
            nivelAnterior = nivelAnterior,
            visitas = descubrimiento.visitas,
            tiempoTotal = descubrimiento.tiempoExplorado,
            quizCompletado = descubrimiento.quizCompletado
        )
    }

    private fun obtenerOCrearProgreso(usuarioId: Long): ProgresoExploracion {
        return progresoRepository.findByUsuarioId(usuarioId)
            ?: progresoRepository.save(ProgresoExploracion(usuarioId = usuarioId))
    }

    private fun crearNuevoDescubrimiento(usuarioId: Long, puntoId: Long, tiempo: Int): Descubrimiento {
        return descubrimientoRepository.save(
            Descubrimiento(
                usuarioId = usuarioId,
                puntoId = puntoId,
                nivelDescubrimiento = NivelDescubrimiento.BRONCE,
                visitas = 1,
                tiempoExplorado = tiempo
            )
        )
    }

    private fun actualizarDescubrimiento(descubrimiento: Descubrimiento, tiempoAdicional: Int): Descubrimiento {
        val nuevoDescubrimiento = descubrimiento.copy(
            visitas = descubrimiento.visitas + 1,
            tiempoExplorado = descubrimiento.tiempoExplorado + tiempoAdicional,
            fechaUltimaVisita = LocalDateTime.now(),
            nivelDescubrimiento = when {
                descubrimiento.visitas >= 3 && descubrimiento.quizCompletado -> NivelDescubrimiento.ORO
                descubrimiento.visitas >= 2 && descubrimiento.quizCompletado -> NivelDescubrimiento.PLATA
                else -> descubrimiento.nivelDescubrimiento
            }
        )
        return descubrimientoRepository.save(nuevoDescubrimiento)
    }

    private fun buscarArtefactoAleatorio(usuarioId: Long, puntoId: Long): Artefacto? {
        val artefactosDisponibles = artefactoRepository.findByPuntoInteresIdAndActivoTrue(puntoId)
        val artefactosYaEncontrados = usuarioArtefactoRepository.findByUsuarioId(usuarioId)
            .map { it.artefactoId }

        val artefactosPorEncontrar = artefactosDisponibles.filter { it.id !in artefactosYaEncontrados }

        if (artefactosPorEncontrar.isEmpty()) {
            logger.info("📦 Usuario {} ya encontró todos los artefactos del punto {}", usuarioId, puntoId)
            return null
        }

        // Buscar basado en probabilidad
        val artefactoEncontrado = artefactosPorEncontrar.firstOrNull {
            Random.nextDouble() < it.probabilidadEncuentro
        }

        if (artefactoEncontrado != null) {
            // 👇 GUARDAR la relación usuario-artefacto
            usuarioArtefactoRepository.save(
                UsuarioArtefacto(
                    usuarioId = usuarioId,
                    artefactoId = artefactoEncontrado.id!!
                )
            )
            logger.info("✨ Usuario {} encontró artefacto: {}", usuarioId, artefactoEncontrado.nombre)
        }

        return artefactoEncontrado
    }

    private fun calcularNivel(experiencia: Int): Int {
        return (experiencia / 1000) + 1
    }

    private fun convertirAPuntoDTO(
        punto: PuntoInteres,
        descubrimientos: List<Descubrimiento>,
        artefactos: List<UsuarioArtefacto>,
        progreso: ProgresoExploracion
    ): PuntoInteresDTO {
        val descubrimiento = descubrimientos.find { it.puntoId == punto.id }

        // 👇 ARREGLADO: Obtener todos los artefactos del punto
        val todosArtefactosPunto = artefactoRepository.findByPuntoInteresIdAndActivoTrue(punto.id!!)
        val artefactosDisponibles = todosArtefactosPunto.size

        // 👇 ARREGLADO: Contar los que el usuario encontró de ESTE punto
        val artefactosEncontrados = todosArtefactosPunto.count { artefactoPunto ->
            artefactos.any { ua -> ua.artefactoId == artefactoPunto.id }
        }

        return PuntoInteresDTO(
            id = punto.id!!,
            nombre = punto.nombre,
            nombreKichwa = punto.nombreKichwa,
            descripcion = punto.descripcion,
            imagenUrl = punto.imagenUrl,
            coordenadaX = punto.coordenadaX,
            coordenadaY = punto.coordenadaY,
            categoria = punto.categoria,
            nivelRequerido = punto.nivelRequerido,
            puntosPorDescubrir = punto.puntosPorDescubrir,
            desbloqueado = punto.nivelRequerido <= progreso.nivelArqueologo,
            visitado = descubrimiento != null,
            nivelDescubrimiento = descubrimiento?.nivelDescubrimiento ?: NivelDescubrimiento.NO_VISITADO,
            visitas = descubrimiento?.visitas ?: 0,
            tiempoExplorado = descubrimiento?.tiempoExplorado ?: 0,
            quizCompletado = descubrimiento?.quizCompletado ?: false,
            artefactosDisponibles = artefactosDisponibles,
            artefactosEncontrados = artefactosEncontrados  // 👈 AHORA CORRECTO
        )
    }

    private fun convertirAProgresoDTO(progreso: ProgresoExploracion, totalPuntos: Int): ProgresoExploracionDTO {
        val expParaSiguiente = progreso.nivelArqueologo * 1000
        val expEnNivelActual = progreso.experienciaTotal % 1000
        val porcentaje = (expEnNivelActual.toDouble() / 1000) * 100

        return ProgresoExploracionDTO(
            nivelArqueologo = progreso.nivelArqueologo,
            experienciaActual = progreso.experienciaTotal,
            experienciaParaSiguienteNivel = expParaSiguiente,
            porcentajeProgreso = porcentaje,
            puntosDescubiertos = progreso.puntosDescubiertos,
            totalPuntos = totalPuntos,
            artefactosEncontrados = progreso.artefactosEncontrados,
            totalArtefactos = artefactoRepository.countByActivoTrue().toInt(),
            misionesCompletadas = progreso.misionesCompletadas
        )
    }

    private fun convertirADescubrimientoDTO(descubrimiento: Descubrimiento, punto: PuntoInteres): DescubrimientoDTO {
        return DescubrimientoDTO(
            puntoId = punto.id!!,
            nombrePunto = punto.nombre,
            nivelDescubrimiento = descubrimiento.nivelDescubrimiento,
            nivelAnterior = NivelDescubrimiento.NO_VISITADO,
            visitas = descubrimiento.visitas,
            tiempoTotal = descubrimiento.tiempoExplorado,
            quizCompletado = descubrimiento.quizCompletado
        )
    }

    private fun convertirAArtefactoDTO(
        artefacto: Artefacto,
        encontrado: Boolean,
        fechaEncontrado: LocalDateTime?,
        cantidad: Int
    ): ArtefactoDTO {
        val punto = puntoInteresRepository.findById(artefacto.puntoInteresId).get()
        return ArtefactoDTO(
            id = artefacto.id!!,
            nombre = artefacto.nombre,
            nombreKichwa = artefacto.nombreKichwa,
            descripcion = artefacto.descripcion,
            imagenUrl = artefacto.imagenUrl,
            categoria = artefacto.categoria,
            rareza = artefacto.rareza,
            encontrado = encontrado,
            fechaEncontrado = fechaEncontrado,
            cantidad = cantidad,
            puntoInteres = punto.nombre
        )
    }

    private fun obtenerMisionesActivas(usuarioId: Long): List<MisionDTO> {
        // TODO: Implementar lógica de misiones
        return emptyList()
    }

    private fun obtenerArtefactosRecientes(usuarioId: Long, limit: Int): List<ArtefactoDTO> {
        return usuarioArtefactoRepository.findByUsuarioId(usuarioId)
            .sortedByDescending { it.fechaEncontrado }
            .take(limit)
            .mapNotNull { ua ->
                artefactoRepository.findById(ua.artefactoId).orElse(null)?.let {
                    convertirAArtefactoDTO(it, true, ua.fechaEncontrado, ua.cantidad)
                }
            }
    }

    private fun calcularEstadisticas(usuarioId: Long, descubrimientos: List<Descubrimiento>): EstadisticasExploracionDTO {
        // TODO: Implementar cálculo completo
        return EstadisticasExploracionDTO(
            tiempoTotalExploracion = descubrimientos.sumOf { it.tiempoExplorado } / 60,
            visitasTotales = descubrimientos.sumOf { it.visitas },
            quizzesRespondidos = 0,
            quizzesCorrectos = 0,
            tasaAcierto = 0.0,
            artefactosPorCategoria = emptyMap(),
            puntosFavorito = null
        )
    }

    private fun actualizarMisiones(usuarioId: Long, puntoId: Long): List<MisionDTO> {
        // TODO: Implementar actualización de misiones
        return emptyList()
    }

    @Transactional(readOnly = true)
    fun obtenerDetallePunto(puntoId: Long, usuarioId: Long): DetallePuntoResponse {
        logger.info("📖 Obteniendo detalle del punto: {}", puntoId)

        val punto = puntoInteresRepository.findById(puntoId)
            .orElseThrow { IllegalArgumentException("Punto no encontrado") }

        val descubrimientos = descubrimientoRepository.findByUsuarioId(usuarioId)
        val artefactosUsuario = usuarioArtefactoRepository.findByUsuarioId(usuarioId)
        val progreso = obtenerOCrearProgreso(usuarioId)

        val puntoDTO = convertirAPuntoDTO(punto, descubrimientos, artefactosUsuario, progreso)

        val descubrimiento = descubrimientos.find { it.puntoId == puntoId }
        val nivel = descubrimiento?.nivelDescubrimiento ?: NivelDescubrimiento.NO_VISITADO

        val narrativa = NarrativaDTO(
            texto = generarNarrativa(punto, nivel),
            nivel = nivel,
            generadaPorIA = false
        )

        val preguntas = preguntaQuizRepository.findByPuntoInteresIdAndActivaTrue(puntoId)
            .take(3)
            .map { convertirAPreguntaDTO(it) }

        val artefactosDisponibles = artefactoRepository.findByPuntoInteresIdAndActivoTrue(puntoId)
            .map { artefacto ->
                val encontrado = artefactosUsuario.any { it.artefactoId == artefacto.id }
                val ua = artefactosUsuario.find { it.artefactoId == artefacto.id }
                convertirAArtefactoDTO(artefacto, encontrado, ua?.fechaEncontrado, ua?.cantidad ?: 0)
            }

        return DetallePuntoResponse(
            punto = puntoDTO,
            narrativa = narrativa,
            quiz = if (preguntas.isNotEmpty()) preguntas else null,
            artefactosDisponibles = artefactosDisponibles,
            historiaCompleta = punto.historiaDetallada
        )
    }

    @Transactional
    fun responderQuiz(request: ResponderQuizRequest): ResultadoQuizResponse {
        logger.info("📝 Respondiendo quiz - Punto: {}, Pregunta: {}", request.puntoId, request.preguntaId)

        val pregunta = preguntaQuizRepository.findById(request.preguntaId)
            .orElseThrow { IllegalArgumentException("Pregunta no encontrada") }

        val correcto = pregunta.respuestaCorrecta == request.respuesta
        var expGanada = 0

        if (correcto) {
            expGanada = pregunta.dificultad * 50

            // Actualizar descubrimiento
            val descubrimiento = descubrimientoRepository
                .findByUsuarioIdAndPuntoId(request.usuarioId, request.puntoId)

            if (descubrimiento != null && !descubrimiento.quizCompletado) {
                val actualizado = descubrimiento.copy(
                    quizCompletado = true,
                    nivelDescubrimiento = when (descubrimiento.nivelDescubrimiento) {
                        NivelDescubrimiento.BRONCE -> NivelDescubrimiento.PLATA
                        NivelDescubrimiento.PLATA -> NivelDescubrimiento.ORO
                        else -> descubrimiento.nivelDescubrimiento
                    }
                )
                descubrimientoRepository.save(actualizado)
            }

            // Actualizar progreso
            val progreso = obtenerOCrearProgreso(request.usuarioId)
            progreso.experienciaTotal += expGanada
            progresoRepository.save(progreso)
        }

        return ResultadoQuizResponse(
            correcto = correcto,
            explicacion = pregunta.explicacion,
            experienciaGanada = expGanada,
            puntoDesbloqueado = correcto
        )
    }

    @Transactional
    fun buscarArtefactoManual(request: BuscarArtefactoRequest): ResultadoBusquedaResponse {
        logger.info("🔍 Búsqueda manual de artefacto - Usuario: {}, Punto: {}",
            request.usuarioId, request.puntoId)

        val artefacto = buscarArtefactoAleatorio(request.usuarioId, request.puntoId)

        if (artefacto != null) {
            val expGanada = artefacto.rareza * 50
            val progreso = obtenerOCrearProgreso(request.usuarioId)

            // 👇 ARREGLADO: Actualizar experiencia Y contador de artefactos
            progreso.experienciaTotal += expGanada
            progreso.artefactosEncontrados += 1
            progreso.ultimaVisita = LocalDateTime.now()

            progresoRepository.save(progreso)

            return ResultadoBusquedaResponse(
                encontrado = true,
                artefacto = convertirAArtefactoDTO(artefacto, true, LocalDateTime.now(), 1),
                mensaje = "¡Encontraste ${artefacto.nombre}!",
                experienciaGanada = expGanada
            )
        }

        return ResultadoBusquedaResponse(
            encontrado = false,
            artefacto = null,
            mensaje = "No encontraste ningún artefacto esta vez. ¡Sigue explorando!",
            experienciaGanada = 0
        )
    }

    @Transactional(readOnly = true)
    fun obtenerColeccionArtefactos(usuarioId: Long): List<ArtefactoDTO> {
        logger.info("📦 Obteniendo colección de artefactos - Usuario: {}", usuarioId)

        val todosArtefactos = artefactoRepository.findByActivoTrue()
        val artefactosUsuario = usuarioArtefactoRepository.findByUsuarioId(usuarioId)

        return todosArtefactos.map { artefacto ->
            val ua = artefactosUsuario.find { it.artefactoId == artefacto.id }
            val encontrado = ua != null
            convertirAArtefactoDTO(
                artefacto,
                encontrado,
                ua?.fechaEncontrado,
                ua?.cantidad ?: 0
            )
        }
    }

    @Transactional(readOnly = true)
    fun obtenerMisionesDisponibles(usuarioId: Long): List<MisionDTO> {
        // TODO: Implementar cuando tengamos misiones creadas
        return emptyList()
    }

    @Transactional
    fun aceptarMision(usuarioId: Long, misionId: Long): MisionDTO {
        // TODO: Implementar
        throw NotImplementedError("Misiones próximamente")
    }

    @Transactional(readOnly = true)
    fun obtenerEstadisticasDetalladas(usuarioId: Long): EstadisticasExploracionDTO {
        val descubrimientos = descubrimientoRepository.findByUsuarioId(usuarioId)
        return calcularEstadisticas(usuarioId, descubrimientos)
    }

    // Métodos helper
    private fun generarNarrativa(punto: PuntoInteres, nivel: NivelDescubrimiento): String {
        return when (nivel) {
            NivelDescubrimiento.NO_VISITADO -> punto.descripcion
            NivelDescubrimiento.BRONCE -> "${punto.descripcion}\n\nPrimera exploración completada."
            NivelDescubrimiento.PLATA -> "${punto.historiaDetallada}\n\nDescubrimiento avanzado."
            NivelDescubrimiento.ORO -> "${punto.historiaDetallada}\n\n¡Exploración maestra!"
        }
    }

    private fun convertirAPreguntaDTO(pregunta: PreguntaQuiz): PreguntaQuizDTO {
        return PreguntaQuizDTO(
            id = pregunta.id!!,
            pregunta = pregunta.pregunta,
            opciones = listOf(
                OpcionQuizDTO("A", pregunta.opcionA),
                OpcionQuizDTO("B", pregunta.opcionB),
                OpcionQuizDTO("C", pregunta.opcionC),
                OpcionQuizDTO("D", pregunta.opcionD)
            ),
            dificultad = pregunta.dificultad
        )
    }
}