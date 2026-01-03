package com.tesis.gamificacion.repository

import com.tesis.gamificacion.model.entities.*
import com.tesis.gamificacion.model.enums.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface MisionRepository : JpaRepository<Mision, Long> {
    fun findByActivaTrueOrderByOrden(): List<Mision>
    fun findByNivelMinimoLessThanEqualAndActivaTrue(nivelUsuario: Int): List<Mision>
}

@Repository
interface FaseMisionRepository : JpaRepository<FaseMision, Long> {
    fun findByMisionIdOrderByNumeroFase(misionId: Long): List<FaseMision>
    fun findByMisionIdAndNumeroFase(misionId: Long, numeroFase: Int): FaseMision?
}

@Repository
interface PreguntaFaseRepository : JpaRepository<PreguntaFase, Long> {
    fun findByFaseIdOrderByOrden(faseId: Long): List<PreguntaFase>
}

@Repository
interface InsigniaRepository : JpaRepository<Insignia, Long> {
    fun findByCodigoAndActivaTrue(codigo: String): Insignia?
    fun findByActivaTrue(): List<Insignia>
}

@Repository
interface UsuarioInsigniaRepository : JpaRepository<UsuarioInsignia, Long> {
    fun findByUsuarioId(usuarioId: Long): List<UsuarioInsignia>
    fun findByUsuarioIdAndInsigniaId(usuarioId: Long, insigniaId: Long): UsuarioInsignia?
    fun countByUsuarioId(usuarioId: Long): Long
}

@Repository
interface MisionInsigniaRepository : JpaRepository<MisionInsignia, Long> {
    fun findByMisionId(misionId: Long): List<MisionInsignia>
}