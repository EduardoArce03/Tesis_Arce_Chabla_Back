package com.tesis.gamificacion.exception

import com.tesis.gamificacion.dto.ErrorDTO
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(UPSGamificacionCustomException::class)
    fun  handleCustomException(
        ex: UPSGamificacionCustomException
    ): ResponseEntity<ErrorDTO> {
        val errorDTO = ErrorDTO(
            title = ex.title,
            message = ex.message,
            severity = ex.severity,
        )
        return ResponseEntity.badRequest().body(errorDTO)
    }

}