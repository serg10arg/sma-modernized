package com.sma.licensing.event;

/**
 * Evento de cambio de organización consumido desde Kafka.
 * Replica la estructura publicada por organization-service.
 *
 * @param action         tipo de cambio (CREATED, UPDATED, DELETED)
 * @param organizationId id de la organización afectada
 * @param correlationId  id de correlación (vacío hasta la etapa de trazabilidad)
 */
public record OrganizationChangeModel(
        ActionEnum action,
        String organizationId,
        String correlationId
) {
}
