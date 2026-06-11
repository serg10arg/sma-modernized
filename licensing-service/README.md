# licensing-service

## Descripción

Microservicio de gestión de licencias de software del sistema sma-modernized.
Expone una API REST para consultar, crear, actualizar y eliminar licencias
asociadas a organizaciones. Persiste los datos en PostgreSQL, obtiene su
configuración del servidor de configuración centralizada y se registra en el
servidor de descubrimiento. Al consultar una licencia, enriquece la respuesta con
los datos de su organización, obtenidos del organization-service mediante
descubrimiento de servicios.

## Etapa

Introducido en la **Etapa 2** (esqueleto REST con i18n, HATEOAS y healthcheck),
ampliado en la **Etapa 3** con persistencia en PostgreSQL y configuración
centralizada, y en la **Etapa 4** con el registro en Eureka, la comunicación con
el organization-service y la propagación de un identificador de correlación.

## Responsabilidades

- Exponer los endpoints REST CRUD de licencias por organización
- Persistir las licencias en PostgreSQL mediante Spring Data JPA
- Registrarse en el servidor de descubrimiento (Eureka)
- Enriquecer cada licencia con los datos de su organización, consultando el
  organization-service por descubrimiento de servicios
- Generar y propagar un identificador de correlación (`tmx-correlation-id`) entre servicios
- Devolver respuestas con links hipermedia (HATEOAS) e i18n según `Accept-Language`
- Publicar el endpoint de salud `/actuator/health`

## Tecnologías

| Tecnología | Versión | Rol |
|---|---|---|
| Java | 21 | Lenguaje principal |
| Spring Boot | 3.3.x | Framework base del microservicio |
| Spring Web (Tomcat) | 3.3.x | Servidor HTTP embebido y capa REST |
| Spring Data JPA | 3.3.x | Acceso a datos y mapeo objeto-relacional |
| Spring Cloud Config Client | 4.x | Lectura de configuración centralizada |
| Spring Cloud Netflix Eureka Client | 4.x | Registro y descubrimiento de servicios |
| Spring Cloud LoadBalancer | 4.x | Balanceo de carga del lado cliente |
| RestClient | 6.1+ | Cliente HTTP síncrono para llamar al organization-service |
| Spring HATEOAS | 3.3.x | Links hipermedia en las respuestas REST |
| Spring Boot Actuator | 3.3.x | Endpoints de salud y métricas |
| PostgreSQL | 16 | Base de datos relacional (base `sma_licensing`) |
| Lombok | - | Reducción de boilerplate en el modelo |
| Maven | 3.9.x | Herramienta de construcción |
| Docker | - | Contenerización del servicio |

## Configuración

El `application.yml` local contiene solo el nombre del servicio y la importación de
la configuración remota. El resto se sirve desde el config-server en
`config/licensing-service.yml`.

### Configuración local (`application.yml`)

| Propiedad | Valor | Descripción |
|---|---|---|
| `spring.application.name` | `licensing-service` | Nombre lógico; con él se registra en Eureka y solicita su config |
| `spring.config.import` | `configserver:${CONFIG_SERVER_URI:http://localhost:8888}` | URI del config-server |

### Variables de entorno

| Variable | Por defecto | Descripción |
|---|---|---|
| `CONFIG_SERVER_URI` | `http://localhost:8888` | URI del servidor de configuración |
| `EUREKA_SERVER_URI` | `http://localhost:8761/eureka/` | URI del servidor de descubrimiento |
| `DB_HOST` | `localhost` | Host de PostgreSQL |
| `DB_PORT` | `5432` | Puerto de PostgreSQL |
| `DB_NAME` | `sma_licensing` | Nombre de la base de datos |
| `DB_USER` | `sma_user` | Usuario de la base de datos |
| `DB_PASSWORD` | `sma_password` | Contraseña de la base de datos |

### Internacionalización

Mensajes en `messages.properties` (inglés) y `messages_es.properties` (español),
con placeholders nativos de `MessageSource` (`{0}`, `{1}`). El idioma se selecciona
según el header `Accept-Language`.

### Identificador de correlación

Cada petición entrante recibe un identificador de correlación: si la cabecera
`tmx-correlation-id` viene en la petición, se reutiliza; si no, se genera uno nuevo.
Ese identificador se devuelve en la respuesta y se propaga automáticamente en las
llamadas salientes al organization-service, permitiendo seguir el rastro de una
operación a través de los servicios. Es la base sobre la que se construirá la
trazabilidad distribuida en una etapa posterior.

