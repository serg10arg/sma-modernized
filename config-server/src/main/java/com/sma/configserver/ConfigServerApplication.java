package com.sma.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Punto de entrada del servidor de configuración centralizada.
 * Activa Spring Cloud Config Server mediante @EnableConfigServer.
 * Los demás servicios del sistema obtienen su configuración desde aquí.
 */
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
