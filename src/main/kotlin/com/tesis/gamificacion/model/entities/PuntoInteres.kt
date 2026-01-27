package com.tesis.gamificacion.model.entities

import com.tesis.gamificacion.model.enums.CategoriaArtefacto
import com.tesis.gamificacion.model.enums.CategoriaPunto
import com.tesis.gamificacion.model.enums.DificultadMision
import com.tesis.gamificacion.model.enums.EstadoMision
import com.tesis.gamificacion.model.enums.NivelCapa
import com.tesis.gamificacion.model.enums.RarezaFoto
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

// ========================================
// PUNTOS DE INTERÉS (CONFIGURACIÓN)
// ========================================

// ========================================
// CAPAS TEMPORALES (CONFIGURACIÓN)
// Cada punto tiene 4 capas: SUPERFICIE, INCA, CANARI, ANCESTRAL
// ========================================

// ========================================
// CAPA DESCUBRIMIENTO (PROGRESO USUARIO EN CADA CAPA)
// Representa el progreso del usuario en cada nivel temporal
// ========================================


// ========================================
// PUNTO DESCUBRIMIENTO (USUARIO DESCUBRE UN PUNTO)
// ========================================

// ========================================
// FOTOGRAFÍA - OBJETIVOS
// ========================================

// ========================================
// DIÁLOGOS CON ESPÍRITUS
// ========================================

// ========================================
// MISIONES
// ========================================


// ========================================
// ARTEFACTOS
// ========================================

// ========================================
// PREGUNTAS QUIZ
// ========================================