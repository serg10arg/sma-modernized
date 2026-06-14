package com.sma.organization.event;

/**
 * Evento publicado cuando una organización cambia de estado.
 * Viaja por Kafka hacia los servicios interesados (ej. licensing-service).
 *
 * @param action         tipo de cambio (CREATED, UPDATED, DELETED)
 * @param organizationId id de la organización afectada
 * @param correlationId  id de correlación; se rellena con el traceId de la traza en curso
 */
public record OrganizationChangeModel(
        ActionEnum action,
        String organizationId,
        String correlationId
) {
}
