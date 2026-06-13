package com.sma.licensing.event;

import com.sma.licensing.repository.OrganizationRedisRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

/**
 * Consumidor de eventos de cambio de organización publicados por el
 * organization-service. Mantiene la coherencia de la caché Redis:
 * ante un UPDATED o DELETED invalida (borra) la entrada cacheada, de modo
 * que la siguiente lectura sea un MISS y recachee el dato fresco.
 * En CREATED no hay nada que invalidar (no existe entrada previa).
 */
@Configuration
public class OrganizationChangeHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(OrganizationChangeHandler.class);

    private final OrganizationRedisRepository redisRepository;

    public OrganizationChangeHandler(OrganizationRedisRepository redisRepository) {
        this.redisRepository = redisRepository;
    }

    @Bean
    public Consumer<OrganizationChangeModel> organizationChange() {
        return change -> {
            logger.debug("Evento de organización recibido: action={}, organizationId={}, correlationId={}",
                    change.action(), change.organizationId(), change.correlationId());

            switch (change.action()) {
                case UPDATED, DELETED -> invalidateCache(change.organizationId());
                case CREATED -> logger.debug(
                        "Acción CREATED para {}: no hay entrada que invalidar", change.organizationId());
            }
        };
    }

    /** Borra la entrada de Redis tolerando fallos: un Redis caído no debe romper el consumidor. */
    private void invalidateCache(String organizationId) {
        try {
            redisRepository.deleteById(organizationId);
            logger.debug("Caché Redis invalidada para la organización {}", organizationId);
        } catch (Exception ex) {
            logger.error("No se pudo invalidar la caché Redis de la organización {}", organizationId, ex);
        }
    }
}
