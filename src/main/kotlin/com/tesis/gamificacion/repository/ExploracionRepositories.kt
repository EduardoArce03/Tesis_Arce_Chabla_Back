// ========================================
// PUNTOS DE INTERÉS
// ========================================

package com.tesis.gamificacion.repository

import com.tesis.gamificacion.model.entities.*
import com.tesis.gamificacion.model.entities.PuntoInteres
import com.tesis.gamificacion.model.entities.UsuarioMision
import com.tesis.gamificacion.model.enums.CategoriaPunto
import com.tesis.gamificacion.model.enums.NivelCapa
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface PuntoInteresRepository : JpaRepository<PuntoInteres, Long> {
    fun findByActivoTrue(): List<PuntoInteres>
    fun findByNivelMinimoLessThanEqualAndActivoTrue(nivel: Int): List<PuntoInteres>
    fun findByCategoriaAndActivoTrue(categoria: CategoriaPunto): List<PuntoInteres>
    fun findByOrdenDesbloqueo(orden: Int): PuntoInteres?

    @Query("SELECT COUNT(p) FROM PuntoInteres p WHERE p.nivelMinimo <= :nivel AND p.activo = true")
    fun countByNivelMinimo(nivel: Int): Int
}

// ========================================
// CAPAS TEMPORALES (CONFIGURACIÓN)
// ========================================

@Repository
interface CapaTemporalRepository : JpaRepository<CapaTemporal, Long> {
    fun findByPuntoInteresIdAndNivel(puntoId: Long, nivel: com.tesis.gamificacion.model.enums.NivelCapa): CapaTemporal?
    fun findAllByPuntoInteresId(puntoId: Long): List<CapaTemporal>
    fun findByNivel(nivel: com.tesis.gamificacion.model.enums.NivelCapa): List<CapaTemporal>
    fun findByPuntoInteres(punto: PuntoInteres): List<CapaTemporal>
    fun findByPuntoInteresAndNivel(puntoInteres: PuntoInteres, nivel: NivelCapa): CapaTemporal?
    fun findByPuntoInteresOrderByNivelAsc(punto: PuntoInteres): List<CapaTemporal>

}

// ========================================
// PROGRESO DE EXPLORACIÓN
// ========================================

@Repository
interface ProgresoExploracionRepository : JpaRepository<ProgresoExploracion, Long> {
    fun findByUsuarioId(usuarioId: Long): ProgresoExploracion?
    fun findByPartidaId(partidaId: Long): ProgresoExploracion?
    fun existsByUsuarioId(usuarioId: Long): Boolean
    fun existsByPartidaId(partidaId: Long): Boolean
    fun findByPartidaIdAndUsuarioId(partidaId: Long, usuarioId: Long): Optional<ProgresoExploracion>
}

// ========================================
// CAPAS DESCUBRIMIENTO (PROGRESO USUARIO)
// ========================================

@Repository
interface CapaDescubrimientoRepository : JpaRepository<CapaDescubrimiento, Long> {
    fun findByProgreso(progreso: ProgresoExploracion): List<CapaDescubrimiento>

    fun findByProgresoAndNivel(
        progreso: ProgresoExploracion,
        nivel: com.tesis.gamificacion.model.enums.NivelCapa
    ): CapaDescubrimiento?

    fun findByProgresoAndDesbloqueadaTrue(
        progreso: ProgresoExploracion
    ): List<CapaDescubrimiento>

    fun countByProgresoAndDesbloqueadaTrue(
        progreso: ProgresoExploracion
    ): Long

    // En CapaDescubrimientoRepository.kt
    fun findByProgresoOrderByNivelAsc(progreso: ProgresoExploracion): List<CapaDescubrimiento>
    fun findByProgresoOrderByNivelDesc(progreso: ProgresoExploracion): List<CapaDescubrimiento>

    // En DialogoHistorialRepository.kt
    //fun countByProgresoAndCapa(progreso: ProgresoExploracion, capa: CapaDescubrimiento): Long

    // En FotografiaCapturadaRepository.kt
    //fun countByProgresoAndObjetivoIn(progreso: ProgresoExploracion, objetivos: List<FotografiaObjetivo>): Long
}

// ========================================
// DESCUBRIMIENTOS DE PUNTOS
// ========================================

@Repository
interface PuntoDescubrimientoRepository : JpaRepository<PuntoDescubrimiento, Long> {
    fun findByUsuarioIdAndPuntoInteresId(
        usuarioId: Long,
        puntoInteresId: Long
    ): PuntoDescubrimiento?

    fun findByProgresoAndPuntoInteres(
        progreso: ProgresoExploracion,
        punto: PuntoInteres
    ): PuntoDescubrimiento?

    fun findByProgreso(progreso: ProgresoExploracion): List<PuntoDescubrimiento>

    fun findByUsuarioId(usuarioId: Long): List<PuntoDescubrimiento>

    fun countByUsuarioId(usuarioId: Long): Long

    fun countByProgreso(progreso: ProgresoExploracion): Long

