package com.sma.gateway.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Post-filtro global: tras responder el servicio destino, devuelve el
 * correlationId en la cabecera de la respuesta para trazabilidad de extremo a extremo.
 */
@Component
public class ResponseFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(ResponseFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // .then() ejecuta esto DESPUÉS de que el servicio destino responda
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            String correlationId = exchange.getRequest().getHeaders().getFirst(FilterUtils.CORRELATION_ID);
            logger.debug("Devolviendo correlationId en la respuesta: {}", correlationId);
            exchange.getResponse().getHeaders().add(FilterUtils.CORRELATION_ID, correlationId);
        }));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
