# licensing-service

## Descripción

Microservicio de gestión de licencias de software del sistema sma-modernized.
Expone una API REST para consultar, crear, actualizar y eliminar licencias
asociadas a organizaciones. En esta etapa los datos se sirven en memoria, sin
dependencias de infraestructura externa. Las respuestas incluyen links
hipermedia (HATEOAS) y los mensajes se adaptan al idioma del cliente.

## Etapa

Introducido en la **Etapa 2** como esqueleto REST básico y completado en la
misma etapa con internacionalización, Spring HATEOAS y un Dockerfile con
healthcheck.

Las etapas posteriores añadirán persistencia con PostgreSQL, integración con
Config Server, registro en Eureka, comunicación con el organization-service y
seguridad OAuth2.

## Responsabilidades

- Exponer los endpoints REST CRUD de licencias por organización
- Devolver la representación JSON de una licencia con links hipermedia (HATEOAS)
- Adaptar los mensajes de respuesta al idioma del cliente (header `Accept-Language`)
- Publicar el endpoint de salud `/actuator/health` para verificación operacional
- Actuar como módulo Maven independiente dentro del monorepo `sma-modernized`

## Tecnologías

| Tecnología | Versión | Rol |
|---|---|---|
| Java | 21 | Lenguaje principal |
| Spring Boot | 3.3.x | Framework base del microservicio |
| Spring Web (Tomcat) | 3.3.x | Servidor HTTP embebido y capa REST |
| Spring HATEOAS | 3.3.x | Links hipermedia en las respuestas REST |
| Spring Boot Actuator | 3.3.x | Endpoints de salud y métricas |
| Lombok | - | Reducción de boilerplate en el modelo |
| Maven | 3.9.x | Herramienta de construcción |
| Docker | - | Contenerización del servicio |

## Configuración

El servicio se configura mediante `src/main/resources/application.yml`.

| Propiedad | Valor por defecto | Descripción |
|---|---|---|
| `spring.application.name` | `licensing-service` | Nombre del servicio; usado por Eureka y Config Server en etapas futuras |
| `server.port` | `8080` | Puerto en el que escucha el servidor HTTP |
| `management.endpoints.web.exposure.include` | `health,info,metrics` | Endpoints de Actuator expuestos |
| `management.endpoint.health.show-details` | `always` | Nivel de detalle del endpoint de salud |

### Internacionalización

Los mensajes se externalizan en archivos de propiedades dentro de
`src/main/resources`:

- `messages.properties` — mensajes en inglés (idioma por defecto)
- `messages_es.properties` — mensajes en español

El idioma se selecciona automáticamente según el header `Accept-Language` de la
petición. Spring Boot resuelve el `Locale` y lo inyecta en los métodos del
controller.

## Ejecución local

**Con Maven (desde la raíz del monorepo):**

```bash
# Compilar solo este módulo y sus dependencias del monorepo
mvn clean package -pl licensing-service -am -DskipTests

# Arrancar el servicio
mvn spring-boot:run -pl licensing-service
```

**Con Docker (desde la raíz del monorepo):**

```bash
# Construir la imagen (build multi-etapa)
docker build -f licensing-service/Dockerfile -t licensing-service:local .

# Arrancar el contenedor
docker run -p 8080:8080 licensing-service:local
```

**Con Docker Compose (desde la raíz del monorepo):**

```bash
docker compose up licensing-service
```

## API

Todos los endpoints aceptan el header `Accept-Language` (`en` o `es`) para
seleccionar el idioma de los mensajes de respuesta.

### GET /v1/organization/{organizationId}/license/{licenseId}

Obtiene una licencia específica de una organización, con links HATEOAS.

**Ejemplo de request:**

```
GET /v1/organization/org-001/license/lic-001
Accept-Language: es
```

**Ejemplo de response (HTTP 200):**

