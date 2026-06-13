# organization-service

## Descripción

Microservicio de gestión de organizaciones. Expone una API REST para operaciones
CRUD sobre organizaciones y las persiste en su propia base de datos. Cada vez que
una organización se crea, actualiza o elimina, publica un evento de cambio en
Kafka para que otros servicios (como el `licensing-service`) reaccionen, por
ejemplo invalidando su caché.

## Etapa

Introducido en la **Etapa 4**. Ampliado en etapas posteriores:

- **Etapa 7:** seguridad OAuth2 + JWT como *resource server*.
- **Etapa 8:** productor de eventos de cambio de organización en Kafka.

## Responsabilidades

- CRUD de organizaciones bajo `/v1/organization`.
- Persistir las organizaciones en la base `sma_organization` (PostgreSQL).
- Registrarse en Eureka para ser descubierto por otros servicios.
- Publicar un evento `OrganizationChangeModel` (`CREATED` / `UPDATED` / `DELETED`)
  en Kafka tras cada mutación, mediante `StreamBridge`.
- Validar el token JWT y autorizar por rol como *resource server* OAuth2.

## Tecnologías

- Java 21, Spring Boot 3.3.x
- Spring Web (REST) y Spring Data JPA + PostgreSQL
- Spring Cloud Config Client (`spring.config.import`) y Eureka Client
- Spring Cloud Stream + binder de Kafka (productor, vía `StreamBridge`)
- Spring Security 6 + OAuth2 Resource Server (JWT)

## Configuración

La configuración se obtiene del `config-server` mediante `spring.config.import`.
Propiedades relevantes:

| Propiedad | Descripción |
|---|---|
| `spring.datasource.*` | Conexión a la base `sma_organization` (PostgreSQL). `ddl-auto: none` + `schema.sql` |
| `eureka.client.*` | Registro en el `eureka-server` |
| `spring.cloud.stream.bindings.organizationChange-out-0` | Binding de salida hacia el topic `sma.organization.changes` |
| `spring.cloud.stream.kafka.binder.brokers` | `kafka:9092` |
| `spring.security.oauth2.resourceserver.jwt.issuer-uri` | `http://keycloak:8080/realms/sma` |

## Ejecución local

- **Con Maven:** `mvn spring-boot:run -pl organization-service` (requiere el resto
  de la pila disponible; en la práctica se levanta con Docker Compose).
- **Con Docker:** forma parte de `docker compose up`. No expone puerto público: se
  accede a través del `gateway-server` en el puerto `8072`.

## API

Base (a través del gateway, por ruta de descubrimiento):
`http://localhost:8072/organization-service/v1/organization`

Todas las llamadas requieren `Authorization: Bearer {JWT}`.

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/{organizationId}` | Obtiene una organización por su id |
| POST | `/` | Crea una nueva organización (publica evento `CREATED`) |
| PUT | `/{organizationId}` | Actualiza una organización (publica evento `UPDATED`) |
| DELETE | `/{organizationId}` | Elimina una organización (publica evento `DELETED`) |

> **Nota sobre el enrutado:** la ruta de colección con barra final
> (`/organization/`) tiene una limitación conocida en el gateway. Para crear y
> listar organizaciones se utiliza la ruta de descubrimiento
> `/organization-service/v1/organization`.

**Ejemplo — crear una organización:**

Request:

```
POST http://localhost:8072/organization-service/v1/organization
Authorization: Bearer eyJhbGciOiJSUzI1NiI...
Content-Type: application/json

{
  "organizationId": "be1cd947-...",
  "name": "ACME",
  "contactName": "Jane Doe",
  "contactEmail": "jane@acme.com",
  "contactPhone": "600000000"
}
```

Tras la respuesta `200 OK`, el servicio publica en `sma.organization.changes` un
evento con `action = CREATED` y el `organizationId` correspondiente.

## Decisiones técnicas

| Decisión tomada | Alternativas consideradas | Motivo de la elección |
|---|---|---|
| Base de datos propia `sma_organization` | Compartir base con `licensing-service` | Aislamiento de datos por servicio (cada microservicio es dueño de sus datos) |
| Un único contenedor PostgreSQL con dos bases | Dos contenedores PostgreSQL | Simplicidad operativa, manteniendo bases separadas por servicio |
| `StreamBridge` para publicar los eventos | Binding de salida declarativo con `Supplier` | Publicación imperativa justo tras la mutación de dominio, más natural para eventos disparados por acción |
| `record OrganizationChangeModel` + `enum ActionEnum` | Clase POJO mutable | Inmutabilidad y concisión del modelo de evento |
| `spring.config.import` para el Config Client | `bootstrap.yml` | `bootstrap.yml` está obsoleto en Spring Boot 3.x |

## Dependencias con otros servicios

- `config-server` — configuración centralizada.
- `eureka-server` — descubrimiento de servicios.
- PostgreSQL — base de datos `sma_organization`.
- Kafka — publicación de eventos de cambio (topic `sma.organization.changes`).
- Keycloak — validación del token JWT.
- `gateway-server` — entrada única al sistema.

## Limitaciones conocidas

- Los eventos publicados llevan el `correlationId` a `null`: este servicio aún no
  lee ni reemite el header `tmx-correlation-id`. Se resolverá en la etapa de
  trazabilidad distribuida (Etapa 9).