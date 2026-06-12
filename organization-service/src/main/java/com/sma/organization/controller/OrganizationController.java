package com.sma.organization.controller;

import com.sma.organization.model.Organization;
import com.sma.organization.service.OrganizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST del servicio de organizaciones.
 * Expone endpoints CRUD bajo /v1/organization.
 */
@RestController
@RequestMapping("/v1/organization")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    /**
     * Obtiene una organización por su id.
     */
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{organizationId}")
    public ResponseEntity<Organization> getOrganization(@PathVariable String organizationId) {
        return ResponseEntity.ok(organizationService.getOrganization(organizationId));
    }

    /**
     * Lista todas las organizaciones.
     */
    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public ResponseEntity<List<Organization>> getAllOrganizations() {
        return ResponseEntity.ok(organizationService.getAllOrganizations());
    }

    /**
     * Crea una nueva organización.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Organization> createOrganization(@RequestBody Organization organization) {
        return ResponseEntity.ok(organizationService.createOrganization(organization));
    }

    /**
     * Actualiza una organización existente.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{organizationId}")
    public ResponseEntity<Organization> updateOrganization(
            @PathVariable String organizationId,
            @RequestBody Organization organization) {
        organization.setOrganizationId(organizationId);
        return ResponseEntity.ok(organizationService.updateOrganization(organization));
    }

    /**
     * Elimina una organización.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{organizationId}")
    public ResponseEntity<Void> deleteOrganization(@PathVariable String organizationId) {
        organizationService.deleteOrganization(organizationId);
        return ResponseEntity.noContent().build();
    }
}