```json
{
  "id": 0,
  "licenseId": "lic-001",
  "description": "Software product",
  "organizationId": "org-001",
  "productName": "O-stock",
  "licenseType": "full",
  "comment": "No se puede encontrar la licencia con id lic-001 para la organización org-001",
  "_links": {
    "self": {
      "href": "http://localhost:8080/v1/organization/org-001/license/lic-001"
    },
    "createLicense": {
      "href": "http://localhost:8080/v1/organization/org-001/license"
    },
    "updateLicense": {
      "href": "http://localhost:8080/v1/organization/org-001/license"
    },
    "deleteLicense": {
      "href": "http://localhost:8080/v1/organization/org-001/license/lic-001"
    }
  }
}
```

---

### POST /v1/organization/{organizationId}/license

Crea una nueva licencia para una organización. Asigna un `licenseId` aleatorio.

**Ejemplo de request:**

```
POST /v1/organization/org-001/license
Accept-Language: es
Content-Type: application/json

{
  "description": "Software product",
  "productName": "O-stock",
  "licenseType": "full"
}
```

**Ejemplo de response (HTTP 200):**

```
Licencia creada License(id=0, licenseId=..., description=Software product, organizationId=org-001, productName=O-stock, licenseType=full, comment=null)
```

---

### PUT /v1/organization/{organizationId}/license

Actualiza una licencia existente de una organización.

**Ejemplo de request:**

```
PUT /v1/organization/org-001/license
Accept-Language: es
Content-Type: application/json

{
  "licenseId": "lic-001",
  "description": "Software product",
  "productName": "O-stock",
  "licenseType": "full"
}
```

**Ejemplo de response (HTTP 200):**

```
Licencia actualizada License(...)
```

---

### DELETE /v1/organization/{organizationId}/license/{licenseId}

Elimina una licencia de una organización.

**Ejemplo de request:**

```
DELETE /v1/organization/org-001/license/lic-001
Accept-Language: es
```

**Ejemplo de response (HTTP 200):**

```
Licencia eliminada lic-001
```

---

### GET /actuator/health

Verifica el estado operacional del servicio. Usado por el healthcheck del
contenedor Docker.

**Response:**

```json
{
  "status": "UP"
}
```

## Decisiones técnicas

| Decisión tomada | Alternativas consideradas | Motivo de la elección |
|---|---|---|
| `License` como clase con Lombok | Mantener Java record | Spring HATEOAS requiere extender `RepresentationModel<License>`, lo cual exige herencia; los records no pueden extender clases |
| Internacionalización con `MessageSource` | Mensajes hardcodeados, librería externa | Es el mecanismo nativo de Spring; resuelve el `Locale` automáticamente desde `Accept-Language` |
| HATEOAS vía `WebMvcLinkBuilder` | Construcción manual de URLs, omitir HATEOAS | Genera los links a partir de los métodos del controller, evitando URLs hardcodeadas que se rompen al cambiar rutas |
| Dockerfile multi-etapa con healthcheck | Imagen única, sin healthcheck | El build multi-etapa reduce el tamaño de la imagen final; el healthcheck permite a Docker Compose esperar a que el servicio esté `healthy` |
| Datos en memoria | PostgreSQL desde el inicio | Mantiene el servicio autónomo en esta etapa; la persistencia se introduce en una etapa posterior |

## Dependencias con otros servicios

En esta etapa el servicio opera de forma completamente autónoma, sin dependencias
de otros servicios del sistema.

Las siguientes dependencias se incorporarán en etapas futuras:

| Servicio | Etapa | Motivo |
|---|---|---|
| `config-server` | Etapa 3 | Externalización de la configuración |
| PostgreSQL | Etapa 3 | Persistencia de las licencias |
| `eureka-server` | Etapa 4 | Registro y descubrimiento de servicio |
| `organization-service` | Etapa 4 | Resolución del nombre de la organización por ID |
| `gateway-server` | Etapa 6 | Enrutamiento centralizado de peticiones |