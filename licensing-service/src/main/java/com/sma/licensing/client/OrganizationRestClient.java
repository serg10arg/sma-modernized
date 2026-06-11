package com.sma.licensing.client;

import com.sma.licensing.model.Organization;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Cliente que consulta el organization-service descubriéndolo por su nombre
 * lógico en Eureka. La URL "http://organization-service" no es una dirección
 * física: Spring Cloud LoadBalancer la resuelve a una instancia real.
 */
@Component
public class OrganizationRestClient {

    private final RestClient organizationRestClient;

    public OrganizationRestClient(RestClient organizationRestClient) {
        this.organizationRestClient = organizationRestClient;
    }

    /**
     * Recupera una organización por su id desde el organization-service.
     * Si el servicio remoto falla, la excepción se propaga (sin resiliencia en esta etapa).
     */
    public Organization getOrganization(String organizationId) {
        return organizationRestClient.get()
                .uri("http://organization-service/v1/organization/{id}", organizationId)
                .retrieve()
                .body(Organization.class);
    }
}
