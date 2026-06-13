# licensing-service

## Descripción

Microservicio de gestión de licencias de software. Expone una API REST para
operaciones CRUD sobre licencias asociadas a organizaciones. Para enriquecer la
licencia con los datos de su organización propietaria, consulta al
`organization-service` descubriéndolo por nombre lógico en Eureka, cachea esa
información en Redis (patrón cache-aside) y reacciona a los eventos de cambio de
organización publicados en Kafka para mantener la caché coherente.

## Etapa

Introducido en la **Etapa 2** (esqueleto REST). Ampliado en etapas posteriores:

- **Etapa 3:** persistencia con PostgreSQL y Spring Data JPA.
- **Etapa 4:** comunicación con el `organization-service` y propagación del
  identificador de correlación.
- **Etapa 5:** resiliencia con Resilience4j (circuit breaker, retry, bulkhead,
  rate limiter).
- **Etapa 6:** acceso a través del `gateway-server` (deja de exponer puerto
  público).
- **Etapa 7:** seguridad OAuth2 + JWT como *resource server*.
- **Etapa 8:** consumidor de eventos Kafka y caché distribuida con Redis.

## Responsabilidades

- CRUD de licencias bajo `/v1/organization/{organizationId}/license`.
- Consultar la organización propietaria en el `organization-service` mediante
  descubrimiento (Eureka + Spring Cloud LoadBalancer).
- Cachear las organizaciones consultadas en Redis (cache-aside) para reducir las
  llamadas remotas.
- Invalidar la entrada de caché al recibir un evento de cambio de organización
  (`UPDATED` / `DELETED`) por Kafka.
- Degradar con elegancia ante fallos del `organization-service` (circuit breaker,
  retry, bulkhead, rate limiter) y ante un Redis no disponible (la petición
  continúa contra la fuente).
- Internacionalizar los mensajes según el header `Accept-Language` y añadir links
  HATEOAS a las respuestas.
- Validar el token JWT y autorizar por rol como *resource server* OAuth2.

## Tecnologías

- Java 21, Spring Boot 3.3.x
- Spring Web (REST) y Spring HATEOAS
- Spring Data JPA + PostgreSQL
- Spring Cloud Config Client (`spring.config.import`) y Eureka Client
- Spring Cloud LoadBalancer + `RestClient` anotado con `@LoadBalanced`
- Resilience4j (circuit breaker, retry, bulkhead, rate limiter)
- Spring Cloud Stream + binder de Kafka (consumidor)
- Spring Data Redis (cliente Lettuce) para la caché
- Spring Security 6 + OAuth2 Resource Server (JWT)
- `MessageSource` para internacionalización

## Configuración

La configuración se obtiene del `config-server` mediante `spring.config.import`.
Propiedades relevantes:

| Propiedad | Descripción |
|---|---|
| `spring.datasource.*` | Conexión a la base `sma_licensing` (PostgreSQL). `ddl-auto: none` + `schema.sql` |
| `eureka.client.*` | Registro en el `eureka-server` |
| `spring.data.redis.host` / `spring.data.redis.port` | Conexión a Redis (`${REDIS_HOST:redis}:${REDIS_PORT:6379}`) |
| `spring.cloud.function.definition` | `organizationChange` (función consumidora) |
| `spring.cloud.stream.bindings.organizationChange-in-0` | Binding de entrada al topic `sma.organization.changes`, grupo `licensing-group` |
| `resilience4j.*` | Instancias `organizationService` (circuit breaker), `retryLicenseService`, bulkhead y rate limiter |
| `spring.security.oauth2.resourceserver.jwt.issuer-uri` | `http://keycloak:8080/realms/sma` |
| `management.health.redis.enabled` | `false` — la caché es no crítica: un Redis caído no degrada la salud del servicio |

Las variables de entorno (`DB_*`, `REDIS_HOST`, `REDIS_PORT`, etc.) se definen en
el archivo `.env` de la raíz.

## Ejecución local

- **Con Maven:** `mvn spring-boot:run -pl licensing-service` (requiere
  `config-server`, `eureka-server`, PostgreSQL, Redis, Kafka y Keycloak
  disponibles; en la práctica se levanta toda la pila con Docker Compose).
- **Con Docker:** forma parte de `docker compose up`. No expone puerto público: se
  accede a través del `gateway-server` en el puerto `8072`.

## API

Base (a través del gateway): `http://localhost:8072/licensing-service/v1/organization/{organizationId}/license`

Todas las llamadas requieren `Authorization: Bearer {JWT}`.

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/{licenseId}` | Obtiene una licencia (con links HATEOAS y datos de organización cacheados) |
| POST | `/` | Crea una nueva licencia |
| PUT | `/` | Actualiza una licencia existente |
| DELETE | `/{licenseId}` | Elimina una licencia |

**Ejemplo — obtener una licencia:**

Request:

```
GET http://localhost:8072/licensing-service/v1/organization/be1cd947-.../license/c4a1b2...
Authorization: Bearer eyJhbGciOiJSUzI1NiI...
Accept-Language: es
```

Response (`200 OK`):

```json
{
  "licenseId": "c4a1b2...",
  "organizationId": "be1cd947-...",
  "description": "Software product",
  "productName": "O-stock",
  "licenseType": "full",
  "organizationName": "ACME",
  "_links": {
    "self": { "href": ".../license/c4a1b2..." },
    "createLicense": { "href": ".../license" },
    "updateLicense": { "href": ".../license" },
    "deleteLicense": { "href": ".../license/c4a1b2..." }
  }
}
```

## Decisiones técnicas

| Decisión tomada | Alternativas consideradas | Motivo de la elección |
|---|---|---|
| `RestClient` con `@LoadBalanced` para la llamada entre servicios | `RestTemplate` (en desuso), Feign | Cliente moderno de Spring 6 con descubrimiento por nombre lógico |
| Caché cache-aside escrita a mano en un wrapper | Abstracción declarativa `@Cacheable` | Control explícito: evitar cachear el fallback y tolerar un Redis caído sin romper la petición |
| Cliente Redis Lettuce por auto-configuración | Jedis con `JedisConnectionFactory` manual | Es el cliente por defecto de Spring Boot; sin configuración manual |
| `@RedisHash(timeToLive = 600)` en el modelo cacheado | Sin TTL | Red de seguridad ante entradas obsoletas (10 min) |
| Invalidación por evento (`deleteById` en `UPDATED`/`DELETED`) | Confiar solo en el TTL | Coherencia inmediata tras un cambio de organización |
| `management.health.redis.enabled: false` | Indicador de salud de Redis activo | Desacopla la disponibilidad de la caché (no crítica) de la salud del servicio |
| Circuit breaker de la llamada entre servicios ubicado en `OrganizationRestClient` | Ubicarlo en `LicenseService` | Evita la auto-invocación: el proxy AOP no se aplica en llamadas dentro del mismo bean |
| `spring.config.import` para el Config Client | `bootstrap.yml` | `bootstrap.yml` está obsoleto en Spring Boot 3.x |

## Dependencias con otros servicios

- `config-server` — configuración centralizada.
- `eureka-server` — descubrimiento de servicios.
- PostgreSQL — base de datos `sma_licensing`.
- `organization-service` — datos de organización (vía Eureka).
- Redis — caché distribuida de organizaciones.
- Kafka — eventos de cambio de organización (topic `sma.organization.changes`).
- Keycloak — validación del token JWT.
- `gateway-server` — entrada única al sistema.