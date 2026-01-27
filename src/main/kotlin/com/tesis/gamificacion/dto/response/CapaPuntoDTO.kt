// src/main/kotlin/com/tesis/gamificacion/model/responses/CapaPuntoDTO.kt
package com.tesis.gamificacion.model.responses

import com.tesis.gamificacion.model.enums.NivelCapa
import com.tesis.gamificacion.model.enums.NivelDescubrimiento
import com.tesis.gamificacion.model.request.RecompensaDTO

/**
 * DTO que representa el estado de UNA capa específica de un punto
 */
data class CapaPuntoDTO(
    val id: Long?, // ⬅️ AGREGAR PARA TRACKING
    val nivelCapa: NivelCapa,
    val nombre: String,
    val descripcion: String,
    val desbloqueada: Boolean,
    val nivelDescubrimiento: NivelDescubrimiento, // NO_VISITADO, BRONCE, PLATA, ORO
    val porcentajeCompletitud: Double, // 0-100

    // Estado de actividades dentro de la capa
    val narrativaLeida: Boolean,
    val narrativaTexto: String?,

    val fotografiasRequeridas: Int,
    val fotografiasCompletadas: Int,
    val fotografiasPendientes: List<FotografiaObjetivoSimpleDTO>,

    val dialogosRealizados: Int,
    val tieneDialogoDisponible: Boolean,

    val misionAsociada: com.tesis.gamificacion.model.request.MisionDTO?,
    val misionCompletada: Boolean,

    // Recompensas
    val puntosGanados: Int,
    val recompensaFinal: RecompensaDTO?,

    val completada: Boolean
)

data class FotografiaObjetivoSimpleDTO(
    val id: Long,
    val descripcion: String,
    val rareza: String,
    val completada: Boolean
)

/**
 * Request para entrar/descubrir una capa específica de un punto
 */
data class DescubrirCapaPuntoRequest(
    val usuarioId: Long,
    val partidaId: Long,
    val puntoId: Long,
    val nivelCapa: NivelCapa
)

/**
 * Response al entrar a una capa
 */
data class DescubrirCapaPuntoResponse(
    val exito: Boolean,
    val capa: CapaPuntoDTO?,
    val narrativaNueva: Boolean,
    val mensaje: String
)