    fun findByProgresoAndPuntoInteresAndNivelDescubrimiento(
        progreso: ProgresoExploracion,
        puntoInteres: PuntoInteres,
        nivelDescubrimiento: NivelCapa
    ): PuntoDescubrimiento?

    @Query("""
        SELECT d.puntoInteres.id 
        FROM PuntoDescubrimiento d 
        WHERE d.usuarioId = :usuarioId
    """)
    fun findPuntosDescubiertosByUsuarioId(usuarioId: Long): List<Long>

    fun existsByProgresoAndPuntoInteres(
        progreso: ProgresoExploracion,
        punto: PuntoInteres
    ): Boolean
}

// ========================================
// FOTOGRAFÍA - OBJETIVOS
// ========================================

@Repository
interface FotografiaObjetivoRepository : JpaRepository<FotografiaObjetivo, Long> {
    fun findByCapaTemporalId(capaTemporalId: Long): List<FotografiaObjetivo>

    fun findByPuntoInteresId(puntoInteresId: Long): List<FotografiaObjetivo>
    //fun countByProgresoAndObjetivoIn(progreso: ProgresoExploracion, objetivos: List<FotografiaObjetivo>): Long

    fun findByNivelRequeridoLessThanEqualAndActivoTrue(
        nivel: com.tesis.gamificacion.model.enums.NivelCapa
    ): List<FotografiaObjetivo>

    fun findByActivoTrue(): List<FotografiaObjetivo>

    fun findByPuntoInteresIdAndNivelRequerido(
        puntoInteresId: Long,
        nivel: com.tesis.gamificacion.model.enums.NivelCapa
    ): List<FotografiaObjetivo>

    fun findByNivelRequeridoLessThanEqual(nivelActual: NivelCapa): List<FotografiaObjetivo>
}

// ========================================
// FOTOGRAFÍA - CAPTURADAS
// ========================================

@Repository
interface FotografiaCapturadaRepository : JpaRepository<FotografiaCapturada, Long> {
    fun findByProgresoOrderByFechaDesc(
        progreso: ProgresoExploracion
    ): List<FotografiaCapturada>

    fun findByProgresoAndObjetivo(
        progreso: ProgresoExploracion,
        objetivo: FotografiaObjetivo
    ): FotografiaCapturada?

    fun existsByProgresoAndObjetivo(
        progreso: ProgresoExploracion,
        objetivo: FotografiaObjetivo
    ): Boolean

    fun countByProgreso(progreso: ProgresoExploracion): Long

    fun countByProgresoAndRarezaObtenida(
        progreso: ProgresoExploracion,
        rareza: com.tesis.gamificacion.model.enums.RarezaFoto
    ): Long

    @Query("""
        SELECT f FROM FotografiaCapturada f 
        WHERE f.progreso = :progreso 
        AND f.rarezaObtenida IN :rarezas
        ORDER BY f.fecha DESC
    """)
    fun findByProgresoAndRarezasIn(
        progreso: ProgresoExploracion,
        rarezas: List<com.tesis.gamificacion.model.enums.RarezaFoto>
    ): List<FotografiaCapturada>

    fun countByProgresoAndObjetivoIn(progreso: ProgresoExploracion, objetivos: List<FotografiaObjetivo>): Long
}

// ========================================
// DIÁLOGOS
// ========================================

@Repository
interface DialogoHistorialRepository : JpaRepository<DialogoHistorial, Long> {
    fun findByProgresoAndCapaOrderByFechaDesc(
        progreso: ProgresoExploracion,
        capa: CapaDescubrimiento
    ): List<DialogoHistorial>

    fun findByProgresoOrderByFechaDesc(
        progreso: ProgresoExploracion
    ): List<DialogoHistorial>

    fun countByProgresoAndCapa(
        progreso: ProgresoExploracion,
        capa: CapaDescubrimiento
    ): Long

    fun countByProgreso(progreso: ProgresoExploracion): Long

    @Query("""
        SELECT d FROM DialogoHistorial d 
        WHERE d.progreso = :progreso 
        AND d.capa.nivel = :nivel
        ORDER BY d.fecha DESC
    """)
    fun findByProgresoAndNivelCapa(
        progreso: ProgresoExploracion,
        nivel: com.tesis.gamificacion.model.enums.NivelCapa
    ): List<DialogoHistorial>
}

// ========================================
// MISIONES
// ========================================

//@Repository
//interface MisionRepository : JpaRepository<Mision, Long> {
//    fun findByActivaTrueOrderByOrden(): List<Mision>
//
//    fun findByNivelMinimoLessThanEqualAndActivaTrue(
//        nivelMinimo: Int
//    ): List<Mision>
//
//    fun findByNivelMinimoAndActivaTrue(nivelMinimo: Int): List<Mision>
//
//    fun findByCapaTemporalId(capaTemporalId: Long): Mision?
//
//    fun findByActivaTrue(): List<Mision>
//}

@Repository
interface MisionProgresoRepository : JpaRepository<MisionProgreso, Long> {
    fun findByProgreso(progreso: ProgresoExploracion): List<MisionProgreso>

