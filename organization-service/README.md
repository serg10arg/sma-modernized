# organization-service

## Descripción

Microservicio de gestión de organizaciones del sistema sma-modernized. Expone una
API REST para consultar, crear, actualizar y eliminar organizaciones. Persiste los
datos en su propia base de datos PostgreSQL, obtiene su configuración del servidor
de configuración centralizada y se registra en el servidor de descubrimiento para
ser localizable por otros servicios.

## Etapa

Introducido en la **Etapa 4**, cuando el sistema pasa a comportarse como una
arquitectura distribuida real. Es el segundo servicio de negocio del sistema y el
primero al que el licensing-service llama mediante descubrimiento de servicios.

## Responsabilidades

- Exponer los endpoints REST CRUD de organizaciones
- Persistir las organizaciones en su propia base de datos PostgreSQL
- Registrarse en el servidor de descubrimiento (Eureka) para ser localizable por nombre
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
| Spring Cloud Netflix Eureka Client | 4.x | Registro en el servidor de descubrimiento |
| Spring HATEOAS | 3.3.x | Soporte de links hipermedia en el modelo |
| Spring Boot Actuator | 3.3.x | Endpoints de salud y métricas |
| PostgreSQL | 16 | Base de datos relacional (base `sma_organization`) |
| Lombok | - | Reducción de boilerplate en el modelo |
| Maven | 3.9.x | Herramienta de construcción |
| Docker | - | Contenerización del servicio |

## Configuración

El `application.yml` local contiene solo el nombre del servicio y la importación de
la configuración remota. El resto (puerto, datasource, JPA, Eureka) se sirve desde
el config-server en `config/organization-service.yml`.

### Configuración local (`application.yml`)

| Propiedad | Valor | Descripción |
|---|---|---|
| `spring.application.name` | `organization-service` | Nombre lógico; con él se registra en Eureka y solicita su config |
| `spring.config.import` | `configserver:${CONFIG_SERVER_URI:http://localhost:8888}` | URI del config-server |

### Configuración centralizada (servida por el config-server)

| Propiedad | Valor | Descripción |
|---|---|---|
| `server.port` | `8081` | Puerto del servidor HTTP |
| `spring.datasource.url` | `jdbc:postgresql://${DB_HOST}:${DB_PORT}/${ORG_DB_NAME}` | Conexión a la base `sma_organization` |
| `spring.jpa.hibernate.ddl-auto` | `none` | El esquema lo gestiona `schema.sql` |
| `eureka.client.service-url.defaultZone` | `${EUREKA_SERVER_URI}` | URL del servidor de descubrimiento |

### Variables de entorno

| Variable | Por defecto | Descripción |
|---|---|---|
| `CONFIG_SERVER_URI` | `http://localhost:8888` | URI del servidor de configuración |
| `EUREKA_SERVER_URI` | `http://localhost:8761/eureka/` | URI del servidor de descubrimiento |
| `DB_HOST` | `localhost` | Host de PostgreSQL |
| `DB_PORT` | `5432` | Puerto de PostgreSQL |
| `ORG_DB_NAME` | `sma_organization` | Nombre de la base de datos |
| `ORG_DB_USER` | `sma_user` | Usuario de la base de datos |
| `ORG_DB_PASSWORD` | `sma_password` | Contraseña de la base de datos |

## Ejecución local

Requiere config-server, Eureka y PostgreSQL disponibles. Lo más simple es Docker Compose.

```bash
docker compose up --build organization-service
```

Solo el servicio con Maven (con la infraestructura ya activa):

```bash
mvn clean package -pl organization-service -am -DskipTests
mvn spring-boot:run -pl organization-service
```

## API

### GET /v1/organization/{organizationId}

Obtiene una organización por su id. Devuelve HTTP 404 si no existe.

**Response (HTTP 200):**

```json
{
  "organizationId": "org-001",
  "name": "Organización de prueba",
  "contactName": "Ana Contacto",
  "contactEmail": "ana@ejemplo.com",
  "contactPhone": "600000000"
}
```

---

### GET /v1/organization

Lista todas las organizaciones.

---

### POST /v1/organization

Crea una nueva organización. Asigna un `organizationId` aleatorio.

**Request:**

```json
{
  "name": "Nueva organización",
  "contactName": "Juan Pérez",
  "contactEmail": "juan@ejemplo.com",
  "contactPhone": "611111111"
}
```

**Response (HTTP 200):** la organización creada, con su `organizationId` asignado.

---

### PUT /v1/organization/{organizationId}

Actualiza una organización existente.

---

### DELETE /v1/organization/{organizationId}

Elimina una organización. Devuelve HTTP 204 si tiene éxito, 404 si no existe.

---

### GET /actuator/health

Verifica el estado operacional del servicio.

```json
{ "status": "UP" }
```

## Decisiones técnicas

| Decisión tomada | Alternativas consideradas | Motivo de la elección |
|---|---|---|
| Base de datos propia (`sma_organization`) | Compartir base con licensing-service | Aísla los datos de cada servicio; cada microservicio es dueño de su propia base |
| Detección automática del cliente Eureka | Anotación `@EnableEurekaClient` explícita | En el stack actual la anotación es opcional; basta la dependencia en el classpath |
| `organizationId` (String) como clave primaria | Campo numérico autogenerado | Identificador de negocio único y significativo |
| `ddl-auto: none` + `schema.sql` | `ddl-auto: update` de Hibernate | Control explícito del esquema |

## Dependencias con otros servicios

| Servicio | Estado | Motivo |
|---|---|---|
| `config-server` | Activo | Provee la configuración al arrancar |
| `eureka-server` | Activo | Registro para ser descubierto por otros servicios |
| PostgreSQL | Activo | Almacena las organizaciones |

Este servicio es **consumido por** el licensing-service, que lo localiza por nombre
a través de Eureka para enriquecer las licencias con datos de organización.