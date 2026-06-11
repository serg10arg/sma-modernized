package com.sma.organization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del microservicio de organizaciones.
 * Spring Boot detecta automáticamente el cliente de Eureka por el classpath,
 * por lo que no es necesaria la anotación @EnableEurekaClient.
 */
@SpringBootApplication
public class OrganizationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrganizationServiceApplication.class, args);
    }
}
