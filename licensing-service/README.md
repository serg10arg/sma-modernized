# licensing-service

## Descripción

Microservicio de gestión de licencias de software del sistema sma-modernized.
Expone una API REST para consultar, crear, actualizar y eliminar licencias
asociadas a organizaciones. Persiste los datos en PostgreSQL mediante Spring
Data JPA y obtiene su configuración del servidor de configuración centralizada.
Las respuestas incluyen links hipermedia (HATEOAS) y los mensajes se adaptan al
idioma del cliente.

## Etapa

Introducido en la **Etapa 2** como esqueleto REST básico, completado en la misma
etapa con internacionalización, Spring HATEOAS y un Dockerfile con healthcheck.
En la **Etapa 3** se incorporó la persistencia en PostgreSQL con Spring Data JPA
y la integración con el servidor de configuración centralizada.

Las etapas posteriores añadirán el registro en Eureka, la comunicación con el
organization-service y la seguridad OAuth2.

## Responsabilidades

- Exponer los endpoints REST CRUD de licencias por organización
- Persistir las licencias en PostgreSQL mediante Spring Data JPA
- Devolver la representación JSON de una licencia con links hipermedia (HATEOAS)
- Adaptar los mensajes de respuesta al idioma del cliente (header `Accept-Language`)
- Obtener su configuración del config-server al arrancar
- Publicar el endpoint de salud `/actuator/health` para verificación operacional

## Tecnologías

| Tecnología | Versión | Rol |
|---|---|---|
| Java | 21 | Lenguaje principal |
| Spring Boot | 3.3.x | Framework base del microservicio |
| Spring Web (Tomcat) | 3.3.x | Servidor HTTP embebido y capa REST |
| Spring Data JPA | 3.3.x | Acceso a datos y mapeo objeto-relacional |
| Spring Cloud Config Client | 4.x | Lectura de configuración centralizada |
| Spring HATEOAS | 3.3.x | Links hipermedia en las respuestas REST |
| Spring Boot Actuator | 3.3.x | Endpoints de salud y métricas |
| PostgreSQL | 16 | Base de datos relacional |
| Lombok | - | Reducción de boilerplate en el modelo |
| Maven | 3.9.x | Herramienta de construcción |
| Docker | - | Contenerización del servicio |

## Configuración

El servicio mantiene en su `application.yml` local únicamente el nombre del
servicio y la importación de la configuración remota. El resto de la
configuración (puerto, datasource, JPA) se sirve desde el config-server, en el
archivo `config/licensing-service.yml`.

### Configuración local (`application.yml`)

| Propiedad | Valor | Descripción |
|---|---|---|
| `spring.application.name` | `licensing-service` | Determina qué archivo de configuración solicita al config-server |
| `spring.config.import` | `configserver:${CONFIG_SERVER_URI:http://localhost:8888}` | URI del config-server; sobreescribible por variable de entorno |

### Configuración centralizada (servida por el config-server)

| Propiedad | Valor | Descripción |
|---|---|---|
| `server.port` | `8080` | Puerto del servidor HTTP |
| `spring.datasource.url` | `jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}` | URL de conexión a PostgreSQL |
| `spring.jpa.hibernate.ddl-auto` | `none` | El esquema lo gestiona `schema.sql`, no Hibernate |
| `spring.sql.init.mode` | `always` | Ejecuta `schema.sql` y `data.sql` al arrancar |

### Variables de entorno

| Variable | Por defecto | Descripción |
|---|---|---|
| `CONFIG_SERVER_URI` | `http://localhost:8888` | URI del servidor de configuración |
| `DB_HOST` | `localhost` | Host de PostgreSQL |
| `DB_PORT` | `5432` | Puerto de PostgreSQL |
| `DB_NAME` | `sma_licensing` | Nombre de la base de datos |
| `DB_USER` | `sma_user` | Usuario de la base de datos |
| `DB_PASSWORD` | `sma_password` | Contraseña de la base de datos |

### Internacionalización

Los mensajes se externalizan en `src/main/resources`:

- `messages.properties` — inglés (idioma por defecto)
- `messages_es.properties` — español

Usan placeholders nativos de `MessageSource` (`{0}`, `{1}`). El idioma se
selecciona automáticamente según el header `Accept-Language`.

## Ejecución local

El servicio requiere que el config-server y una instancia de PostgreSQL estén
disponibles. La forma más sencilla es levantar todo con Docker Compose.

**Con Docker Compose (desde la raíz del monorepo):**

```bash
docker compose up --build licensing-service
```

