package com.tesis.gamificacion.service

import com.tesis.gamificacion.dto.response.ElementoCulturalResponse
import com.tesis.gamificacion.model.enums.TipoDialogo
import com.tesis.gamificacion.model.enums.TipoHint
import com.tesis.gamificacion.model.responses.DialogoCulturalResponse
import com.tesis.gamificacion.model.responses.NarrativaEducativaResponse
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

@Service
class CacheLlamadaIAService(
    private val modeloService: ModeloService,
    private val elementoCulturalService: ElementoCulturalService,
    private val restTemplate: RestTemplate
) {

    // Caché en memoria (por partida)
    private val cacheNarrativas = ConcurrentHashMap<String, NarrativaEducativaResponse>()
    private val cacheDialogos = ConcurrentHashMap<String, DialogoCulturalResponse>()
    private val cacheHints = ConcurrentHashMap<String, String>()

    /**
     * Pre-carga narrativas al iniciar partida (async)
     */
    @Async
    fun precargarNarrativas(partidaId: Long, elementos: List<ElementoCulturalResponse>) {
        elementos.forEach { elemento ->
            val key = "narrativa_${partidaId}_${elemento.id}"

            if (!cacheNarrativas.containsKey(key)) {
                val narrativa = modeloService.generarNarrativaEducativa(
                    imagenBytes = obtenerImagenBytes(elemento.imagenUrl),
                    nombreArchivo = "${elemento.nombreEspanol}.jpg",
                    conceptoClave = elemento.categoria.name,
                    nombreKichwa = elemento.nombreKichwa,
                    nombreEspanol = elemento.nombreEspanol
                )

                if (narrativa != null) {
                    cacheNarrativas[key] = narrativa
                }
            }
        }
    }

    /**
     * Obtiene narrativa educativa (caché o genera)
     */
    fun obtenerNarrativaEducativa(partidaId: Long, elementoId: Long): NarrativaEducativaResponse {
        val key = "narrativa_${partidaId}_${elementoId}"

        return cacheNarrativas.getOrPut(key) {
            val elemento = elementoCulturalService.obtenerPorId(elementoId)

            modeloService.generarNarrativaEducativa(
                imagenBytes = obtenerImagenBytes(elemento.imagenUrl),
                nombreArchivo = "${elemento.nombreEspanol}.jpg",
                conceptoClave = elemento.categoria.name,
                nombreKichwa = elemento.nombreKichwa,
                nombreEspanol = elemento.nombreEspanol
            ) ?: throw IllegalStateException("No se pudo generar narrativa")
        }
    }

    /**
     * Obtiene diálogo cultural (caché o genera)
     */
    fun obtenerDialogoCultural(
        partidaId: Long,
        elementoId: Long,
        tipoDialogo: TipoDialogo
    ): DialogoCulturalResponse {
        val key = "dialogo_${partidaId}_${elementoId}_${tipoDialogo.name}"

        return cacheDialogos.getOrPut(key) {
            val elemento = elementoCulturalService.obtenerPorId(elementoId)

            modeloService.generarDialogoCultural(
                imagenBytes = obtenerImagenBytes(elemento.imagenUrl),
                nombreArchivo = "${elemento.nombreEspanol}.jpg",
                conceptoClave = elemento.categoria.name,
                tipoDialogo = tipoDialogo
            ) ?: throw IllegalStateException("No se pudo generar diálogo")
        }
    }

    /**
     * Obtiene hint (caché o genera)
     */
    fun obtenerHint(
        partidaId: Long,
        elementoId: Long,
        tipoHint: TipoHint
    ): String {
        val key = "hint_${partidaId}_${elementoId}_${tipoHint.name}"

        return cacheHints.getOrPut(key) {
            val elemento = elementoCulturalService.obtenerPorId(elementoId)

            modeloService.generarHintContextual(
                imagenBytes = obtenerImagenBytes(elemento.imagenUrl),
                nombreArchivo = "${elemento.nombreEspanol}.jpg",
                tipoHint = tipoHint,
                categoria = elemento.categoria.name
            ) ?: "Busca un elemento cultural importante"
        }
    }

    /**
     * Limpia caché de una partida específica
     */
    fun limpiarCachePartida(partidaId: Long) {
        cacheNarrativas.keys.removeIf { it.startsWith("narrativa_${partidaId}_") }
        cacheDialogos.keys.removeIf { it.startsWith("dialogo_${partidaId}_") }
        cacheHints.keys.removeIf { it.startsWith("hint_${partidaId}_") }
    }

    /**
     * Descarga imagen desde URL y la convierte a ByteArray
     */
    private fun obtenerImagenBytes(imagenUrl: String): ByteArray {
        return try {
            println("🖼️ Descargando imagen desde: $imagenUrl")

            // Opción 1: Usando RestTemplate
            val response = restTemplate.getForEntity(imagenUrl, ByteArray::class.java)

            if (response.statusCode.is2xxSuccessful && response.body != null) {
                println("✅ Imagen descargada: ${response.body!!.size} bytes")
                response.body!!
            } else {
                println("⚠️ Error descargando imagen, usando fallback")
                byteArrayOf()
            }

        } catch (e: Exception) {
            println("❌ Error al obtener imagen: ${e.message}")

            // Fallback: Intentar con URL directamente
            try {
                URL(imagenUrl).openStream().readBytes()
            } catch (e2: Exception) {
                println("❌ Fallback también falló: ${e2.message}")
                byteArrayOf()
            }
        }
    }
}