## Ejecución local

Requiere config-server, Eureka, PostgreSQL y el organization-service disponibles.
Lo más simple es Docker Compose.

```bash
docker compose up --build licensing-service
```

## API

Todos los endpoints aceptan el header `Accept-Language` (`en` o `es`) y, opcionalmente,
`tmx-correlation-id`.

### GET /v1/organization/{organizationId}/license/{licenseId}

Obtiene una licencia, enriquecida con los datos de su organización y con links HATEOAS.
Devuelve HTTP 404 si la licencia no existe.

**Request:**

```
GET /v1/organization/org-001/license/lic-001
Accept-Language: es
tmx-correlation-id: test-corr-123
```

**Response (HTTP 200):**

```json
{
  "licenseId": "lic-001",
  "organizationId": "org-001",
  "description": "Licencia de prueba",
  "productName": "O-stock",
  "licenseType": "full",
  "comment": "Registro de ejemplo",
  "organization": {
    "organizationId": "org-001",
    "name": "Organización de prueba",
    "contactName": "Ana Contacto",
    "contactEmail": "ana@ejemplo.com",
    "contactPhone": "600000000"
  },
  "_links": {
    "self": { "href": "http://localhost:8080/v1/organization/org-001/license/lic-001" },
    "createLicense": { "href": "http://localhost:8080/v1/organization/org-001/license" },
    "updateLicense": { "href": "http://localhost:8080/v1/organization/org-001/license" },
    "deleteLicense": { "href": "http://localhost:8080/v1/organization/org-001/license/lic-001" }
  }
}
```

La respuesta incluye además la cabecera `tmx-correlation-id` con el valor recibido o generado.

---

### GET /v1/organization/{organizationId}/license

Lista todas las licencias de una organización.

---

### POST /v1/organization/{organizationId}/license

Crea una nueva licencia. Asigna un `licenseId` aleatorio y la persiste.

---

### PUT /v1/organization/{organizationId}/license

Actualiza una licencia existente.

---

### DELETE /v1/organization/{organizationId}/license/{licenseId}

Elimina una licencia. Devuelve HTTP 404 si no existe.

---

### GET /actuator/health

Verifica el estado operacional del servicio.

```json
{ "status": "UP" }
```

## Decisiones técnicas

| Decisión tomada | Alternativas consideradas | Motivo de la elección |
|---|---|---|
| `RestClient` con `@LoadBalanced` | `RestTemplate`, OpenFeign, `WebClient` | `RestTemplate` está en mantenimiento; `RestClient` es el cliente síncrono moderno; el balanceo permite llamar al servicio por nombre lógico |
| Descubrimiento por nombre vía Eureka | URL fija del organization-service | Desacopla los servicios de direcciones físicas; las instancias pueden cambiar sin reconfigurar |
| Propagar el error si el servicio remoto falla | Degradación manual con try/catch | La resiliencia (circuit breaker, fallback) se implementará correctamente con Resilience4j en una etapa posterior; un try/catch ahora sería código desechable |
| Modelo `Organization` ligero (POJO) | Reutilizar la entidad del organization-service | El licensing-service solo consume organizaciones, no las persiste; no necesita una entidad JPA |
| Campo `@Transient organization` en License | Crear un DTO de respuesta separado | Mantiene la etapa simple; el campo no se persiste en la base del licensing-service |
| correlationId con `ThreadLocal` + filtro + interceptor | No implementarlo aún | Sienta la base de la trazabilidad distribuida y evita retrabajo en etapas posteriores |

## Dependencias con otros servicios

| Servicio | Estado | Motivo |
|---|---|---|
| `config-server` | Activo | Provee la configuración al arrancar |
| `eureka-server` | Activo | Registro propio y descubrimiento del organization-service |
| `organization-service` | Activo | Provee los datos de organización para enriquecer las licencias |
| PostgreSQL | Activo | Almacena las licencias (base `sma_licensing`) |

Las siguientes se incorporarán en etapas futuras:

| Servicio | Etapa | Motivo |
|---|---|---|
| `gateway-server` | Etapa 6 | Enrutamiento centralizado de peticiones |
