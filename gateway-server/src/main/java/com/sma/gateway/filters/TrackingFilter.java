package com.sma.gateway.filters;

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
 * Si no viene en el header, lo genera e inyecta en la petición para que los
 * servicios destino (y el ResponseFilter) lo encuentren.
 */
@Component
public class TrackingFilter implements GlobalFilter, Ordered {

    private static final int FILTER_ORDER = 1;
    private static final Logger logger = LoggerFactory.getLogger(TrackingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        ServerWebExchange mutated = exchange;

        if (headers.getFirst(FilterUtils.CORRELATION_ID) == null) {
            // No venía correlationId: lo generamos y lo inyectamos en la petición
            String correlationId = UUID.randomUUID().toString();
            ServerHttpRequest request = exchange.getRequest().mutate()
                    .header(FilterUtils.CORRELATION_ID, correlationId).build();
            mutated = exchange.mutate().request(request).build();
            logger.debug("correlationId generado en el gateway: {}", correlationId);
        } else {
            // Ya existía: forma parte de una cadena de llamadas; no lo tocamos
            logger.debug("correlationId presente: {}", headers.getFirst(FilterUtils.CORRELATION_ID));
        }
        return chain.filter(mutated);
    }

    @Override
    public int getOrder() {
        return FILTER_ORDER;
    }
}
