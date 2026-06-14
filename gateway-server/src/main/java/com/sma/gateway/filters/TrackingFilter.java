package com.sma.gateway.filters;

import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Pre-filtro global: garantiza que toda petición entrante lleve un correlationId.
 * Si no viene en el header, lo deriva del traceId de la traza en curso (Micrometer)
 * y lo inyecta en la petición para que los servicios destino (y el ResponseFilter)
 * lo encuentren. Así el tmx-correlation-id de negocio coincide con el traceId de Zipkin.
 */
@Component
public class TrackingFilter implements GlobalFilter, Ordered {

    private static final int FILTER_ORDER = 1;
    private static final Logger logger = LoggerFactory.getLogger(TrackingFilter.class);

    private final Tracer tracer;

    public TrackingFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Mono.defer ejecuta el cuerpo en tiempo de suscripción, dentro de un operador,
        // donde el hook de propagación automática ya ha restaurado el ThreadLocal de la traza.
        return Mono.defer(() -> {
            HttpHeaders headers = exchange.getRequest().getHeaders();
            ServerWebExchange mutated = exchange;

            if (headers.getFirst(FilterUtils.CORRELATION_ID) == null) {
                // No venía correlationId: lo derivamos del traceId y lo inyectamos en la petición
                String correlationId = resolveCorrelationId();
                ServerHttpRequest request = exchange.getRequest().mutate()
                        .header(FilterUtils.CORRELATION_ID, correlationId).build();
                mutated = exchange.mutate().request(request).build();
                logger.debug("tmx-correlation-id (= traceId) generado en el gateway: {}", correlationId);
            } else {
                // Ya existía: forma parte de una cadena de llamadas; no lo tocamos
                logger.debug("correlationId presente: {}", headers.getFirst(FilterUtils.CORRELATION_ID));
            }
            return chain.filter(mutated);
        });
    }

    /**
     * Id de correlación = traceId de la traza en curso. Si por timing reactivo no
     * hubiera span activo, se genera un UUID de respaldo (no debería ocurrir).
     */
    private String resolveCorrelationId() {
        var span = tracer.currentSpan();
        return (span != null) ? span.context().traceId() : UUID.randomUUID().toString();
    }

    @Override
    public int getOrder() {
        return FILTER_ORDER;
    }
}
