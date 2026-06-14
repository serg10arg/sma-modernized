package com.sma.organization.service;

import com.sma.organization.event.ActionEnum;
import com.sma.organization.event.OrganizationChangeModel;
import com.sma.organization.exception.OrganizationNotFoundException;
import com.sma.organization.model.Organization;
import com.sma.organization.repository.OrganizationRepository;
import io.micrometer.tracing.Tracer;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Servicio de negocio para la gestión de organizaciones.
 * Persiste los datos en PostgreSQL y publica un evento de cambio en Kafka
 * tras cada operación de creación, actualización o borrado.
 */
@Service
public class OrganizationService {

    // Nombre del binding de salida; debe coincidir con la config de Spring Cloud Stream
    private static final String OUTPUT_BINDING = "organizationChange-out-0";

    private final OrganizationRepository repository;
    private final StreamBridge streamBridge;
    private final Tracer tracer;

    public OrganizationService(OrganizationRepository repository, StreamBridge streamBridge, Tracer tracer) {
        this.repository = repository;
        this.streamBridge = streamBridge;
        this.tracer = tracer;
    }

    /**
     * Recupera una organización por su id. Lanza HTTP 404 si no existe.
     */
    public Organization getOrganization(String organizationId) {
        return repository.findByOrganizationId(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException(
                        "No se encontró la organización con id " + organizationId));
    }

    /**
     * Devuelve todas las organizaciones.
     */
    public List<Organization> getAllOrganizations() {
        return repository.findAll();
    }

    /**
     * Crea y persiste una nueva organización con un id aleatorio, y publica un evento CREATED.
     */
    public Organization createOrganization(Organization organization) {
        organization.setOrganizationId(UUID.randomUUID().toString());
        Organization saved = repository.save(organization);
        publishChange(ActionEnum.CREATED, saved.getOrganizationId());
        return saved;
    }

    /**
     * Actualiza una organización existente y publica un evento UPDATED.
     */
    public Organization updateOrganization(Organization organization) {
        Organization saved = repository.save(organization);
        publishChange(ActionEnum.UPDATED, saved.getOrganizationId());
        return saved;
    }

    /**
     * Elimina una organización (404 si no existe) y publica un evento DELETED.
     */
    public void deleteOrganization(String organizationId) {
        Organization organization = getOrganization(organizationId);
        repository.delete(organization);
        publishChange(ActionEnum.DELETED, organizationId);
    }

    /**
     * Publica el evento de cambio en Kafka mediante StreamBridge.
     * El correlationId se rellena con el traceId de la traza en curso.
     */
    private void publishChange(ActionEnum action, String organizationId) {
        OrganizationChangeModel event = new OrganizationChangeModel(action, organizationId, currentTraceId());
        streamBridge.send(OUTPUT_BINDING, event);
    }

    /**
     * Devuelve el traceId de la traza en curso, que usamos como identificador de
     * correlación del evento. Si no hay span activo, devuelve null.
     */
    private String currentTraceId() {
        var span = tracer.currentSpan();
        return (span != null) ? span.context().traceId() : null;
    }
}
