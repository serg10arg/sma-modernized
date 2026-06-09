package com.sma.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Punto de entrada del servidor de descubrimiento de servicios.
 * Activa Netflix Eureka Server mediante @EnableEurekaServer.
 * Los demás servicios del sistema se registran aquí al arrancar.
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
