package com.tesis.gamificacion.dto.response

import com.tesis.gamificacion.model.enums.*
import java.time.LocalDateTime

// Dashboard de Exploración
data class DashboardExploracionResponse(
    val progreso: ProgresoExploracionDTO,
    val puntosDescubiertos: List<PuntoInteresDTO>,
    val puntosDisponibles: List<PuntoInteresDTO>,
    val misionesActivas: List<MisionDTO>,
    val artefactosRecientes: List<ArtefactoDTO>,
    val estadisticas: EstadisticasExploracionDTO
)

data class ProgresoExploracionDTO(
    val nivelArqueologo: Int,
    val experienciaActual: Int,
    val experienciaParaSiguienteNivel: Int,
    val porcentajeProgreso: Double,
    val puntosDescubiertos: Int,
    val totalPuntos: Int,
    val artefactosEncontrados: Int,
    val totalArtefactos: Int,
    val misionesCompletadas: Int
)

data class PuntoInteresDTO(
    val id: Long,
    val nombre: String,
    val nombreKichwa: String,
    val descripcion: String,
    val imagenUrl: String,
    val coordenadaX: Double,
    val coordenadaY: Double,
    val categoria: CategoriaPunto,
    val nivelRequerido: Int,
    val puntosPorDescubrir: Int,
    val desbloqueado: Boolean,
    val visitado: Boolean,
    val nivelDescubrimiento: NivelDescubrimiento,
    val visitas: Int,
    val tiempoExplorado: Int,
    val quizCompletado: Boolean,
    val artefactosDisponibles: Int,
    val artefactosEncontrados: Int
)

data class MisionDTO(
    val id: Long,
    val titulo: String,
    val descripcion: String,
    val tipo: TipoMision,
    val objetivo: ObjetivoMisionDTO,
    val progresoActual: ProgresoMisionDTO,
    val recompensaXP: Int,
    val recompensaPuntos: Int,
    val nivelRequerido: Int,
    val completada: Boolean,
    val fechaInicio: LocalDateTime?,
    val diasRestantes: Int?
)

data class ObjetivoMisionDTO(
    val descripcion: String,
    val puntosObjetivo: List<Long>?,
    val cantidadRequerida: Int?,
    val tiempoRequerido: Int?
)

data class ProgresoMisionDTO(
    val puntosVisitados: List<Long>,
    val artefactosEncontrados: Int,
    val quizzesCompletados: Int,
    val tiempoExplorado: Int,
    val porcentajeCompletado: Double
)

data class ArtefactoDTO(
    val id: Long,
    val nombre: String,
    val nombreKichwa: String,
    val descripcion: String,
    val imagenUrl: String,
    val categoria: CategoriaArtefacto,
    val rareza: Int,
    val encontrado: Boolean,
    val fechaEncontrado: LocalDateTime?,
    val cantidad: Int,
    val puntoInteres: String
)

data class EstadisticasExploracionDTO(
    val tiempoTotalExploracion: Int, // minutos
    val visitasTotales: Int,
    val quizzesRespondidos: Int,
    val quizzesCorrectos: Int,
    val tasaAcierto: Double,
    val artefactosPorCategoria: Map<CategoriaArtefacto, Int>,
    val puntosFavorito: PuntoInteresDTO?
)

// Detalle de Punto
data class DetallePuntoResponse(
    val punto: PuntoInteresDTO,
    val narrativa: NarrativaDTO,
    val quiz: List<PreguntaQuizDTO>?,
    val artefactosDisponibles: List<ArtefactoDTO>,
    val historiaCompleta: String
)

data class NarrativaDTO(
    val texto: String,
    val nivel: NivelDescubrimiento,
    val generadaPorIA: Boolean
)

data class PreguntaQuizDTO(
    val id: Long,
    val pregunta: String,
    val opciones: List<OpcionQuizDTO>,
    val dificultad: Int
)

data class OpcionQuizDTO(
    val letra: String,
    val texto: String
)

// Request para visitar punto
data class VisitarPuntoRequest(
    val usuarioId: Long,
    val puntoId: Long,
    val tiempoSegundos: Int
)

// Response de visita
data class VisitaPuntoResponse(
    val descubrimiento: DescubrimientoDTO,
    val artefactoEncontrado: ArtefactoDTO?,
    val experienciaGanada: Int,
    val nivelSubido: Boolean,
    val nuevoNivel: Int?,
    val misionesActualizadas: List<MisionDTO>
)

data class DescubrimientoDTO(
    val puntoId: Long,
    val nombrePunto: String,
    val nivelDescubrimiento: NivelDescubrimiento,
    val nivelAnterior: NivelDescubrimiento,
    val visitas: Int,
    val tiempoTotal: Int,
    val quizCompletado: Boolean
)

// Request para responder quiz
data class ResponderQuizRequest(
    val usuarioId: Long,
    val puntoId: Long,
    val preguntaId: Long,
    val respuesta: String // A, B, C, D
)

data class ResultadoQuizResponse(
    val correcto: Boolean,
    val explicacion: String,
    val experienciaGanada: Int,
    val puntoDesbloqueado: Boolean
)

// Request para buscar artefacto
data class BuscarArtefactoRequest(
    val usuarioId: Long,
    val puntoId: Long
)

data class ResultadoBusquedaResponse(
    val encontrado: Boolean,
    val artefacto: ArtefactoDTO?,
    val mensaje: String,
    val experienciaGanada: Int
)