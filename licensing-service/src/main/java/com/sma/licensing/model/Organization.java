package com.sma.licensing.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Representación ligera (POJO) de una organización, usada para deserializar
 * la respuesta del organization-service. No es una entidad JPA: este servicio
 * no persiste organizaciones, solo las consume.
 */
@Getter
@Setter
@ToString
public class Organization {

    private String organizationId;
    private String name;
    private String contactName;
    private String contactEmail;
    private String contactPhone;
}
