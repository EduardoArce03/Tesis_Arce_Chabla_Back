package com.tesis.gamificacion.dto.response

import com.tesis.gamificacion.dto.misiones.ProgresoMisionDTO
import com.tesis.gamificacion.model.enums.*
import java.time.LocalDateTime

// ========== LISTADO DE MISIONES ==========
data class ListaMisionesResponse(
    val disponibles: List<MisionCardDTO>,
    val enProgreso: List<MisionCardDTO>,
    val completadas: List<MisionCardDTO>,
    val bloqueadas: List<MisionCardDTO>,
    val estadisticas: EstadisticasMisionesDTO
)

data class MisionCardDTO(
    val id: Long,
    val titulo: String,
    val tituloKichwa: String,
    val descripcionCorta: String,
    val imagenPortada: String,
    val dificultad: DificultadMision,
    val tiempoEstimado: Int,
    val estado: EstadoMision,
    val npcGuia: NPCGuiaDTO,
    val recompensas: RecompensasDTO,
    val requisitos: RequisitosDTO,
    val progreso: ProgresoMisionDTO?
)

data class NPCGuiaDTO(
    val nombre: String,
    val nombreKichwa: String,
    val avatar: String
)

data class RecompensasDTO(
    val experiencia: Int,
    val puntos: Int,
    val insignias: List<InsigniaDTO>
)

data class RequisitosDTO(
    val nivelMinimo: Int,
    val misionesPrevias: List<Long>?,
    val insignias: List<String>?
)

data class EstadisticasMisionesDTO(
    val completadas: Int,
    val enProgreso: Int,
    val insigniasObtenidas: Int,
    val totalMisiones: Int,
    val porcentajeCompletado: Double
)

// ========== DETALLE DE MISIÓN ==========
data class DetalleMisionResponse(
    val mision: MisionDetalleDTO,
    val fases: List<FaseDTO>,
    val progreso: ProgresoMisionDTO?,
    val puedeIniciar: Boolean,
    val motivoBloqueo: String?
)

data class MisionDetalleDTO(
    val id: Long,
    val titulo: String,
    val tituloKichwa: String,
    val descripcionCorta: String,
    val descripcionLarga: String,
    val imagenPortada: String,
    val dificultad: DificultadMision,
    val tiempoEstimado: Int,
    val npcGuia: NPCGuiaDTO,
    val npcDialogoInicial: String,
    val recompensas: RecompensasDTO,
    val requisitos: RequisitosDTO
)

data class FaseDTO(
    val id: Long,
    val numeroFase: Int,
    val titulo: String,
    val descripcion: String,
    val tipoFase: TipoFase,
    val puntoInteresId: Long?,
    val experienciaFase: Int,
    val completada: Boolean
)

// ========== EJECUCIÓN DE MISIÓN ==========
data class IniciarMisionResponse(
    val usuarioMisionId: Long,
    val misionId: Long,
    val faseActual: FaseEjecucionDTO,
    val mensaje: String
)

data class FaseEjecucionDTO(
    val numeroFase: Int,
    val titulo: String,
    val descripcion: String,
    val tipoFase: TipoFase,
    val contenido: ContenidoFaseDTO
)

// Contenido según tipo de fase
sealed class ContenidoFaseDTO {
    data class DialogoContenido(
        val npcNombre: String,
        val npcAvatar: String,
        val dialogo: String
    ) : ContenidoFaseDTO()

    data class QuizContenido(
        val preguntas: List<PreguntaDTO>
    ) : ContenidoFaseDTO()

    data class VisitarPuntoContenido(
        val puntoInteresId: Long,
        val puntoNombre: String,
        val instrucciones: String
    ) : ContenidoFaseDTO()

    data class BuscarArtefactoContenido(
        val artefactoId: Long,
        val artefactoNombre: String,
        val puntoInteresId: Long,
        val pista: String
    ) : ContenidoFaseDTO()

    data class ExploracionLibreContenido(
        val tiempoRequerido: Int, // segundos
        val areaSugerida: String
    ) : ContenidoFaseDTO()

    data class DecisionContenido(
        val situacion: String,
        val opciones: List<OpcionDecisionDTO>
    ) : ContenidoFaseDTO()
}

data class PreguntaDTO(
    val id: Long,
    val pregunta: String,
    val opciones: List<OpcionDTO>,
    val puntos: Int
)

data class OpcionDTO(
    val letra: String,
    val texto: String
)

data class OpcionDecisionDTO(
    val id: String,
    val texto: String,
    val consecuencia: String
)

// ========== RESPONDER FASE ==========
data class ResponderFaseRequest(
    val usuarioMisionId: Long,
    val faseId: Long,
    val respuestas: List<RespuestaDTO>? = null, // Para quiz
    val puntoVisitadoId: Long? = null, // Para visitar punto
    val artefactoEncontradoId: Long? = null, // Para buscar artefacto
    val tiempoExploracion: Int? = null, // Para exploración libre
    val decisionId: String? = null // Para decisiones
)

data class RespuestaDTO(
    val preguntaId: Long,
    val respuesta: String // A, B, C, D
)

data class ResponderFaseResponse(
    val faseCompletada: Boolean,
    val correctas: Int,
    val incorrectas: Int,
    val puntuacion: Int,
    val experienciaGanada: Int,
    val retroalimentacion: List<RetroalimentacionDTO>,
    val siguienteFase: FaseEjecucionDTO?,
    val misionCompletada: Boolean,
    val insigniasObtenidas: List<InsigniaDTO>
)

data class RetroalimentacionDTO(
    val pregunta: String,
    val respuestaUsuario: String,
    val respuestaCorrecta: String,
    val esCorrecta: Boolean,
    val explicacion: String
)

// ========== INSIGNIAS ==========
data class InsigniaDTO(
    val id: Long,
    val codigo: String,
    val nombre: String,
    val nombreKichwa: String,
    val descripcion: String,
    val icono: String,
    val rareza: RarezaInsignia,
    val fechaObtencion: LocalDateTime?,
    val obtenida: Boolean
)

data class ColeccionInsigniasResponse(
    val insignias: List<InsigniaDTO>,
    val totalObtenidas: Int,
    val totalDisponibles: Int,
    val porcentajeCompletado: Double
)