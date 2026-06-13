package com.sma.licensing.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

/**
 * Consumidor de los eventos de cambio de organización.
 * Por ahora solo registra el evento; en la etapa de caché reaccionará
 * invalidando la entrada correspondiente en Redis.
 */
@Configuration
public class OrganizationChangeHandler {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationChangeHandler.class);

    /**
     * Bean funcional ligado al binding organizationChange-in-0 (topic sma.organization.changes).
     * El nombre del bean debe coincidir con spring.cloud.function.definition.
     */
    @Bean
    public Consumer<OrganizationChangeModel> organizationChange() {
        return event -> logger.info(
                "Evento recibido de organization-service: action={}, organizationId={}, correlationId={}",
                event.action(), event.organizationId(), event.correlationId());
    }
}
