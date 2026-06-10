package com.sma.licensing.controller;

import com.sma.licensing.model.License;
import com.sma.licensing.service.LicenseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Controlador REST del servicio de licencias.
 * Expone endpoints CRUD bajo /v1/organization/{organizationId}/license.
 * Incluye internacionalización (header Accept-Language) y links HATEOAS.
 */
@RestController
@RequestMapping("/v1/organization/{organizationId}/license")
public class LicenseController {

    private final LicenseService licenseService;

    public LicenseController(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    /**
     * Obtiene una licencia específica de una organización, con links HATEOAS.
     */
    @GetMapping("/{licenseId}")
    public ResponseEntity<License> getLicense(
            @PathVariable String organizationId,
            @PathVariable String licenseId,
            Locale locale) {

        License license = licenseService.getLicense(licenseId, organizationId, locale);

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
     * Lista todas las licencias de una organización.
     */
    @GetMapping
    public ResponseEntity<List<License>> getLicenses(@PathVariable String organizationId) {
        return ResponseEntity.ok(licenseService.getLicensesByOrganization(organizationId));
    }

    /**
     * Crea una nueva licencia para una organización.
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
