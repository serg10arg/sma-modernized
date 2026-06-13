package com.sma.licensing.service;

import com.sma.licensing.client.OrganizationRestClient;
import com.sma.licensing.model.Organization;
import com.sma.licensing.repository.OrganizationRedisRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Wrapper que implementa el patrón cache-aside a mano sobre Redis:
 *  - En HIT devuelve la organización cacheada.
 *  - En MISS llama al organization-service (con su propio circuit breaker
 *    y retry) y cachea el resultado.
 * El acceso a Redis va envuelto en try/catch para que un Redis caído NO
 * tumbe la petición: en ese caso se trata como MISS y se va a la fuente.
 * El objeto de fallback NUNCA se cachea.
 */
@Service
public class OrganizationCacheService {

    private static final Logger logger =
            LoggerFactory.getLogger(OrganizationCacheService.class);

    private final OrganizationRedisRepository redisRepository;
    private final OrganizationRestClient organizationRestClient;

    public OrganizationCacheService(OrganizationRedisRepository redisRepository,
                                    OrganizationRestClient organizationRestClient) {
        this.redisRepository = redisRepository;
        this.organizationRestClient = organizationRestClient;
    }

    /**
     * Devuelve la organización aplicando cache-aside.
     */
    public Organization getOrganization(String organizationId) {
        Organization cached = checkRedisCache(organizationId);
        if (cached != null) {
            logger.debug("Organización {} recuperada desde la caché Redis (HIT)", organizationId);
            return cached;
        }

        logger.debug("Organización {} no está en la caché Redis (MISS); se consulta el servicio remoto", organizationId);
        // El rest client conserva su @CircuitBreaker/@Retry (es otro bean: el proxy AOP aplica)
        Organization organization = organizationRestClient.getOrganization(organizationId);

        // No cachear el fallback ni respuestas nulas
        if (organization != null
                && !OrganizationRestClient.FALLBACK_ORG_NAME.equals(organization.getName())) {
            cacheOrganization(organization);
        }
        return organization;
    }

    /** Lee de Redis tolerando fallos: si Redis no responde, se trata como MISS. */
    private Organization checkRedisCache(String organizationId) {
        try {
            return redisRepository.findById(organizationId).orElse(null);
        } catch (Exception ex) {
            logger.error("Error leyendo la organización {} de Redis; se continúa sin caché", organizationId, ex);
            return null;
        }
    }

    /** Guarda en Redis tolerando fallos: un error de caché no afecta a la petición. */
    private void cacheOrganization(Organization organization) {
        try {
            redisRepository.save(organization);
        } catch (Exception ex) {
            logger.error("No se pudo cachear la organización {} en Redis", organization.getOrganizationId(), ex);
        }
    }
}
