package com.sma.licensing.service;

import com.sma.licensing.model.License;
import org.springframework.stereotype.Service;

/**
 * Lógica de negocio para la gestión de licencias.
 * En esta etapa los datos se generan en memoria; en etapas
 * posteriores se conectará a una base de datos real.
 */
@Service
public class LicenseService {

    /**
     * Recupera una licencia por su identificador y el de su organización.
     * Devuelve datos de ejemplo hasta que se integre la capa de persistencia.
     *
     * @param licenseId      Identificador de la licencia
     * @param organizationId Identificador de la organización
     * @return Licencia encontrada
     */
    public License getLicense(String licenseId, String organizationId) {
        return new License(
                licenseId,
                organizationId,
                "O-stock",
                "full",
                "Licencia de software para gestión de activos"
        );
    }
}
