package com.sma.licensing.client;

import com.sma.licensing.model.Organization;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Cliente que consulta el organization-service descubriéndolo por su nombre
 * lógico en Eureka. La URL "http://organization-service" no es una dirección
 * física: Spring Cloud LoadBalancer la resuelve a una instancia real.
 */
@Component
public class OrganizationRestClient {

    /** Nombre sentinel del fallback. Lo usa la capa de caché para NO cachearlo. */
    public static final String FALLBACK_ORG_NAME = "Organización no disponible temporalmente";

    private final RestClient organizationRestClient;

    public OrganizationRestClient(RestClient organizationRestClient) {
        this.organizationRestClient = organizationRestClient;
    }

    /**
     * Recupera una organización por su id desde el organization-service.
     * Protegida con circuit breaker y retry: si el servicio remoto falla de forma
     * persistente, se devuelve la organización de fallback.
     */
    @CircuitBreaker(name = "organizationService", fallbackMethod = "buildFallbackOrganization")
    @Retry(name = "retryLicenseService", fallbackMethod = "buildFallbackOrganization")
    public Organization getOrganization(String organizationId) {
        return organizationRestClient.get()
                .uri("http://organization-service/v1/organization/{id}", organizationId)
                .retrieve()
                .body(Organization.class);
    }

    /**
     * Fallback de getOrganization: devuelve una organización mínima indicando
     * que el servicio no está disponible temporalmente.
     */
    private Organization buildFallbackOrganization(String organizationId, Throwable t) {
        Organization org = new Organization();
        org.setOrganizationId(organizationId);
        org.setName(FALLBACK_ORG_NAME);
        return org;
    }
}
