package com.sma.licensing.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.hateoas.RepresentationModel;

/**
 * Entidad JPA que representa una licencia de software, mapeada a la tabla 'licenses'.
 * Extiende RepresentationModel para soportar links HATEOAS en las respuestas REST.
 * La clave primaria es licenseId (identificador de negocio de tipo String).
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "licenses")
public class License extends RepresentationModel<License> {

    // Clave primaria: identificador único de negocio de la licencia
    @Id
    @Column(name = "license_id", nullable = false)
    private String licenseId;

    // Identificador de la organización propietaria
    @Column(name = "organization_id", nullable = false)
    private String organizationId;

    // Descripción de la licencia
    @Column(name = "description")
    private String description;

    // Nombre del producto al que pertenece esta licencia
    @Column(name = "product_name", nullable = false)
    private String productName;

    // Tipo de licencia (full, trial, etc.)
    @Column(name = "license_type", nullable = false)
    private String licenseType;

    // Comentario adicional asociado a la licencia
    @Column(name = "comment")
    private String comment;
}
