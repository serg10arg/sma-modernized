package com.sma.licensing.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.hateoas.RepresentationModel;

/**
 * Modelo de dominio que representa una licencia de software.
 * Extiende RepresentationModel para soportar links HATEOAS en las respuestas.
 * Se convierte de record a clase para permitir la herencia requerida por HATEOAS.
 */
@Getter
@Setter
@ToString
public class License extends RepresentationModel<License> {

    // Identificador interno de la licencia
    private int id;

    // Identificador único de negocio de la licencia
    private String licenseId;

    // Descripción de la licencia
    private String description;

    // Identificador de la organización propietaria
    private String organizationId;

    // Nombre del producto al que pertenece esta licencia
    private String productName;

    // Tipo de licencia (full, trial, etc.)
    private String licenseType;

    // Comentario adicional inyectado desde configuración externa (etapas futuras)
    private String comment;
}
