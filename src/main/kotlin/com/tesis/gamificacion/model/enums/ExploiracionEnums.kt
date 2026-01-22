package com.tesis.gamificacion.model.enums

enum class CategoriaPunto {
    TEMPLO,
    PLAZA,
    VIVIENDA,
    DEPOSITO,
    OBSERVATORIO,
    CEREMONIAL,
    CAMINO,
    FUENTE
}

enum class NivelDescubrimiento {
    NO_VISITADO,
    BRONCE,    // 1ra visita
    PLATA,     // 2da visita + quiz
    ORO        // 3ra visita + artefacto encontrado
}

enum class CategoriaArtefacto {
    CERAMICA,
    TEXTIL,
    METAL,
    PIEDRA,
    HERRAMIENTA,
    ORNAMENTO,
    RITUAL
}

enum class TipoMision {
    DESCUBRIR_PUNTOS,      // Visita X puntos específicos
    ENCONTRAR_ARTEFACTOS,  // Encuentra X artefactos
    COMPLETAR_QUIZ,        // Responde quizzes correctamente
    TIEMPO_EXPLORACION,    // Explora por X minutos
    SECUENCIAL,             // Visita puntos en orden específico,
    NARRATIVA
}