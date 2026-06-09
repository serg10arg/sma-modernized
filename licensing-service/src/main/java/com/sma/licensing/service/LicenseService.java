package com.sma.licensing.service;

import com.sma.licensing.model.License;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.UUID;

/**
 * Servicio de negocio para la gestión de licencias.
 * Utiliza MessageSource para devolver mensajes localizados según el idioma
 * indicado por el cliente en el header Accept-Language.
 * En esta etapa los datos se sirven en memoria; en etapas futuras
 * se reemplazará por acceso a base de datos PostgreSQL.
 */
@Service
public class LicenseService {

    // Fuente de mensajes internacionalizados inyectada por Spring
    private final MessageSource messages;

    public LicenseService(MessageSource messages) {
        this.messages = messages;
    }

    /**
     * Recupera una licencia por su id y el id de organización.
     *
     * @param licenseId      Identificador de la licencia
     * @param organizationId Identificador de la organización propietaria
     * @param locale         Idioma solicitado por el cliente (header Accept-Language)
     * @return Licencia encontrada
     */
    public License getLicense(String licenseId, String organizationId, Locale locale) {
        License license = new License();
        license.setLicenseId(licenseId);
        license.setOrganizationId(organizationId);
        license.setDescription("Software product");
        license.setProductName("O-stock");
        license.setLicenseType("full");
        license.setComment(messages.getMessage("license.search.error.message",
                new Object[]{licenseId, organizationId}, locale));
        return license;
    }

    /**
     * Crea una nueva licencia asignándole un id aleatorio.
     *
     * @param organizationId Identificador de la organización propietaria
     * @param license        Datos de la licencia a crear
     * @param locale         Idioma solicitado por el cliente
     * @return Mensaje de confirmación localizado
     */
    public String createLicense(String organizationId, License license, Locale locale) {
        license.setLicenseId(UUID.randomUUID().toString());
        license.setOrganizationId(organizationId);
        return String.format(
                messages.getMessage("license.create.message", null, locale),
                license);
    }

    /**
     * Actualiza una licencia existente.
     *
     * @param organizationId Identificador de la organización propietaria
     * @param license        Datos actualizados de la licencia
     * @param locale         Idioma solicitado por el cliente
     * @return Mensaje de confirmación localizado
     */
    public String updateLicense(String organizationId, License license, Locale locale) {
        license.setOrganizationId(organizationId);
        return String.format(
                messages.getMessage("license.update.message", null, locale),
                license);
    }

    /**
     * Elimina una licencia por su id.
     *
     * @param licenseId      Identificador de la licencia a eliminar
     * @param organizationId Identificador de la organización propietaria
     * @param locale         Idioma solicitado por el cliente
     * @return Mensaje de confirmación localizado
     */
    public String deleteLicense(String licenseId, String organizationId, Locale locale) {
        return String.format(
                messages.getMessage("license.delete.message", null, locale),
                licenseId);
    }
}