    fun findByProgresoAndMisionId(
        progreso: ProgresoExploracion,
        misionId: Long
    ): MisionProgreso?

    fun findByProgresoAndEstado(
        progreso: ProgresoExploracion,
        estado: com.tesis.gamificacion.model.enums.EstadoMision
    ): List<MisionProgreso>

    fun countByProgresoAndEstado(
        progreso: ProgresoExploracion,
        estado: com.tesis.gamificacion.model.enums.EstadoMision
    ): Long

    fun existsByProgresoAndMisionId(
        progreso: ProgresoExploracion,
        misionId: Long
    ): Boolean
}

// ========================================
// FASES DE MISIÓN (SI LAS USAS)
// ========================================

//@Repository
//interface FaseMisionRepository : JpaRepository<FaseMision, Long> {
//    fun findByMisionIdOrderByNumeroFase(misionId: Long): List<FaseMision>
//
//    fun findByMisionIdAndNumeroFase(misionId: Long, numeroFase: Int): FaseMision?
//
//    fun countByMisionId(misionId: Long): Long
//}
//
//@Repository
//interface PreguntaFaseRepository : JpaRepository<PreguntaFase, Long> {
//    fun findByFaseIdOrderByOrden(faseId: Long): List<PreguntaFase>
//
//    fun countByFaseId(faseId: Long): Long
//}

// ========================================
// INSIGNIAS (SI LAS USAS)
// ========================================

//@Repository
//interface InsigniaRepository : JpaRepository<Insignia, Long> {
//    fun findByActivaTrue(): List<Insignia>
//
//    fun findByCodigo(codigo: String): Insignia?
//}
//
//@Repository
//interface UsuarioInsigniaRepository : JpaRepository<UsuarioInsignia, Long> {
//    fun findByUsuarioId(usuarioId: Long): List<UsuarioInsignia>
//
//    fun findByUsuarioIdAndInsigniaId(
//        usuarioId: Long,
//        insigniaId: Long
//    ): UsuarioInsignia?
//
//    fun countByUsuarioId(usuarioId: Long): Long
//
//    fun existsByUsuarioIdAndInsigniaId(
//        usuarioId: Long,
//        insigniaId: Long
//    ): Boolean
//}

//@Repository
//interface MisionInsigniaRepository : JpaRepository<MisionInsignia, Long> {
//    fun findByMisionId(misionId: Long): List<MisionInsignia>
//
//    fun findByInsigniaId(insigniaId: Long): List<MisionInsignia>
//}

// ========================================
// ARTEFACTOS
// ========================================

@Repository
interface ArtefactoRepository : JpaRepository<Artefacto, Long> {
    fun findByPuntoInteresIdAndActivoTrue(puntoInteresId: Long): List<Artefacto>

    fun findByActivoTrue(): List<Artefacto>

    fun countByActivoTrue(): Long

    fun findByCategoriaAndActivoTrue(
        categoria: com.tesis.gamificacion.model.enums.CategoriaArtefacto
    ): List<Artefacto>
}

@Repository
interface UsuarioArtefactoRepository : JpaRepository<UsuarioArtefacto, Long> {
    fun findByProgreso(progreso: ProgresoExploracion): List<UsuarioArtefacto>

    fun findByProgresoAndArtefacto(
        progreso: ProgresoExploracion,
        artefacto: Artefacto
    ): UsuarioArtefacto?

    fun countByProgreso(progreso: ProgresoExploracion): Long

    fun existsByProgresoAndArtefacto(
        progreso: ProgresoExploracion,
        artefacto: Artefacto
    ): Boolean
}

// ========================================
// PREGUNTAS QUIZ
// ========================================

@Repository
interface PreguntaQuizRepository : JpaRepository<PreguntaQuiz, Long> {
    fun findByPuntoInteresIdAndActivaTrue(puntoInteresId: Long): List<PreguntaQuiz>

    fun findByPuntoInteresIdAndNivelCapaAndActivaTrue(
        puntoInteresId: Long,
        nivelCapa: com.tesis.gamificacion.model.enums.NivelCapa
    ): List<PreguntaQuiz>

    fun findByActivaTrue(): List<PreguntaQuiz>

    fun countByPuntoInteresId(puntoInteresId: Long): Long
}

// ========================================
// USUARIO MISIÓN (SISTEMA ACTUAL DE MISIONES)
// Si usas el sistema de UsuarioMision existente
// ========================================

@Repository
interface UsuarioMisionRepository : JpaRepository<UsuarioMision, Long> {
    fun findByUsuarioId(usuarioId: Long): List<UsuarioMision>

    fun findByUsuarioIdAndMisionId(usuarioId: Long, misionId: Long): UsuarioMision?

    fun findByUsuarioIdAndEstado(
        usuarioId: Long,
        estado: com.tesis.gamificacion.model.enums.EstadoMision
    ): List<UsuarioMision>

    fun countByUsuarioIdAndEstado(
        usuarioId: Long,
        estado: com.tesis.gamificacion.model.enums.EstadoMision
    ): Long

    fun existsByUsuarioIdAndMisionId(usuarioId: Long, misionId: Long): Boolean
}