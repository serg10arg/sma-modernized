package com.sma.licensing.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Manejador global de excepciones del servicio.
 * Traduce las excepciones de dominio a respuestas HTTP apropiadas.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Traduce LicenseNotFoundException a una respuesta HTTP 404 con el mensaje localizado.
     */
    @ExceptionHandler(LicenseNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleLicenseNotFound(LicenseNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }
}
