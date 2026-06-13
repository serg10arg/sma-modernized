package com.sma.licensing.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

/**
 * Representación ligera (POJO) de una organización, usada para deserializar
 * la respuesta del organization-service y como entrada cacheada en Redis.
 * No es una entidad JPA: este servicio no persiste organizaciones, las consume.
 */
@Getter
@Setter
@ToString
@RedisHash(value = "organization", timeToLive = 600) // TTL 10 min como red de seguridad
public class Organization {

    @Id // org.springframework.data.annotation.Id (clave del hash en Redis)
    private String organizationId;
    private String name;
    private String contactName;
    private String contactEmail;
    private String contactPhone;
}
