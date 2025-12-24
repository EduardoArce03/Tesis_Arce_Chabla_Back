package com.tesis.gamificacion.service

import com.tesis.gamificacion.dto.request.CrearElementoCulturalRequest
import com.tesis.gamificacion.dto.response.ElementoCulturalResponse
import com.tesis.gamificacion.model.entities.ElementoCultural
import com.tesis.gamificacion.model.enums.CategoriasCultural
import com.tesis.gamificacion.repository.ElementoCulturalRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ElementoCulturalService(
    private val elementoCulturalRepository: ElementoCulturalRepository
) {

    @Transactional(readOnly = true)
    fun obtenerTodos(): List<ElementoCulturalResponse> {
        return elementoCulturalRepository.findByActivoTrue()
            .map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun obtenerPorCategoria(categoria: CategoriasCultural): List<ElementoCulturalResponse> {
        return elementoCulturalRepository.findByCategoriaAndActivoTrue(categoria)
            .map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun obtenerPorId(id: Long): ElementoCulturalResponse {
        val elemento = elementoCulturalRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Elemento cultural no encontrado con ID: $id") }
        return elemento.toResponse()
    }

    @Transactional(readOnly = true)
    fun obtenerAleatoriosPorCategoria(categoria: CategoriasCultural, cantidad: Int): List<ElementoCulturalResponse> {
        val elementos = elementoCulturalRepository.findRandomByCategoria(categoria)

        if (elementos.size < cantidad) {
            throw IllegalStateException(
                "No hay suficientes elementos en la categoría $categoria. Requeridos: $cantidad, Disponibles: ${elementos.size}"
            )
        }

        return elementos.take(cantidad).map { it.toResponse() }
    }

    @Transactional
    fun crear(request: CrearElementoCulturalRequest): ElementoCulturalResponse {
        val elemento = ElementoCultural(
            nombreKichwa = request.nombreKichwa,
            nombreEspanol = request.nombreEspanol,
            imagenUrl = request.imagenUrl,
            categoria = request.categoria,
            descripcion = request.descripcion
        )

        val guardado = elementoCulturalRepository.save(elemento)
        return guardado.toResponse()
    }

    @Transactional
    fun actualizar(id: Long, request: CrearElementoCulturalRequest): ElementoCulturalResponse {
        val elemento = elementoCulturalRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Elemento cultural no encontrado con ID: $id") }

        val actualizado = elemento.copy(
            nombreKichwa = request.nombreKichwa,
            nombreEspanol = request.nombreEspanol,
            imagenUrl = request.imagenUrl,
            categoria = request.categoria,
            descripcion = request.descripcion
        )

        val guardado = elementoCulturalRepository.save(actualizado)
        return guardado.toResponse()
    }

    @Transactional
    fun eliminar(id: Long) {
        val elemento = elementoCulturalRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Elemento cultural no encontrado con ID: $id") }

        // Eliminación lógica
        val desactivado = elemento.copy(activo = false)
        elementoCulturalRepository.save(desactivado)
    }

    private fun ElementoCultural.toResponse() = ElementoCulturalResponse(
        id = id!!,
        nombreKichwa = nombreKichwa,
        nombreEspanol = nombreEspanol,
        imagenUrl = imagenUrl,
        categoria = categoria,
        descripcion = descripcion
    )
}