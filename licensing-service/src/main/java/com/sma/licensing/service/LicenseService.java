package com.sma.licensing.service;

import com.sma.licensing.client.OrganizationRestClient;
import com.sma.licensing.exception.LicenseNotFoundException;
import com.sma.licensing.model.License;
import com.sma.licensing.model.Organization;
import com.sma.licensing.repository.LicenseRepository;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Servicio de negocio para la gestión de licencias.
 * Persiste los datos en PostgreSQL a través de LicenseRepository.
 * Los mensajes se resuelven con MessageSource usando placeholders nativos ({0}, {1}),
 * según el idioma indicado por el cliente en el header Accept-Language.
 */
@Service
public class LicenseService {

    // Fuente de mensajes internacionalizados
    private final MessageSource messages;

    // Repositorio de acceso a datos de licencias
    private final LicenseRepository licenseRepository;

    // Cliente del organization-service (descubierto por nombre en Eureka)
    private final OrganizationRestClient organizationRestClient;

    public LicenseService(MessageSource messages,
                          LicenseRepository licenseRepository,
                          OrganizationRestClient organizationRestClient) {
        this.messages = messages;
        this.licenseRepository = licenseRepository;
        this.organizationRestClient = organizationRestClient;
    }

    /**
     * Recupera una licencia y la enriquece con los datos de su organización,
     * consultando el organization-service vía Eureka.
     * Lanza LicenseNotFoundException (HTTP 404) si la licencia no existe.
     */
    public License getLicense(String licenseId, String organizationId, Locale locale) {
        License license = licenseRepository
                .findByOrganizationIdAndLicenseId(organizationId, licenseId)
                .orElseThrow(() -> new LicenseNotFoundException(
                        messages.getMessage(
                                "license.search.error.message",
                                new Object[]{licenseId, organizationId},
                                locale)));

        // Enriquecimiento: consulta el organization-service por descubrimiento.
        // Si el servicio remoto falla, la excepción se propaga (Etapa 5 añadirá resiliencia).
        Organization organization = organizationRestClient.getOrganization(organizationId);
        license.setOrganization(organization);

        return license;
    }

    /**
     * Devuelve todas las licencias de una organización.
     */
    public List<License> getLicensesByOrganization(String organizationId) {
        return licenseRepository.findByOrganizationId(organizationId);
    }

    /**
     * Crea y persiste una nueva licencia con un id aleatorio.
     */
    public String createLicense(String organizationId, License license, Locale locale) {
        license.setLicenseId(UUID.randomUUID().toString());
        license.setOrganizationId(organizationId);
        licenseRepository.save(license);
        return messages.getMessage(
                "license.create.message",
                new Object[]{license},
                locale);
    }

    /**
     * Actualiza y persiste una licencia existente.
     */
    public String updateLicense(String organizationId, License license, Locale locale) {
        license.setOrganizationId(organizationId);
        licenseRepository.save(license);
        return messages.getMessage(
                "license.update.message",
                new Object[]{license},
                locale);
    }

    /**
     * Elimina una licencia. Lanza HTTP 404 si no existe previamente.
     */
    public String deleteLicense(String licenseId, String organizationId, Locale locale) {
        License license = getLicense(licenseId, organizationId, locale);
        licenseRepository.delete(license);
        return messages.getMessage(
                "license.delete.message",
                new Object[]{licenseId},
                locale);
    }
}
