package com.sma.licensing.config;

import java.io.IOException;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;

/**
 * Interceptor que añade el token de máquina (client_credentials) en la cabecera
 * Authorization de cada llamada saliente al organization-service.
 * El token lo gestiona y cachea el OAuth2AuthorizedClientManager; aquí solo se
 * recupera y se inyecta. Al ejecutarse en el momento de la petición, corre dentro
 * del flujo que protege Resilience4j en OrganizationRestClient.
 */
public class OAuth2ClientInterceptor implements ClientHttpRequestInterceptor {

    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final String clientRegistrationId;

    public OAuth2ClientInterceptor(OAuth2AuthorizedClientManager authorizedClientManager,
                                   String clientRegistrationId) {
        this.authorizedClientManager = authorizedClientManager;
        this.clientRegistrationId = clientRegistrationId;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId(clientRegistrationId)
                .principal("licensing-service") // nombre de principal para el flujo de máquina
                .build();

        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);
        if (authorizedClient != null) {
            request.getHeaders().setBearerAuth(authorizedClient.getAccessToken().getTokenValue());
        }
        return execution.execute(request, body);
    }
}
