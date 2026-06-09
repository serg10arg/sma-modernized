package com.sma.licensing.controller;

import com.sma.licensing.model.License;
import com.sma.licensing.service.LicenseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para operaciones sobre licencias.
 * Expone los endpoints bajo /v1/organization/{organizationId}/license.
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
     *
     * @param organizationId Identificador de la organización
     * @param licenseId      Identificador de la licencia
     * @return Licencia encontrada con HTTP 200
     */
    @GetMapping("/{licenseId}")
    public ResponseEntity<License> getLicense(
            @PathVariable String organizationId,
            @PathVariable String licenseId) {

        License license = licenseService.getLicense(licenseId, organizationId);
        return ResponseEntity.ok(license);
    }
}
