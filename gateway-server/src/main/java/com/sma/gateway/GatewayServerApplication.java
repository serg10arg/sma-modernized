package com.sma.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del API Gateway.
 * Spring Boot detecta automáticamente el cliente de Eureka por el classpath,
 * por lo que no es necesaria la anotación @EnableEurekaClient.
 */
@SpringBootApplication
public class GatewayServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayServerApplication.class, args);
    }
}
