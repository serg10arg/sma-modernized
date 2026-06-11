package com.sma.organization.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.hateoas.RepresentationModel;

/**
 * Entidad JPA que representa una organización, mapeada a la tabla 'organizations'.
 * Extiende RepresentationModel para soportar links HATEOAS.
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "organizations")
public class Organization extends RepresentationModel<Organization> {

    // Clave primaria: identificador único de la organización
    @Id
    @Column(name = "organization_id", nullable = false)
    private String organizationId;

    // Nombre de la organización
    @Column(name = "name", nullable = false)
    private String name;

    // Nombre de la persona de contacto
    @Column(name = "contact_name")
    private String contactName;

    // Email de contacto
    @Column(name = "contact_email")
    private String contactEmail;

    // Teléfono de contacto
    @Column(name = "contact_phone")
    private String contactPhone;
}
