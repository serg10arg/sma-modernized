package com.sma.licensing.config;

import com.sma.licensing.utils.UserContextInterceptor;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
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
     * Gestor de clientes autorizados para el flujo client_credentials.
     * Variante "service": no depende del contexto de la petición HTTP entrante
     * y cachea el token, refrescándolo solo al expirar.
     */
    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {

        OAuth2AuthorizedClientProvider provider = OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build();

        AuthorizedClientServiceOAuth2AuthorizedClientManager manager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, authorizedClientService);
        manager.setAuthorizedClientProvider(provider);
        return manager;
    }

    /**
     * RestClient construido a partir del builder balanceado. Dos interceptores:
     * uno propaga el correlationId, otro inyecta el token de servicio (client_credentials).
     */
    @Bean
    public RestClient organizationServiceRestClient(
            RestClient.Builder loadBalancedRestClientBuilder,
            OAuth2AuthorizedClientManager authorizedClientManager) {
        return loadBalancedRestClientBuilder
                .requestInterceptor(new UserContextInterceptor())
                .requestInterceptor(new OAuth2ClientInterceptor(authorizedClientManager, "sma-internal"))
                .build();
    }
}
