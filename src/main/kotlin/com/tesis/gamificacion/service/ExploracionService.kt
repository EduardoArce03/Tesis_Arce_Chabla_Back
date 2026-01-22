// src/main/kotlin/com/tesis/gamificacion/service/ExploracionService.kt
package com.tesis.gamificacion.service

import com.tesis.gamificacion.model.entities.*
import com.tesis.gamificacion.model.enums.*
import com.tesis.gamificacion.model.request.*
import com.tesis.gamificacion.model.responses.*
import com.tesis.gamificacion.model.responses.ProgresoExploracionResponse
import com.tesis.gamificacion.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class ExploracionService(
    private val puntoInteresRepository: PuntoInteresRepository,
    private val capaDescubrimientoRepository: CapaDescubrimientoRepository,
    private val progresoExploracionRepository: ProgresoExploracionRepository,
    private val fotografiaObjetivoRepository: FotografiaObjetivoRepository,
    private val fotografiaCapturadaRepository: FotografiaCapturadaRepository,
    private val artefactoRepository: ArtefactoRepository,
    private val dialogoHistorialRepository: DialogoHistorialRepository,
    private val narrativaIAService: NarrativaIAService,
    private val dialogoIAService: DialogoIAService,
    private val fotografiaIAService: FotografiaIAService,
    private val misionService: MisionService,
    private val puntoDescubrimientoRepository: PuntoDescubrimientoRepository,
    private val usuarioArtefactoRepository: UsuarioArtefactoRepository,
) {

    // ==================== INICIALIZACIÓN ====================

    /**
     * Inicializar exploración para una partida
     */
    fun inicializarExploracion(partidaId: Long, usuarioId: Long): ProgresoExploracionResponse {
        val progresoExistente = progresoExploracionRepository.findByPartidaId(partidaId)
        if (progresoExistente != null) {
            return construirProgresoResponse(progresoExistente)
        }

        val progreso = ProgresoExploracion(
            partidaId = partidaId,
            usuarioId = usuarioId, // ✅ Usar parámetro
            nivelActual = NivelCapa.SUPERFICIE,
            puntosDescubiertos = 0,
            puntosTotales = puntoInteresRepository.count().toInt(),
            misionesCompletadas = 0,
            fotografiasCapturadas = 0,
            dialogosRealizados = 0
        )

        val progresoGuardado = progresoExploracionRepository.save(progreso)
        inicializarCapas(progresoGuardado)
        misionService.generarMisionesIniciales(partidaId)

        return construirProgresoResponse(progresoGuardado)
    }

    /**
     * Construir respuesta de progreso completa
     */
    private fun construirProgresoResponse(progreso: ProgresoExploracion): ProgresoExploracionResponse {
        val capas = obtenerCapas(progreso.partidaId)

        return ProgresoExploracionResponse(
            partidaId = progreso.partidaId,
            usuarioId = progreso.usuarioId,
            nivelActual = progreso.nivelActual,
            puntosDescubiertos = progreso.puntosDescubiertos,
            puntosTotales = progreso.puntosTotales,
            porcentajeTotal = if (progreso.puntosTotales > 0) {
                (progreso.puntosDescubiertos.toDouble() / progreso.puntosTotales) * 100
            } else 0.0,
            misionesCompletadas = progreso.misionesCompletadas,
            fotografiasCapturadas = progreso.fotografiasCapturadas,
            fotosRaras = progreso.fotosRaras,
            fotosLegendarias = progreso.fotosLegendarias,
            dialogosRealizados = progreso.dialogosRealizados,
            capas = capas,
            fechaInicio = progreso.fechaInicio,
            ultimaActividad = progreso.ultimaActividad,
            puntosTotal = progreso.puntosTotal,
            nivelArqueologo = progreso.nivelArqueologo,
            nombreNivel = obtenerNombreNivel(progreso.nivelArqueologo),
            experienciaTotal = progreso.experienciaTotal,
            experienciaParaSiguienteNivel = calcularExpRestante(progreso)
        )
    }

    /**
     * Calcula experiencia restante para siguiente nivel
     */
    private fun calcularExpRestante(progreso: ProgresoExploracion): Int {
        val expParaSiguiente = calcularExpParaNivel(progreso.nivelArqueologo + 1)
        return maxOf(0, expParaSiguiente - progreso.experienciaTotal)
    }

    /**
     * Calcula experiencia necesaria para un nivel
     */
    private fun calcularExpParaNivel(nivel: Int): Int {
        return nivel * 1000 // Cada nivel requiere 1000 XP
    }

    /**
     * Obtiene el nombre descriptivo del nivel
     */
    private fun obtenerNombreNivel(nivel: Int): String {
        return when (nivel) {
            1 -> "Arqueólogo Novato"
            2 -> "Explorador Aprendiz"
            3 -> "Investigador Junior"
            4 -> "Arqueólogo Experimentado"
            5 -> "Explorador Experto"
            6 -> "Investigador Senior"
            7 -> "Maestro Arqueólogo"
            8 -> "Guardián del Conocimiento"
            9 -> "Sabio Ancestral"
            10 -> "Leyenda Viva"
            else -> if (nivel > 10) "Maestro Supremo" else "Principiante"
        }
    }

    private fun inicializarCapas(progreso: ProgresoExploracion) {
        NivelCapa.entries.forEach { nivel ->
            val capa = CapaDescubrimiento(
                progreso = progreso,
                nivel = nivel,
                desbloqueada = nivel == NivelCapa.SUPERFICIE,
                porcentajeDescubrimiento = if (nivel == NivelCapa.SUPERFICIE) 0.0 else 0.0,
                puntosPorDescubrir = contarPuntosPorNivel(nivel)
            )
            capaDescubrimientoRepository.save(capa)
        }
    }

    private fun contarPuntosPorNivel(nivel: NivelCapa): Int {
        return puntoInteresRepository.countByNivelMinimo(nivel.numero)
    }

    // ==================== DESCUBRIMIENTO DE PUNTOS ====================

    /**
     * Descubrir un punto de interés
     */
    fun descubrirPunto(request: DescubrirPuntoRequest): DescubrimientoPuntoResponse {
        val progreso = obtenerProgreso(request.partidaId)
        val punto = puntoInteresRepository.findById(request.puntoId)
            .orElseThrow { IllegalArgumentException("Punto no encontrado") }

        // Verificar si el punto ya fue descubierto
        val descubrimiento = punto.descubrimientos.find { it.progreso.id == progreso.id }



        if (descubrimiento != null) {
            // Ya fue descubierto, solo incrementar visitas
            descubrimiento.visitas++
            descubrimiento.ultimaVisita = LocalDateTime.now()

            return DescubrimientoPuntoResponse(
                puntoId = punto.id!!,
                nombrePunto = punto.nombre,
                yaDescubierto = true,
                nivelDescubierto = descubrimiento.nivelDescubrimiento,
                narrativaGenerada = null,
                recompensas = emptyList(),
                nuevaCapaDesbloqueada = null
            )
        }

        // Primer descubrimiento
        val nivelDescubrimiento = determinarNivelDescubrimiento(punto, progreso)

        val narrativa = generarNarrativaDescubrimiento(punto, nivelDescubrimiento)

        val nuevoDescubrimiento = PuntoDescubrimiento(
            puntoInteres = punto,
            progreso = progreso,
            nivelDescubrimiento = nivelDescubrimiento,
            visitas = 1,
            quizCompletado = false,
            usuarioId = progreso.usuarioId,
            narrativa = narrativa
        )
        punto.descubrimientos.add(nuevoDescubrimiento)
        puntoInteresRepository.save(punto)

        // Actualizar progreso
        progreso.puntosDescubiertos++
        actualizarPorcentajeCapa(progreso, nivelDescubrimiento)

        // Generar narrativa con IA

        // Verificar recompensas
        val recompensas = calcularRecompensasDescubrimiento(punto, nivelDescubrimiento)

        // Verificar desbloqueo de nuevas capas
        val nuevaCapaDesbloqueada = verificarDesbloqueoCapas(progreso)

        progresoExploracionRepository.save(progreso)

        return DescubrimientoPuntoResponse(
            puntoId = punto.id!!,
            nombrePunto = punto.nombre,
            yaDescubierto = false,
            nivelDescubierto = nivelDescubrimiento,
            narrativaGenerada = narrativa,
            recompensas = recompensas,
            nuevaCapaDesbloqueada = nuevaCapaDesbloqueada
        )
    }

    private fun determinarNivelDescubrimiento(punto: PuntoInteres, progreso: ProgresoExploracion): NivelCapa {
        // Determinar en qué nivel se descubre basado en el progreso actual
        val capasDesbloqueadas = capaDescubrimientoRepository.findByProgresoAndDesbloqueadaTrue(progreso)

        return capasDesbloqueadas
            .map { it.nivel }
            .filter { it.numero <= progreso.nivelActual.numero }
            .maxByOrNull { it.numero } ?: NivelCapa.SUPERFICIE
    }

    private fun actualizarPorcentajeCapa(progreso: ProgresoExploracion, nivel: NivelCapa) {
        val capa = capaDescubrimientoRepository.findByProgresoAndNivel(progreso, nivel)
            ?: return

        val puntosDescubiertos = contarPuntosDescubiertosEnNivel(progreso, nivel)
        val puntosTotales = capa.puntosPorDescubrir

        capa.porcentajeDescubrimiento = if (puntosTotales > 0) {
            (puntosDescubiertos.toDouble() / puntosTotales) * 100
        } else 0.0

        capaDescubrimientoRepository.save(capa)
    }

    private fun contarPuntosDescubiertosEnNivel(progreso: ProgresoExploracion, nivel: NivelCapa): Int {
        return puntoInteresRepository.findAll()
            .count { punto ->
                punto.descubrimientos.any {
                    it.progreso.id == progreso.id && it.nivelDescubrimiento == nivel
                }
            }
    }

    private fun generarNarrativaDescubrimiento(punto: PuntoInteres, nivel: NivelCapa): String {
        val conceptoClave = "${punto.nombre} - ${nivel.nombre} - ${punto.categoria}"

        // Intentar generar con IA
        val narrativaIA = punto.imagenUrl?.let { imagenUrl ->
            narrativaIAService.generarNarrativaDescubrimiento(

                nombrePunto = punto.nombre,
                categoria = punto.categoria.name,
                nivel = nivel.name,
                descripcionBase = punto.descripcion
            )
        }
        return narrativaIA ?: generarNarrativaFallback(punto, nivel)
    }

    private fun generarNarrativaFallback(punto: PuntoInteres, nivel: NivelCapa): String {
        return when (nivel) {
            NivelCapa.SUPERFICIE -> "Has descubierto ${punto.nombre}. Este lugar muestra las ruinas tal como las vemos hoy."
            NivelCapa.INCA -> "En tiempos del Imperio Inca, ${punto.nombre} era ${punto.descripcion}."
            NivelCapa.CANARI -> "Antes de los Incas, los Cañaris construyeron aquí ${punto.nombre}."
            NivelCapa.ANCESTRAL -> "Las leyendas ancestrales hablan de ${punto.nombre} como un lugar sagrado."
        }
    }

    private fun calcularRecompensasDescubrimiento(punto: PuntoInteres, nivel: NivelCapa): List<RecompensaDTO> {
        val recompensas = mutableListOf<RecompensaDTO>()

        // Puntos base por descubrimiento
        val puntosBase = when (nivel) {
            NivelCapa.SUPERFICIE -> 10
            NivelCapa.INCA -> 25
            NivelCapa.CANARI -> 50
            NivelCapa.ANCESTRAL -> 100
        }

        recompensas.add(RecompensaDTO(
            tipo = "PUNTOS",
            cantidad = puntosBase,
            descripcion = "Puntos por descubrir ${punto.nombre}"
        ))

        // Bonus por categoría especial
        if (punto.categoria == CategoriaPunto.TEMPLO || punto.categoria == CategoriaPunto.CEREMONIAL) {
            recompensas.add(RecompensaDTO(
                tipo = "BONUS_CULTURAL",
                cantidad = 20,
                descripcion = "Bonus por descubrir sitio ceremonial"
            ))
        }

        return recompensas
    }

    private fun verificarDesbloqueoCapas(progreso: ProgresoExploracion): NivelCapaDTO? {
        val capas = capaDescubrimientoRepository.findByProgreso(progreso)

        // Verificar cada capa bloqueada
        for (capa in capas.filter { !it.desbloqueada }) {
            if (cumpleRequisitosDesbloqueo(capa, progreso)) {
                capa.desbloqueada = true
                capa.fechaDesbloqueo = LocalDateTime.now()
                capaDescubrimientoRepository.save(capa)

                progreso.nivelActual = capa.nivel

                return NivelCapaDTO(
                    nivel = capa.nivel,
                    nombre = capa.nivel.nombre,
                    descripcion = capa.nivel.descripcion,
                    desbloqueada = true,
                    porcentajeDescubrimiento = capa.porcentajeDescubrimiento
                )
            }
        }

        return null
    }

    private fun cumpleRequisitosDesbloqueo(capa: CapaDescubrimiento, progreso: ProgresoExploracion): Boolean {
        return when (capa.nivel) {
            NivelCapa.SUPERFICIE -> true // Siempre desbloqueada
            NivelCapa.INCA -> {
                // Desbloquear tras descubrir 50% de superficie
                val capaSuperficie = capaDescubrimientoRepository.findByProgresoAndNivel(progreso, NivelCapa.SUPERFICIE)
                capaSuperficie?.porcentajeDescubrimiento ?: 0.0 >= 50.0
            }
            NivelCapa.CANARI -> {
                // Desbloquear tras completar 3 misiones y 70% de capa Inca
                val capaInca = capaDescubrimientoRepository.findByProgresoAndNivel(progreso, NivelCapa.INCA)
                progreso.misionesCompletadas >= 3 && (capaInca?.porcentajeDescubrimiento ?: 0.0) >= 70.0
            }
            NivelCapa.ANCESTRAL -> {
                // Desbloquear tras completar todas las capas anteriores al 90%
                val todasLasCapasAnteriores = capaDescubrimientoRepository.findByProgreso(progreso)
                    .filter { it.nivel.numero < NivelCapa.ANCESTRAL.numero }

                todasLasCapasAnteriores.all { it.porcentajeDescubrimiento >= 90.0 }
            }
        }
    }

    // ==================== FOTOGRAFÍA ====================

    /**
     * Capturar fotografía de objetivo
     */
    fun capturarFotografia(request: CapturarFotografiaRequest): CapturarFotografiaResponse {
        val progreso = obtenerProgreso(request.partidaId)
        val objetivo = fotografiaObjetivoRepository.findById(request.objetivoId)
            .orElseThrow { IllegalArgumentException("Objetivo fotográfico no encontrado") }

        // Verificar si ya fue capturada
        val yaCapturada = fotografiaCapturadaRepository.existsByProgresoAndObjetivo(progreso, objetivo)

        if (yaCapturada) {
            return CapturarFotografiaResponse(
                exito = false,
                mensaje = "Ya has capturado esta fotografía anteriormente",
                fotografiaId = null,
                analisisIA = null,
                recompensas = emptyList()
            )
        }

        // Analizar foto con IA
        val analisis = fotografiaIAService.analizarFotografia(
            objetivo = objetivo,
            imagenBase64 = request.imagenBase64,
            descripcionUsuario = request.descripcionUsuario
        )

        if (!analisis.esValida || !analisis.cumpleCriterios) {
            return CapturarFotografiaResponse(
                exito = false,
                mensaje = "La fotografía no cumple con los criterios del objetivo",
                fotografiaId = null,
                analisisIA = FotoAnalisisDTO(
                    esValida = analisis.esValida,
                    descripcionIA = analisis.descripcionIA,
                    cumpleCriterios = analisis.cumpleCriterios,
                    confianza = analisis.confianza
                ),
                recompensas = emptyList()
            )
        }

        // Guardar fotografía capturada
        val fotografiaCapturada = FotografiaCapturada(
            objetivo = objetivo,
            progreso = progreso,
            imagenUrl = request.imagenBase64, // En producción, guardar en storage
            descripcionUsuario = request.descripcionUsuario,
            descripcionIA = analisis.descripcionIA,
            rarezaObtenida = analisis.rarezaDetectada,
            puntuacionIA = analisis.confianza
        )

        val fotografiaGuardada = fotografiaCapturadaRepository.save(fotografiaCapturada)

        // Actualizar progreso
        progreso.fotografiasCapturadas++
        progresoExploracionRepository.save(progreso)

        // Calcular recompensas
        val recompensas = calcularRecompensasFotografia(objetivo, analisis.rarezaDetectada)

        // Verificar si completa alguna misión
        misionService.verificarProgresoMisionesFotografia(request.partidaId, objetivo)

        return CapturarFotografiaResponse(
            exito = true,
            mensaje = "¡Fotografía capturada exitosamente!",
            fotografiaId = fotografiaGuardada.id,
            analisisIA = FotoAnalisisDTO(
                esValida = true,
                descripcionIA = analisis.descripcionIA,
                cumpleCriterios = true,
                confianza = analisis.confianza
            ),
            recompensas = recompensas
        )
    }

    private fun calcularRecompensasFotografia(objetivo: FotografiaObjetivo, rareza: RarezaFoto): List<RecompensaDTO> {
        val recompensas = mutableListOf<RecompensaDTO>()

        val puntosBase = when (rareza) {
            RarezaFoto.COMUN -> 15
            RarezaFoto.POCO_COMUN -> 30
            RarezaFoto.RARA -> 60
            RarezaFoto.EPICA -> 120
            RarezaFoto.LEGENDARIA -> 250
            else -> {
                throw IllegalArgumentException("error")
            }
        }

        recompensas.add(RecompensaDTO(
            tipo = "PUNTOS",
            cantidad = puntosBase,
            descripcion = "Puntos por fotografía ${rareza.name}"
        ))

        // Bonus si es la primera foto de esta rareza
        if (rareza == RarezaFoto.EPICA || rareza == RarezaFoto.LEGENDARIA) {
            recompensas.add(RecompensaDTO(
                tipo = "LOGRO",
                cantidad = 1,
                descripcion = "¡Primera fotografía ${rareza.name}!"
            ))
        }

        return recompensas
    }

    /**
     * Obtener objetivos fotográficos disponibles
     */
    fun obtenerObjetivosFotograficos(partidaId: Long, puntoId: Long?): List<FotografiaObjetivoDTO> {
        val progreso = obtenerProgreso(partidaId)

        val objetivos = if (puntoId != null) {
            fotografiaObjetivoRepository.findByPuntoInteresId(puntoId)
        } else {
            fotografiaObjetivoRepository.findByNivelRequeridoLessThanEqual(progreso.nivelActual)
        }

        return objetivos.map { objetivo ->
            val yaCapturada = fotografiaCapturadaRepository.existsByProgresoAndObjetivo(progreso, objetivo)

            FotografiaObjetivoDTO(
                id = objetivo.id!!,
                puntoInteresId = objetivo.puntoInteres.id!!,
                nombrePunto = objetivo.puntoInteres.nombre,
                descripcion = objetivo.descripcion,
                rareza = objetivo.rareza,
                puntosRecompensa = calcularPuntosObjetivo(objetivo.rareza),
                yaCapturada = yaCapturada
            )
        }
    }

    private fun calcularPuntosObjetivo(rareza: RarezaFoto): Int {
        return when (rareza) {
            RarezaFoto.COMUN -> 15
            RarezaFoto.POCO_COMUN -> 30
            RarezaFoto.RARA -> 60
            RarezaFoto.EPICA -> 120
            RarezaFoto.LEGENDARIA -> 250
            else -> {
                throw IllegalArgumentException("error")
            }
        }
    }

    // ==================== DIÁLOGOS CON ESPÍRITUS ====================

    /**
     * Dialogar con espíritu ancestral
     */
    fun dialogarConEspiritu(request: DialogarEspirituRequest): DialogoEspirituResponse {
        val progreso = obtenerProgreso(request.partidaId)
        val capa = capaDescubrimientoRepository.findByProgresoAndNivel(progreso, request.nivelCapa)
            ?: throw IllegalArgumentException("Capa no encontrada")

        if (!capa.desbloqueada) {
            return DialogoEspirituResponse(
                exito = false,
                mensaje = "Esta capa temporal aún no está desbloqueada",
                respuestaEspiritu = null,
                conocimientoDesbloqueado = null
            )
        }

        // Obtener historial de diálogos previos para contexto
        val historialPrevio = dialogoHistorialRepository
            .findByProgresoAndCapaOrderByFechaDesc(progreso, capa)
            .take(5)

        // ✅ Obtener nombre del punto si se proporciona
        val puntoNombre = request.puntoInteresId?.let { puntoId ->
            puntoInteresRepository.findById(puntoId)
                .map { it.nombre }
                .orElse(null)
        }

        // ✅ Generar respuesta con IA usando el service actualizado
        val respuestaIA = dialogoIAService.generarRespuestaEspiritu(
            capa = capa,
            pregunta = request.pregunta,
            historialPrevio = historialPrevio,
            puntoInteresNombre = puntoNombre
        )

        // Guardar diálogo en historial
        val dialogo = DialogoHistorial(
            progreso = progreso,
            capa = capa,
            preguntaUsuario = request.pregunta,
            respuestaEspiritu = respuestaIA,
            puntoInteresRelacionado = request.puntoInteresId?.let {
                puntoInteresRepository.findById(it).orElse(null)
            }
        )
        dialogoHistorialRepository.save(dialogo)

        // Actualizar contador
        progreso.dialogosRealizados++
        progresoExploracionRepository.save(progreso)

        // Verificar si desbloquea conocimiento especial
        val conocimientoDesbloqueado = verificarDesbloqueoConocimiento(progreso, capa)

        return DialogoEspirituResponse(
            exito = true,
            mensaje = "El espíritu ancestral ha respondido",
            respuestaEspiritu = respuestaIA,
            conocimientoDesbloqueado = conocimientoDesbloqueado
        )
    }

    private fun verificarDesbloqueoConocimiento(
        progreso: ProgresoExploracion,
        capa: CapaDescubrimiento
    ): String? {
        val dialogosEnCapa = dialogoHistorialRepository.countByProgresoAndCapa(progreso, capa)

        return when {
            dialogosEnCapa == 5L -> "Has desbloqueado: Historia básica de ${capa.nivel.nombre}"
            dialogosEnCapa == 15L -> "Has desbloqueado: Secretos de ${capa.nivel.nombre}"
            dialogosEnCapa == 30L -> "Has desbloqueado: Sabiduría ancestral completa"
            else -> null
        }
    }

    // ==================== MISIONES ====================

    /**
     * Obtener misiones disponibles
     */
    fun obtenerMisionesDisponibles(partidaId: Long): List<MisionDTO> {
        val progreso = obtenerProgreso(partidaId)
        return misionService.obtenerMisionesDisponibles(partidaId)
    }

    /**
     * Completar misión
     */
    fun completarMision(request: CompletarMisionRequest): CompletarMisionResponse {
        val progreso = obtenerProgreso(request.partidaId)
        val resultado = misionService.completarMision(request.partidaId, request.misionId)

        if (resultado.completada) {
            progreso.misionesCompletadas++
            progresoExploracionRepository.save(progreso)

            // Verificar si desbloquea nueva capa
            val nuevaCapa = verificarDesbloqueoCapas(progreso)

            return CompletarMisionResponse(
                exito = true,
                mensaje = "¡Misión completada!",
                recompensas = resultado.recompensas,
                nuevaCapaDesbloqueada = nuevaCapa
            )
        }

        return CompletarMisionResponse(
            exito = false,
            mensaje = resultado.mensaje ?: "No se pudo completar la misión",
            recompensas = emptyList(),
            nuevaCapaDesbloqueada = null
        )
    }

    // ==================== CONSULTAS ====================

    /**
     * Obtener progreso completo
     */
    fun obtenerProgresoCompleto(partidaId: Long): ProgresoExploracionResponse {
        val progreso = obtenerProgreso(partidaId)
        return construirProgresoResponse(progreso)
    }

    /**
     * Obtener puntos de interés disponibles
     */
    /**
     * Obtener puntos de interés disponibles con su estado de descubrimiento
     */
    fun obtenerPuntosDisponibles(partidaId: Long): List<PuntoInteresDTO> {
        val progreso = obtenerProgreso(partidaId)
        val puntos = puntoInteresRepository.findByActivoTrue()

        // Obtener todos los descubrimientos del usuario de una vez
        val descubrimientos = puntoDescubrimientoRepository.findByProgreso(progreso)

        // Obtener artefactos por punto
        val artefactosPorPunto = obtenerArtefactosPorPunto(progreso)

        return puntos.map { punto ->
            val descubrimiento = descubrimientos.find { it.puntoInteres.id == punto.id }

            // Verificar si está desbloqueado (por nivel o por orden)
            val desbloqueado = verificarPuntoDesbloqueado(
                punto,
                progreso.nivelArqueologo,
                descubrimientos
            )

            PuntoInteresDTO(
                id = punto.id!!,
                nombre = punto.nombre,
                nombreKichwa = punto.nombreKichwa,
                descripcion = punto.descripcion,
                imagenUrl = punto.imagenUrl,
                coordenadaX = punto.latitud,  // Usar como X
                coordenadaY = punto.longitud, // Usar como Y
                categoria = punto.categoria,
                nivelRequerido = punto.nivelMinimo,
                puntosPorDescubrir = 100, // Puntos base por descubrir
                desbloqueado = desbloqueado,
                visitado = descubrimiento != null,
                nivelDescubrimiento = descubrimiento?.nivelDescubrimiento, // ✅ Ya es NivelCapa?
                visitas = descubrimiento?.visitas ?: 0,
                tiempoExplorado = descubrimiento?.tiempoExplorado ?: 0,
                quizCompletado = descubrimiento?.quizCompletado ?: false,
                artefactosDisponibles = artefactosPorPunto[punto.id]?.first ?: 0,
                artefactosEncontrados = artefactosPorPunto[punto.id]?.second ?: 0
            )
        }
    }

    /**
     * Obtiene cantidad de artefactos por punto
     * Retorna Map<PuntoId, Pair<Disponibles, Encontrados>>
     */
    private fun obtenerArtefactosPorPunto(progreso: ProgresoExploracion): Map<Long, Pair<Int, Int>> {
        val todosArtefactos = artefactoRepository.findByActivoTrue()
        val artefactosUsuario = usuarioArtefactoRepository.findByProgreso(progreso)

        return todosArtefactos.groupBy { it.puntoInteres.id!! }
            .mapValues { (puntoId, artefactos) ->
                val disponibles = artefactos.size
                val encontrados = artefactosUsuario.count {
                    artefactos.any { artefacto -> artefacto.id == it.artefacto.id }
                }
                Pair(disponibles, encontrados)
            }
    }

    /**
     * Verifica si un punto está desbloqueado
     */
    private fun verificarPuntoDesbloqueado(
        punto: PuntoInteres,
        nivelUsuario: Int,
        descubrimientos: List<PuntoDescubrimiento>
    ): Boolean {
        // Verificar nivel mínimo
        if (nivelUsuario < punto.nivelMinimo) {
            return false
        }

        // Si es orden 1, siempre está desbloqueado
        if (punto.ordenDesbloqueo <= 1) {
            return true
        }

        // Verificar que haya descubierto puntos anteriores
        val puntosAnteriores = puntoInteresRepository.findByActivoTrue()
            .filter { it.ordenDesbloqueo < punto.ordenDesbloqueo }

        val todosAnterioresDescubiertos = puntosAnteriores.all { puntoAnterior ->
            descubrimientos.any { it.puntoInteres.id == puntoAnterior.id }
        }

        return todosAnterioresDescubiertos
    }

    /**
     * Obtener capas de descubrimiento
     */
    fun obtenerCapas(partidaId: Long): List<NivelCapaDTO> {
        val progreso = obtenerProgreso(partidaId)
        val capas = capaDescubrimientoRepository.findByProgreso(progreso)

        return capas.sortedBy { it.nivel.numero }.map { capa ->
            NivelCapaDTO(
                nivel = capa.nivel,
                nombre = capa.nivel.nombre,
                descripcion = capa.nivel.descripcion,
                desbloqueada = capa.desbloqueada,
                porcentajeDescubrimiento = capa.porcentajeDescubrimiento,
                fechaDesbloqueo = capa.fechaDesbloqueo,
                puntosDescubiertos = contarPuntosDescubiertosEnNivel(progreso, capa.nivel),
                puntosTotales = capa.puntosPorDescubrir
            )
        }
    }

    /**
     * Obtener historial de diálogos
     */
    fun obtenerHistorialDialogos(partidaId: Long, nivelCapa: NivelCapa?): List<DialogoHistorialDTO> {
        val progreso = obtenerProgreso(partidaId)

        val dialogos = if (nivelCapa != null) {
            val capa = capaDescubrimientoRepository.findByProgresoAndNivel(progreso, nivelCapa)
            capa?.let { dialogoHistorialRepository.findByProgresoAndCapaOrderByFechaDesc(progreso, it) } ?: emptyList()
        } else {
            dialogoHistorialRepository.findByProgresoOrderByFechaDesc(progreso)
        }

        return dialogos.map { dialogo ->
            DialogoHistorialDTO(
                id = dialogo.id!!,
                pregunta = dialogo.preguntaUsuario,
                respuesta = dialogo.respuestaEspiritu,
                nivelCapa = dialogo.capa.nivel,
                fecha = dialogo.fecha,
                puntoInteresNombre = dialogo.puntoInteresRelacionado?.nombre
            )
        }
    }

    /**
     * Obtener galería de fotografías
     */
    fun obtenerGaleriaFotografias(partidaId: Long): List<FotografiaCapturadaDTO> {
        val progreso = obtenerProgreso(partidaId)
        val fotografias = fotografiaCapturadaRepository.findByProgresoOrderByFechaDesc(progreso)

        return fotografias.map { foto ->
            FotografiaCapturadaDTO(
                id = foto.id!!,
                objetivoId = foto.objetivo.id!!,
                descripcionObjetivo = foto.objetivo.descripcion,
                puntoInteresNombre = foto.objetivo.puntoInteres.nombre,
                imagenUrl = foto.imagenUrl,
                descripcionUsuario = foto.descripcionUsuario,
                descripcionIA = foto.descripcionIA,
                rareza = foto.rarezaObtenida,
                puntuacionIA = foto.puntuacionIA,
                fecha = foto.fecha
            )
        }
    }
    private fun obtenerProgreso(partidaId: Long): ProgresoExploracion {
        return progresoExploracionRepository.findByPartidaId(partidaId)
            ?: throw IllegalArgumentException("No existe progreso de exploración para la partida $partidaId. Debes inicializar la exploración primero.")
    }

    // ==================== UTILIDADES ====================
}