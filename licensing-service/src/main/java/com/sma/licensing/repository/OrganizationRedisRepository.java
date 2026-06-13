package com.sma.licensing.repository;

import com.sma.licensing.model.Organization;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Redis para organizaciones cacheadas.
 * CrudRepository sobre una entidad @RedisHash: Spring Data Redis lo enruta
 * automáticamente al store de Redis (no a JPA).
 */
@Repository
public interface OrganizationRedisRepository extends CrudRepository<Organization, String> {
}
