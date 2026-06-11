package com.sma.organization.repository;

import com.sma.organization.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio de acceso a datos para la entidad Organization.
 */
@Repository
public interface OrganizationRepository extends JpaRepository<Organization, String> {

    // Busca una organización por su identificador
    Optional<Organization> findByOrganizationId(String organizationId);
}
