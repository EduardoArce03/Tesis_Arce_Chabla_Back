package com.tesis.gamificacion.repository
import com.tesis.gamificacion.model.entities.*
import com.tesis.gamificacion.model.enums.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface PuntoInteresRepository : JpaRepository<PuntoInteres, Long> {
    fun findByActivoTrue(): List<PuntoInteres>
    fun findByNivelRequeridoLessThanEqualAndActivoTrue(nivel: Int): List<PuntoInteres>
    fun findByCategoriaAndActivoTrue(categoria: CategoriaPunto): List<PuntoInteres>
}

@Repository
interface ProgresoExploracionRepository : JpaRepository<ProgresoExploracion, Long> {
    fun findByUsuarioId(usuarioId: Long): ProgresoExploracion?
}

@Repository
interface DescubrimientoRepository : JpaRepository<Descubrimiento, Long> {
    fun findByUsuarioIdAndPuntoId(usuarioId: Long, puntoId: Long): Descubrimiento?
    fun findByUsuarioId(usuarioId: Long): List<Descubrimiento>
    fun countByUsuarioId(usuarioId: Long): Long

    @Query("SELECT d.puntoId FROM Descubrimiento d WHERE d.usuarioId = :usuarioId")
    fun findPuntosDescubiertosByUsuarioId(usuarioId: Long): List<Long>
}

@Repository
interface ArtefactoRepository : JpaRepository<Artefacto, Long> {
    fun findByPuntoInteresIdAndActivoTrue(puntoInteresId: Long): List<Artefacto>
    fun findByActivoTrue(): List<Artefacto>
    fun countByActivoTrue(): Long
}

@Repository
interface UsuarioArtefactoRepository : JpaRepository<UsuarioArtefacto, Long> {
    fun findByUsuarioId(usuarioId: Long): List<UsuarioArtefacto>
    fun findByUsuarioIdAndArtefactoId(usuarioId: Long, artefactoId: Long): UsuarioArtefacto?
    fun countByUsuarioId(usuarioId: Long): Long
}

@Repository
interface MisionExploracionRepository : JpaRepository<MisionExploracion, Long> {
    fun findByActivaTrueAndNivelRequeridoLessThanEqual(nivelUsuario: Int): List<MisionExploracion>
    fun findByActivaTrue(): List<MisionExploracion>
}

@Repository
interface UsuarioMisionRepository : JpaRepository<UsuarioMision, Long> {
    fun findByUsuarioId(usuarioId: Long): List<UsuarioMision>
    fun findByUsuarioIdAndMisionId(usuarioId: Long, misionId: Long): UsuarioMision?
    fun findByUsuarioIdAndEstado(usuarioId: Long, estado: EstadoMision): List<UsuarioMision>
    fun countByUsuarioIdAndEstado(usuarioId: Long, estado: EstadoMision): Long
}

@Repository
interface PreguntaQuizRepository : JpaRepository<PreguntaQuiz, Long> {
    fun findByPuntoInteresIdAndActivaTrue(puntoInteresId: Long): List<PreguntaQuiz>
}