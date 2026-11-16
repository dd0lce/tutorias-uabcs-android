package mx.edu.uabcs.tutorias_backend.controller

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

/** Las reglas de negocio fallan con `require`; la app muestra `mensaje` tal cual. */
@RestControllerAdvice
class Errores {
    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun conflicto(e: IllegalArgumentException) = mapOf("mensaje" to e.message)
}
