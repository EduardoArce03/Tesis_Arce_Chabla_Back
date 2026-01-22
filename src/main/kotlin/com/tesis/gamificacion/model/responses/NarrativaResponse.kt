package com.tesis.gamificacion.model.responses

import com.tesis.gamificacion.model.enums.CategoriaPunto
import com.tesis.gamificacion.model.enums.EstadoMision
import com.tesis.gamificacion.model.enums.NivelCapa
import com.tesis.gamificacion.model.enums.RarezaFoto
import com.tesis.gamificacion.model.enums.TipoMision
import java.time.LocalDate
import java.time.LocalDateTime

data class NarrativaResponse(
    val puntoInteresId: Long,
    val nivel: String,
    val nombreCapa: String, // "Período Inca"
    val descripcionCapa: String, // "1470-1532 d.C."
    val texto: String, // Narrativa generada por IA
    val elementosClave: List<String>, // ["Arquitectura inca", "Ceremonias solares"]
    val nombreEspiritu: String,
    val epocaEspiritu: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

data class DialogoResponse(
    val pregunta: String,
    val respuesta: String,
    val nombreEspiritu: String,
    val numeroConversacion: Int, // Cuántos diálogos llevas con este espíritu
    val nivelConfianza: Int, // 1-5 estrellas
    val desbloqueos: List<String>, // ["Pista para foto rara", "Fragmento de diario"]
    val puntosObtenidos: Int
)

data class FotoAnalisisResponse(
    val esValida: Boolean,
    val rarezaObtenida: RarezaFoto?,
    val descripcionIA: String,
    val cumpleCriterios: Boolean,
    val puntosObtenidos: Int,
    val mensajeFeedback: String,
    val fotoCapturedId: Long?,
    val objetivoCompletado: Boolean,
    val desbloqueos: List<String> // Qué desbloqueó esta foto
)

data class MisionResponse(
    val misionId: Long,
    val tipo: TipoMision,
    val titulo: String,
    val descripcion: String,
    val objetivos: List<ObjetivoMision>,
    val estado: EstadoMision,
    val porcentajeCompletado: Int,
    val puntosObtenidos: Int,
    val feedbackIA: String?,
    val nivelAlcanzado: String?, // "BRONCE", "PLATA", "ORO"
    val recompensas: List<String>
)

data class ObjetivoMision(
    val id: String,
    val descripcion: String,
    val completado: Boolean
)

data class ProgresoExploracionResponse(
    val partidaId: Long,
    val usuarioId: Long,
    val nivelActual: NivelCapa,
    val puntosDescubiertos: Int,
    val puntosTotales: Int,
    val porcentajeTotal: Double,
    val misionesCompletadas: Int,
    val fotografiasCapturadas: Int,
    val fotosRaras: Int,
    val fotosLegendarias: Int,
    val dialogosRealizados: Int,
    val capas: List<NivelCapaDTO>,
    val fechaInicio: LocalDateTime,
    val ultimaActividad: LocalDateTime,
    val puntosTotal: Int,
    val nivelArqueologo: Int,
    val nombreNivel: String,
    val experienciaTotal: Int,
    val experienciaParaSiguienteNivel: Int
)

data class NivelCapaDTO(
    val nivel: NivelCapa,
    val nombre: String,
    val descripcion: String,
    val desbloqueada: Boolean,
    val porcentajeDescubrimiento: Double,
    val fechaDesbloqueo: LocalDateTime? = null,
    val puntosDescubiertos: Int = 0,
    val puntosTotales: Int = 0
)

data class EstadoCapaDTO(
    val nivel: NivelCapa,
    val completada: Boolean,
    val porcentaje: Double,
    val visitado: Boolean
)

data class ProgresoCapaDTO(
    val puntoInteresId: Long,
    val nombrePunto: String,
    val capas: List<CapaEstadoDTO>
)

data class CapaEstadoDTO(
    val nivel: String,
    val desbloqueada: Boolean,
    val estadoMision: String,
    val fotosCompletadas: Int,
    val fotosTotales: Int,
    val numeroDialogos: Int
)

data class DiarioGeneradoResponse(
    val fecha: LocalDate,
    val titulo: String,
    val contenido: String, // Texto generado por IA
    val descubrimientosHoy: List<String>,
    val fotosDestacadas: List<String>, // URLs de fotos
    val dialogosMemorables: List<String>,
    val puntosGanados: Int
)