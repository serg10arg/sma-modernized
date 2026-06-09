package com.sma.licensing.controller;

import com.sma.licensing.model.License;
import com.sma.licensing.service.LicenseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Controlador REST del servicio de licencias.
 * Expone endpoints CRUD bajo /v1/organization/{organizationId}/license.
 * Incluye soporte para internacionalización (header Accept-Language)
 * y links HATEOAS en las respuestas.
 */
@RestController
@RequestMapping("/v1/organization/{organizationId}/license")
public class LicenseController {

    private final LicenseService licenseService;

    public LicenseController(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    /**
     * Obtiene una licencia específica de una organización.
     * Añade links HATEOAS para todas las operaciones relacionadas.
     *
     * @param organizationId Identificador de la organización
     * @param licenseId      Identificador de la licencia
     * @param locale         Idioma del cliente (resuelto desde Accept-Language)
     * @return Licencia con links hipermedia y HTTP 200
     */
    @GetMapping("/{licenseId}")
    public ResponseEntity<License> getLicense(
            @PathVariable String organizationId,
            @PathVariable String licenseId,
            Locale locale) {

        License license = licenseService.getLicense(licenseId, organizationId, locale);

        // Añade links HATEOAS apuntando a cada operación del controller
        license.add(
                linkTo(methodOn(LicenseController.class)
                        .getLicense(organizationId, licenseId, locale))
                        .withSelfRel(),
                linkTo(methodOn(LicenseController.class)
                        .createLicense(organizationId, null, locale))
                        .withRel("createLicense"),
                linkTo(methodOn(LicenseController.class)
                        .updateLicense(organizationId, null, locale))
                        .withRel("updateLicense"),
                linkTo(methodOn(LicenseController.class)
                        .deleteLicense(organizationId, licenseId, locale))
                        .withRel("deleteLicense")
        );

        return ResponseEntity.ok(license);
    }

    /**
     * Crea una nueva licencia para una organización.
     *
     * @param organizationId Identificador de la organización
     * @param license        Datos de la licencia en el body del request
     * @param locale         Idioma del cliente
     * @return Mensaje de confirmación localizado y HTTP 200
     */
    @PostMapping
    public ResponseEntity<String> createLicense(
            @PathVariable String organizationId,
            @RequestBody License license,
            Locale locale) {

        return ResponseEntity.ok(
                licenseService.createLicense(organizationId, license, locale));
    }

    /**
     * Actualiza una licencia existente de una organización.
     *
     * @param organizationId Identificador de la organización
     * @param license        Datos actualizados en el body del request
     * @param locale         Idioma del cliente
     * @return Mensaje de confirmación localizado y HTTP 200
     */
    @PutMapping
    public ResponseEntity<String> updateLicense(
            @PathVariable String organizationId,
            @RequestBody License license,
            Locale locale) {

        return ResponseEntity.ok(
                licenseService.updateLicense(organizationId, license, locale));
    }

    /**
     * Elimina una licencia de una organización.
     *
     * @param organizationId Identificador de la organización
     * @param licenseId      Identificador de la licencia a eliminar
     * @param locale         Idioma del cliente
     * @return Mensaje de confirmación localizado y HTTP 200
     */
    @DeleteMapping("/{licenseId}")
    public ResponseEntity<String> deleteLicense(
            @PathVariable String organizationId,
            @PathVariable String licenseId,
            Locale locale) {

        return ResponseEntity.ok(
                licenseService.deleteLicense(licenseId, organizationId, locale));
    }
}
