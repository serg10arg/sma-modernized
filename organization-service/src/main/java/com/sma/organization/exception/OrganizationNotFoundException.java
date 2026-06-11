package com.sma.organization.exception;

/**
 * Excepción lanzada cuando no se encuentra una organización solicitada.
 * Se traduce a una respuesta HTTP 404.
 */
public class OrganizationNotFoundException extends RuntimeException {

    public OrganizationNotFoundException(String message) {
        super(message);
    }
}
