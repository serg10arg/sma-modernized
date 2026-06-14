package com.sma.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Hooks;

/**
 * Punto de entrada del API Gateway.
 * Spring Boot detecta automáticamente el cliente de Eureka por el classpath,
 * por lo que no es necesaria la anotación @EnableEurekaClient.
 */
@SpringBootApplication
public class GatewayServerApplication {

    public static void main(String[] args) {
        // Restaura los ThreadLocal (traza, etc.) desde el Reactor Context en código reactivo.
        // Fix canónico de Micrometer para que tracer.currentSpan() funcione dentro de operadores en WebFlux.
        Hooks.enableAutomaticContextPropagation();
        SpringApplication.run(GatewayServerApplication.class, args);
    }
}