**Solo el servicio con Maven (requiere config-server y PostgreSQL ya activos):**

```bash
mvn clean package -pl licensing-service -am -DskipTests
mvn spring-boot:run -pl licensing-service
```

## API

Todos los endpoints aceptan el header `Accept-Language` (`en` o `es`).

### GET /v1/organization/{organizationId}/license/{licenseId}

Obtiene una licencia específica, con links HATEOAS. Devuelve HTTP 404 si no existe.

**Request:**

```
GET /v1/organization/org-001/license/lic-001
Accept-Language: es
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
  "_links": {
    "self": { "href": "http://localhost:8080/v1/organization/org-001/license/lic-001" },
    "createLicense": { "href": "http://localhost:8080/v1/organization/org-001/license" },
    "updateLicense": { "href": "http://localhost:8080/v1/organization/org-001/license" },
    "deleteLicense": { "href": "http://localhost:8080/v1/organization/org-001/license/lic-001" }
  }
}
```

**Response (HTTP 404):**

```json
{
  "error": "No se puede encontrar la licencia con id NO-EXISTE para la organización org-001"
}
```

---

### GET /v1/organization/{organizationId}/license

Lista todas las licencias de una organización.

**Response (HTTP 200):**

```json
[
  {
    "licenseId": "lic-001",
    "organizationId": "org-001",
    "description": "Licencia de prueba",
    "productName": "O-stock",
    "licenseType": "full",
    "comment": "Registro de ejemplo"
  }
]
```

---

### POST /v1/organization/{organizationId}/license

Crea una nueva licencia. Asigna un `licenseId` aleatorio y la persiste.

**Request:**

```
POST /v1/organization/org-001/license
Accept-Language: es
Content-Type: application/json

{
  "description": "Licencia de produccion",
  "productName": "O-stock",
  "licenseType": "full"
}
```

**Response (HTTP 200):**

```
Licencia creada License(licenseId=..., organizationId=org-001, ...)
```

---

### PUT /v1/organization/{organizationId}/license

Actualiza una licencia existente.

**Request:**

```
PUT /v1/organization/org-001/license
Accept-Language: es
Content-Type: application/json

{
  "licenseId": "lic-001",
  "description": "Descripción actualizada",
  "productName": "O-stock",
  "licenseType": "full"
}
```

**Response (HTTP 200):**

```
Licencia actualizada License(...)
```

---

### DELETE /v1/organization/{organizationId}/license/{licenseId}

Elimina una licencia. Devuelve HTTP 404 si no existe.

**Response (HTTP 200):**

```
Licencia eliminada lic-001
```

---

### GET /actuator/health

Verifica el estado operacional del servicio. Usado por el healthcheck del contenedor.

```json
{ "status": "UP" }
```

## Decisiones técnicas

| Decisión tomada | Alternativas consideradas | Motivo de la elección |
|---|---|---|
| PostgreSQL 16 | MySQL, H2 en memoria | Base de datos robusta estándar; con volumen persistente sobrevive a reinicios, a diferencia de H2 |
| Spring Data JPA | JDBC plano, MyBatis | Reduce boilerplate; integración nativa con Spring Boot |
| `licenseId` (String) como clave primaria | Campo `id` numérico autogenerado | El identificador de negocio es único y significativo; evita una clave artificial redundante |
| `ddl-auto: none` + `schema.sql` | `ddl-auto: update` de Hibernate | Control explícito del esquema; `update` no es seguro ni predecible para entornos reales |
| `spring.config.import` | `bootstrap.yml` | El `bootstrap.yml` está obsoleto en Spring Boot 3.x; `spring.config.import` es el mecanismo actual |
| `MessageSource` con placeholders `{0}` | `String.format` con `%s` | Es el mecanismo nativo de Spring para i18n; evita el doble procesamiento de cadenas |
| Una sola clase `License` (entidad + HATEOAS) | DTO separado de la entidad | Mantiene la etapa simple; la separación en DTO queda como posible refactor futuro |

## Dependencias con otros servicios

| Servicio | Estado | Motivo |
|---|---|---|
| `config-server` | Activo | Provee la configuración del servicio al arrancar |
| PostgreSQL | Activo | Almacena las licencias |

Las siguientes se incorporarán en etapas futuras:

| Servicio | Etapa | Motivo |
|---|---|---|
| `eureka-server` | Etapa 4 | Registro y descubrimiento de servicio |
| `organization-service` | Etapa 4 | Resolución del nombre de la organización por ID |
| `gateway-server` | Etapa 6 | Enrutamiento centralizado de peticiones |