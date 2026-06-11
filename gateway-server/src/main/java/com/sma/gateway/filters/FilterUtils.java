package com.sma.gateway.filters;

/**
 * Constantes compartidas por los filtros del gateway.
 */
public final class FilterUtils {

    // Header de correlación. DEBE coincidir con UserContext.CORRELATION_ID del
    // licensing-service para que la cadena de trazas encaje entre servicios (Etapa 4).
    public static final String CORRELATION_ID = "tmx-correlation-id";

    private FilterUtils() {
        // Clase de constantes: no instanciable
    }
}
