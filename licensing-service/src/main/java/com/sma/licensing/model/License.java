package com.sma.licensing.model;

/**
 * Representa una licencia de software asignada a una organización.
 * Se usa Java record (Java 21) en lugar de Lombok para inmutabilidad nativa.
 *
 * @param licenseId      Identificador único de la licencia
 * @param organizationId Identificador de la organización propietaria
 * @param productName    Nombre del producto licenciado
 * @param licenseType    Tipo de licencia (por ejemplo: full, trial)
 * @param description    Descripción adicional de la licencia
 */
public record License(
        String licenseId,
        String organizationId,
        String productName,
        String licenseType,
        String description
) {}
