package com.sma.licensing.utils;

/**
 * Almacena información de contexto de la petición (correlationId) usando ThreadLocal,
 * de modo que esté disponible durante todo el procesamiento del hilo actual.
 */
public class UserContext {

    // Nombre del header HTTP que transporta el identificador de correlación
    public static final String CORRELATION_ID = "tmx-correlation-id";

    // Contexto ligado al hilo actual
    private static final ThreadLocal<String> correlationId = new ThreadLocal<>();

    private UserContext() {
        // Clase de utilidad: no instanciable
    }

    public static String getCorrelationId() {
        return correlationId.get();
    }

    public static void setCorrelationId(String value) {
        correlationId.set(value);
    }

    /**
     * Limpia el contexto del hilo. Debe llamarse al finalizar la petición
     * para evitar fugas de datos entre peticiones que reutilizan el mismo hilo.
     */
    public static void clear() {
        correlationId.remove();
    }
}
