// src/main/kotlin/com/tesis/gamificacion/model/enums/TipoNarrativa.kt
package com.tesis.gamificacion.model.enums

enum class TipoNarrativa {
    NARRATIVA_EDUCATIVA,     // Al perder vida
    PREGUNTA_RECUPERACION,   // Para recuperar vida
    HINT_CONTEXTUAL,         // Pista sin revelar
    DATO_CURIOSO,           // Al romper combo
    DIALOGO_CULTURAL        // Post-pareja perfecta
}

enum class TipoDialogo {
    PAREJA_PERFECTA,
    PRIMER_DESCUBRIMIENTO,
    COMBO_ACTIVO
}

enum class TipoHint {
    DESCRIPCION_CONTEXTUAL,  // "Busca un instrumento ceremonial"
    PISTA_VISUAL,           // "Tiene forma alargada"
    CATEGORIA_CULTURAL      // "Usado en festividades"
}