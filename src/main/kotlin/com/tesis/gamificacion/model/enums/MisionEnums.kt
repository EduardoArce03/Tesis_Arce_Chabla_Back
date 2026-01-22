package com.tesis.gamificacion.model.enums

enum class DificultadMision {
    FACIL,
    MEDIO,
    DIFICIL,
    EXPERTO
}

enum class EstadoMision {
    BLOQUEADA,
    DISPONIBLE,
    EN_PROGRESO,
    COMPLETADA,
    NO_INICIADA
}

enum class TipoFase {
    DIALOGO,           // Conversación con NPC
    QUIZ,              // Preguntas de opción múltiple
    VISITAR_PUNTO,     // Ir a un punto de interés
    BUSCAR_ARTEFACTO,  // Encontrar un artefacto específico
    EXPLORACION_LIBRE, // Explorar por X tiempo
    DECISION           // Elegir entre opciones (narrativa)
}

enum class RarezaInsignia {
    COMUN,
    RARA,
    EPICA,
    LEGENDARIA
}