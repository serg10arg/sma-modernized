package com.sma.licensing.utils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filtro que se ejecuta una vez por petición entrante.
 * Lee el correlationId del header; si no viene, genera uno nuevo.
 * Lo guarda en UserContext para que esté disponible durante el procesamiento
 * y limpia el contexto al terminar.
 */
@Component
public class UserContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String correlationId = request.getHeader(UserContext.CORRELATION_ID);
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = UUID.randomUUID().toString();
            }
            UserContext.setCorrelationId(correlationId);

            // Devuelve el correlationId también en la respuesta, para trazabilidad
            response.addHeader(UserContext.CORRELATION_ID, correlationId);

            filterChain.doFilter(request, response);
        } finally {
            // Evita fugas entre peticiones que reutilizan el mismo hilo
            UserContext.clear();
        }
    }
}
