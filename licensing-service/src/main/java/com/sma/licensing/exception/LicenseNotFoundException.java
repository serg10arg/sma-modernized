package com.sma.licensing.exception;

/**
 * Excepción lanzada cuando no se encuentra una licencia solicitada.
 * Se traduce a una respuesta HTTP 404 mediante GlobalExceptionHandler.
 */
public class LicenseNotFoundException extends RuntimeException {

    public LicenseNotFoundException(String message) {
        super(message);
    }
}
