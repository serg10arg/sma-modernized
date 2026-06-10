package com.sma.licensing.repository;

import com.sma.licensing.model.License;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de acceso a datos para la entidad License.
 * Spring Data JPA genera la implementación en tiempo de ejecución.
 */
@Repository
public interface LicenseRepository extends JpaRepository<License, String> {

    // Devuelve todas las licencias de una organización
    List<License> findByOrganizationId(String organizationId);

    // Busca una licencia concreta dentro de una organización
    Optional<License> findByOrganizationIdAndLicenseId(String organizationId, String licenseId);
}
