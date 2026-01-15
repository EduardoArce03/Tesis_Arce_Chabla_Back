// src/main/kotlin/com/tesis/gamificacion/config/PuzzleDataLoader.kt
package com.tesis.gamificacion.config

import com.tesis.gamificacion.model.entities.ImagenPuzzle
import com.tesis.gamificacion.model.enums.CategoriasCultural
import com.tesis.gamificacion.repository.ImagenPuzzleRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class PuzzleDataLoader(
    private val imagenPuzzleRepository: ImagenPuzzleRepository
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (imagenPuzzleRepository.count() == 0L) {
            println("📦 Cargando imágenes de puzzle...")
            cargarImagenesPuzzle()
            println("✅ ${imagenPuzzleRepository.count()} imágenes cargadas")
        } else {
            println("ℹ️ Imágenes de puzzle ya cargadas (${imagenPuzzleRepository.count()} registros)")
        }
    }

    private fun cargarImagenesPuzzle() {
        val imagenes = listOf(
            // ==================== LUGARES ====================
            ImagenPuzzle(
                titulo = "Ruinas de Ingapirca",
                nombreKichwa = "Ingapirca",
                categoria = CategoriasCultural.LUGARES,
                imagenUrl = "https://images.unsplash.com/photo-1531065208531-4036c0dba3ca?w=800&q=80",
                descripcionCompleta = "Ingapirca es el sitio arqueológico más importante del Ecuador, construido por los Cañaris e Incas como centro ceremonial y astronómico. Sus piedras perfectamente talladas demuestran el avanzado conocimiento arquitectónico de estas culturas.",
                dificultadMinima = 3,
                dificultadMaxima = 6,
                ordenDesbloqueo = 1  // ⬅️ Primera imagen (desbloqueada por defecto)
            ),

            ImagenPuzzle(
                titulo = "Los Andes Cañaris",
                nombreKichwa = "Cañari Urcu",
                categoria = CategoriasCultural.LUGARES,
                imagenUrl = "https://images.unsplash.com/photo-1589802829985-817e51171b92?w=800&q=80",
                descripcionCompleta = "Las montañas sagradas de la región Cañari, hogar ancestral de esta cultura milenaria. Estos picos han sido testigos de ceremonias ancestrales durante siglos.",
                dificultadMinima = 3,
                dificultadMaxima = 5,
                ordenDesbloqueo = 2
            ),

            ImagenPuzzle(
                titulo = "Páramo Andino",
                nombreKichwa = "Jalca",
                categoria = CategoriasCultural.LUGARES,
                imagenUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800&q=80",
                descripcionCompleta = "El ecosistema de páramo es sagrado para los pueblos andinos y fuente de agua para las comunidades. Los Cañaris consideraban estos lugares como espacios de conexión espiritual.",
                dificultadMinima = 4,
                dificultadMaxima = 6,
                ordenDesbloqueo = 7
            ),

            // ==================== VESTIMENTA ====================
            ImagenPuzzle(
                titulo = "Tejido Ancestral",
                nombreKichwa = "Awasqa",
                categoria = CategoriasCultural.VESTIMENTA,
                imagenUrl = "https://images.unsplash.com/photo-1576435728678-68d0fbf94e91?w=800&q=80",
                descripcionCompleta = "Los textiles Cañaris representan siglos de tradición artesanal y simbolismo cultural. Cada diseño cuenta una historia y mantiene viva la identidad del pueblo.",
                dificultadMinima = 3,
                dificultadMaxima = 5,
                ordenDesbloqueo = 3
            ),

            ImagenPuzzle(
                titulo = "Cerámica Cañari",
                nombreKichwa = "Mankakuna",
                categoria = CategoriasCultural.VESTIMENTA,
                imagenUrl = "https://images.unsplash.com/photo-1578749556568-bc2c40e68b61?w=800&q=80",
                descripcionCompleta = "La cerámica Cañari se caracteriza por sus diseños geométricos y uso ceremonial. Estas piezas eran utilizadas en rituales importantes y celebraciones.",
                dificultadMinima = 3,
                dificultadMaxima = 5,
                ordenDesbloqueo = 6
            ),

            ImagenPuzzle(
                titulo = "Orfebrería Cañari",
                nombreKichwa = "Qullqi Rurana",
                categoria = CategoriasCultural.VESTIMENTA,
                imagenUrl = "https://images.unsplash.com/photo-1515562141207-7a88fb7ce338?w=800&q=80",
                descripcionCompleta = "Los Cañaris fueron expertos orfebres, creando joyas ceremoniales de gran valor cultural. Sus trabajos en oro y plata eran reconocidos en toda la región andina.",
                dificultadMinima = 3,
                dificultadMaxima = 5,
                ordenDesbloqueo = 8
            ),

            // ==================== MÚSICA ====================
            ImagenPuzzle(
                titulo = "Instrumentos Andinos",
                nombreKichwa = "Takikuna",
                categoria = CategoriasCultural.MUSICA,
                imagenUrl = "https://images.unsplash.com/photo-1511379938547-c1f69419868d?w=800&q=80",
                descripcionCompleta = "Los instrumentos musicales tradicionales como el rondador y la quena son parte esencial de las celebraciones Cañaris. La música conecta a la comunidad con sus ancestros.",
                dificultadMinima = 4,
                dificultadMaxima = 6,
                ordenDesbloqueo = 4
            ),

            // ==================== FESTIVIDADES ====================
            ImagenPuzzle(
                titulo = "Inti Raymi",
                nombreKichwa = "Inti Raymi",
                categoria = CategoriasCultural.FESTIVIDADES,
                imagenUrl = "https://images.unsplash.com/photo-1533929736458-ca588d08c8be?w=800&q=80",
                descripcionCompleta = "La fiesta del sol es una de las celebraciones más importantes de la cultura andina, heredada por los Cañaris. Se celebra durante el solsticio de verano en junio.",
                dificultadMinima = 4,
                dificultadMaxima = 6,
                ordenDesbloqueo = 5
            ),

            // ==================== IMÁGENES BONUS ====================
            ImagenPuzzle(
                titulo = "Laguna de Culebrillas",
                nombreKichwa = "Culebrillas Qucha",
                categoria = CategoriasCultural.LUGARES,
                imagenUrl = "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800&q=80",
                descripcionCompleta = "Laguna sagrada en el territorio Cañari, lugar de peregrinación y ceremonias ancestrales. Se dice que en sus aguas habitan espíritus protectores.",
                dificultadMinima = 4,
                dificultadMaxima = 6,
                ordenDesbloqueo = 9
            ),

            ImagenPuzzle(
                titulo = "Mercado Tradicional",
                nombreKichwa = "Qhatu",
                categoria = CategoriasCultural.LUGARES,
                imagenUrl = "https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=800&q=80",
                descripcionCompleta = "Los mercados tradicionales son centros de intercambio cultural y económico donde se mantienen vivas las tradiciones comerciales ancestrales.",
                dificultadMinima = 3,
                dificultadMaxima = 5,
                ordenDesbloqueo = 10
            ),

            ImagenPuzzle(
                titulo = "Danza Ritual",
                nombreKichwa = "Tushuy",
                categoria = CategoriasCultural.FESTIVIDADES,
                imagenUrl = "https://images.unsplash.com/photo-1504609773096-104ff2c73ba4?w=800&q=80",
                descripcionCompleta = "Las danzas rituales Cañaris representan historias ancestrales y conexiones con la naturaleza. Cada movimiento tiene un significado profundo.",
                dificultadMinima = 4,
                dificultadMaxima = 6,
                ordenDesbloqueo = 11
            ),

            ImagenPuzzle(
                titulo = "Chakana Sagrada",
                nombreKichwa = "Chakana",
                categoria = CategoriasCultural.FESTIVIDADES,
                imagenUrl = "https://images.unsplash.com/photo-1518837695005-2083093ee35b?w=800&q=80",
                descripcionCompleta = "La chakana o cruz andina es un símbolo fundamental en la cosmovisión Cañari, representando la conexión entre el mundo terrenal y espiritual.",
                dificultadMinima = 5,
                dificultadMaxima = 6,
                ordenDesbloqueo = 12
            )
        )

        imagenPuzzleRepository.saveAll(imagenes)
    }
}