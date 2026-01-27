package com.tesis.gamificacion.model.request

import com.tesis.gamificacion.model.enums.CategoriaPunto
import com.tesis.gamificacion.model.enums.NivelCapa
import com.tesis.gamificacion.model.responses.NivelCapaDTO
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.time.LocalDateTime


data class NivelCapaDTOXD(
    val nivel: NivelCapa,
    val nombre: String,
    val descripcion: String,
    val desbloqueada: Boolean,
    val porcentajeDescubrimiento: Double,
    val fechaDesbloqueo: LocalDateTime? = null,
    val puntosDescubiertos: Int = 0,
    val puntosTotales: Int = 0
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
    val nivelDescubrimiento: NivelCapa?, // ✅ USAR NivelCapa, NO NivelDescubrimiento
    val visitas: Int,
    val tiempoExplorado: Int,
    val quizCompletado: Boolean,
    val artefactosDisponibles: Int,
    val artefactosEncontrados: Int
)

data class FotografiaObjetivoDTO(
    val id: Long,
    val puntoInteresId: Long,
    val nombrePunto: String,
    val descripcion: String,
    val rareza: com.tesis.gamificacion.model.enums.RarezaFoto,
    val puntosRecompensa: Int,
    val yaCapturada: Boolean
)

data class FotografiaCapturadaDTO(
    val id: Long,
    val objetivoId: Long,
    val descripcionObjetivo: String,
    val puntoInteresNombre: String,
    val imagenUrl: String,
    val descripcionUsuario: String?,
    val descripcionIA: String,
    val rareza: com.tesis.gamificacion.model.enums.RarezaFoto,
    val puntuacionIA: Double,
    val fecha: LocalDateTime
)

data class DialogoHistorialDTO(
    val id: Long,
    val pregunta: String,
    val respuesta: String,
    val nivelCapa: NivelCapa,
    val fecha: LocalDateTime,
    val puntoInteresNombre: String?
)

data class MisionDTO(
    val id: Long,
    val titulo: String,
    val descripcion: String,
    val tipo: com.tesis.gamificacion.model.enums.TipoMision,
    val estado: com.tesis.gamificacion.model.enums.EstadoMision,
    val progreso: Int,
    val objetivo: Int,
    val recompensaPuntos: Int,
    val fechaLimite: LocalDateTime?
)

data class RecompensaDTO(
    val tipo: String,
    val cantidad: Int,
    val descripcion: String
)

data class DescubrirPuntoRequest(
    @field:NotNull @field:Positive
    val partidaId: Long,

    @field:NotNull @field:Positive
    val puntoId: Long,

    val jugadorId: Long
)

data class CapturarFotografiaRequest(
    @field:NotNull @field:Positive
    val partidaId: Long,

    @field:NotNull @field:Positive
    val objetivoId: Long,

    @field:NotBlank
    val imagenBase64: String,

    val descripcionUsuario: String?,

    val jugadorId: Long
)

//data class DialogarEspirituRequest(
//    @field:NotNull @field:Positive
//    val partidaId: Long,
//
//    @field:NotNull
//    val nivelCapa: NivelCapa,
//
//    @field:NotBlank
//    val pregunta: String,
//
//    val puntoInteresId: Long?
//)
//
//data class CompletarMisionRequest(
//    @field:NotNull @field:Positive
//    val partidaId: Long,
//
//    @field:NotNull @field:Positive
//    val misionId: Long
//)

data class DescubrimientoPuntoResponse(
    val puntoId: Long,
    val nombrePunto: String,
    val yaDescubierto: Boolean,
    val nivelDescubierto: NivelCapa,
    val narrativaGenerada: String?,
    val recompensas: List<RecompensaDTO>,
    val nuevaCapaDesbloqueada: NivelCapaDTO?
)

data class CapturarFotografiaResponse(
    val exito: Boolean,
    val mensaje: String,
    val fotografiaId: Long?,
    val analisisIA: FotoAnalisisDTO?,
    val recompensas: List<RecompensaDTO>
)

data class FotoAnalisisDTO(
    val esValida: Boolean,
    val descripcionIA: String,
    val cumpleCriterios: Boolean,
    val confianza: Double
)

data class DialogoEspirituResponse(
    val exito: Boolean,
    val mensaje: String,
    val respuestaEspiritu: String?,
    val conocimientoDesbloqueado: String?
)

data class CompletarMisionResponse(
    val exito: Boolean,
    val mensaje: String,
    val recompensas: List<RecompensaDTO>,
    val nuevaCapaDesbloqueada: NivelCapaDTO?
)