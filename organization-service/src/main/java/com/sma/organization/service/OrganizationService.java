package com.sma.organization.service;

import com.sma.organization.exception.OrganizationNotFoundException;
import com.sma.organization.model.Organization;
import com.sma.organization.repository.OrganizationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Servicio de negocio para la gestión de organizaciones.
 * Persiste los datos en PostgreSQL a través de OrganizationRepository.
 */
@Service
public class OrganizationService {

    private final OrganizationRepository repository;

    public OrganizationService(OrganizationRepository repository) {
        this.repository = repository;
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
     * Crea y persiste una nueva organización con un id aleatorio.
     */
    public Organization createOrganization(Organization organization) {
        organization.setOrganizationId(UUID.randomUUID().toString());
        return repository.save(organization);
    }

    /**
     * Actualiza una organización existente.
     */
    public Organization updateOrganization(Organization organization) {
        return repository.save(organization);
    }

    /**
     * Elimina una organización. Lanza HTTP 404 si no existe previamente.
     */
    public void deleteOrganization(String organizationId) {
        Organization organization = getOrganization(organizationId);
        repository.delete(organization);
    }
}
