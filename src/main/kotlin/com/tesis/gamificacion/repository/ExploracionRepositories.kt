package com.tesis.gamificacion.repository

import com.tesis.gamificacion.model.entities.DialogoHistorial
import com.tesis.gamificacion.model.entities.FotografiaCapturada
import com.tesis.gamificacion.model.entities.Partida
import com.tesis.gamificacion.model.entities.ProgresoCapa
import com.tesis.gamificacion.model.enums.CapaNivel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProgresoCapaRepository : JpaRepository<ProgresoCapa, Long> {

    // Buscar progreso específico por partida + punto + capa
    fun findByPartidaAndPuntoIdAndCapaNivel(
        partida: Partida,
        puntoId: Int,
        capaNivel: CapaNivel
    ): ProgresoCapa?

    // Obtener todas las capas de una partida
    fun findByPartida(partida: Partida): List<ProgresoCapa>

    // Obtener capas de un punto específico en una partida
    fun findByPartidaAndPuntoId(partida: Partida, puntoId: Int): List<ProgresoCapa>

    // Obtener capas completadas de una partida
    fun findByPartidaAndCompletadaTrue(partida: Partida): List<ProgresoCapa>

    // Verificar si existe una capa específica
    fun existsByPartidaAndPuntoIdAndCapaNivel(
        partida: Partida,
        puntoId: Int,
        capaNivel: CapaNivel
    ): Boolean

    // Contar capas completadas
    fun countByPartidaAndCompletadaTrue(partida: Partida): Long
}

@Repository
interface FotografiaCapturadaRepository : JpaRepository<FotografiaCapturada, Long> {

    // Buscar todas las fotos de un progreso de capa
    fun findByProgresoCapa(progresoCapa: ProgresoCapa): List<FotografiaCapturada>

    // Verificar si un objetivo ya fue capturado
    fun existsByProgresoCapaAndObjetivoId(
        progresoCapa: ProgresoCapa,
        objetivoId: Int
    ): Boolean

    // Buscar foto específica por objetivo
    fun findByProgresoCapaAndObjetivoId(
        progresoCapa: ProgresoCapa,
        objetivoId: Int
    ): FotografiaCapturada?

    // Contar fotos capturadas en un progreso
    fun countByProgresoCapa(progresoCapa: ProgresoCapa): Long

    // Contar fotos validadas por IA
    fun countByProgresoCapaAndValidadaPorIATrue(progresoCapa: ProgresoCapa): Long

    // Obtener fotos ordenadas por fecha
    fun findByProgresoCapaOrderByFechaDesc(progresoCapa: ProgresoCapa): List<FotografiaCapturada>
}

@Repository
interface DialogoHistorialRepository : JpaRepository<DialogoHistorial, Long> {

    // Buscar todos los diálogos de un progreso de capa
    fun findByProgresoCapa(progresoCapa: ProgresoCapa): List<DialogoHistorial>

    // Obtener diálogos ordenados por fecha (más reciente primero)
    fun findByProgresoCapaOrderByFechaDesc(progresoCapa: ProgresoCapa): List<DialogoHistorial>

    // Obtener últimos N diálogos (para contexto de IA)
    fun findTop5ByProgresoCapaOrderByFechaDesc(progresoCapa: ProgresoCapa): List<DialogoHistorial>

    // Contar diálogos realizados en una capa
    fun countByProgresoCapa(progresoCapa: ProgresoCapa): Long

    // Buscar diálogos que contengan una palabra clave
    fun findByProgresoCapaAndPreguntaUsuarioContainingIgnoreCase(
        progresoCapa: ProgresoCapa,
        palabraClave: String
    ): List<DialogoHistorial>
}