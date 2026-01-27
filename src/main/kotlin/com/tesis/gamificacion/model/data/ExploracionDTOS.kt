package com.tesis.gamificacion.model.data

import com.tesis.gamificacion.model.enums.CapaNivel
import java.time.LocalDateTime

data class PartidaDTO(
    val id: Long,
    val jugadorId: Long,
    val puntosExplorados: Int,
    val fotografiasCapturadas: Int,
    val dialogosRealizados: Int,
    val puntuacionTotal: Int,
    val completada: Boolean,
    val fechaInicio: LocalDateTime,
    val fechaFin: LocalDateTime? = null
)

data class MapaDTO(
    val partidaId: Long,
    val jugadorId: Long,
    val puntos: List<PuntoDTO>,
    val puntosExplorados: Int,
    val fotografiasCapturadas: Int,
    val dialogosRealizados: Int,
    val puntuacionTotal: Int,
    val completada: Boolean
)

data class PuntoDTO(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val coordenadaX: Double,
    val coordenadaY: Double,
    val imagenUrl: String,
    val explorado: Boolean,
    val capas: List<CapaDTO>
)

data class CapaDTO(
    val id: Long,
    val nivel: CapaNivel,
    val nombre: String,
    val desbloqueada: Boolean,
    val completada: Boolean,
    val narrativaLeida: Boolean,
    val fotografiasCompletadas: Int,
    val fotografiasRequeridas: Int,
    val dialogosRealizados: Int,
    val porcentaje: Double
)

data class ExplorarCapaRequest(
    val partidaId: Long,
    val puntoId: Int,
    val capaNivel: CapaNivel
)

data class ExplorarCapaResponse(
    val exito: Boolean,
    val capa: CapaDTO? = null,
    val narrativa: NarrativaDTO? = null,
    val objetivosFotograficos: List<ObjetivoFotoDTO>,
    val primerDescubrimiento: Boolean,
    val mensaje: String? = null
)

data class NarrativaDTO(
    val titulo: String,
    val texto: String,
    val nombreEspiritu: String
)

data class ObjetivoFotoDTO(
    val id: Int,
    val descripcion: String,
    val completada: Boolean
)

data class CapturarFotoRequest(
    val partidaId: Long,
    val progresoCapaId: Long,
    val objetivoId: Int,
    val imagenBase64: String? = null
)

data class CapturarFotoResponse(
    val exito: Boolean,
    val mensaje: String,
    val fotografiasCompletadas: Int = 0,
    val fotografiasRequeridas: Int = 0,
    val puntos: Int = 0,
    val descripcionIA: String? = null,
    val fotografiaValida: Boolean = false,
)

data class DialogarRequest(
    val partidaId: Long,
    val progresoCapaId: Long,
    val pregunta: String
)

data class DialogarResponse(
    val exito: Boolean,
    val respuesta: String,
    val nombreEspiritu: String,
    val dialogosRealizados: Int,
    val mensaje: String? = null
)