package com.sma.licensing.config;

import com.sma.licensing.utils.UserContextInterceptor;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Configuración del cliente HTTP para la comunicación entre servicios.
 * El RestClient.Builder anotado con @LoadBalanced permite invocar servicios
 * por su nombre lógico (ej. http://organization-service), resolviéndolo vía
 * Eureka y Spring Cloud LoadBalancer.
 */
@Configuration
public class RestClientConfig {

    /**
     * Builder balanceado: resuelve nombres lógicos de servicio a instancias reales.
     */
    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    /**
     * RestClient construido a partir del builder balanceado, con el interceptor
     * que propaga el correlationId en cada llamada saliente.
     */
    @Bean
    public RestClient organizationServiceRestClient(RestClient.Builder loadBalancedRestClientBuilder) {
        return loadBalancedRestClientBuilder
                .requestInterceptor(new UserContextInterceptor())
                .build();
    }
}
