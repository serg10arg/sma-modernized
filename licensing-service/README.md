# licensing-service

## Descripción

Microservicio de gestión de licencias de software del sistema SMA Modernized.
Expone una API REST para consultar licencias asociadas a organizaciones.
En esta etapa inicial opera de forma completamente autónoma sin dependencias
de infraestructura externa; los datos se sirven en memoria.

## Etapa

Introducido en la **Etapa 2** del proyecto como esqueleto REST básico.
Las etapas posteriores añadirán persistencia, integración con Config Server,
registro en Eureka y seguridad OAuth2.

## Responsabilidades

- Exponer el endpoint REST de consulta de licencias por organización
- Devolver la representación JSON de una licencia con sus atributos principales
- Publicar el endpoint de salud `/actuator/health` para verificación operacional
- Actuar como módulo Maven independiente dentro del monorepo `sma-modernized`

## Tecnologías

| Tecnología | Versión | Rol |
|---|---|---|
| Java | 21 | Lenguaje principal |
| Spring Boot | 3.3.x | Framework base del microservicio |
| Spring Web (Tomcat) | 3.3.x | Servidor HTTP embebido y capa REST |
| Spring Boot Actuator | 3.3.x | Endpoints de salud y métricas |
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
# Construir la imagen
docker build -f licensing-service/Dockerfile -t licensing-service:local .

# Arrancar el contenedor
docker run -p 8080:8080 licensing-service:local
```

**Con Docker Compose (desde la raíz del monorepo):**

```bash
docker compose up licensing-service
```

## API

### GET /v1/organization/{organizationId}/license/{licenseId}

Obtiene una licencia específica asociada a una organización.

**Parámetros de ruta:**

| Parámetro | Tipo | Descripción |
|---|---|---|
| `organizationId` | String | Identificador de la organización propietaria |
| `licenseId` | String | Identificador único de la licencia |

**Ejemplo de request:**

```
GET /v1/organization/org-001/license/lic-001
```

**Ejemplo de response (HTTP 200):**

```json
{
  "licenseId": "lic-001",
  "organizationId": "org-001",
  "productName": "O-stock",
  "licenseType": "full",
  "description": "Licencia de software para gestión de activos"
}
```

---

### GET /actuator/health

Verifica el estado operacional del servicio.

**Ejemplo de response (HTTP 200):**

```json
{
  "status": "UP"
}
```

---

### GET /actuator

Lista todos los endpoints de Actuator disponibles.

**Ejemplo de response (HTTP 200):**

```json
{
  "_links": {
    "self": { "href": "http://localhost:8080/actuator" },
    "health": { "href": "http://localhost:8080/actuator/health" },
    "info":   { "href": "http://localhost:8080/actuator/info" },
    "metrics":{ "href": "http://localhost:8080/actuator/metrics" }
  }
}
```

## Decisiones técnicas

| Decisión tomada | Alternativas consideradas | Motivo de la elección |
|---|---|---|
| Java records para el modelo `License` | Lombok `@Data`, clases con getters manuales | Java 21 soporta records de forma nativa; evita dependencias externas y garantiza inmutabilidad sin boilerplate |
| Datos en memoria sin base de datos | H2 embebida, PostgreSQL | Etapa de esqueleto; la persistencia se introduce en una etapa posterior para mantener el foco |
| `application.yml` en lugar de `application.properties` | properties | Mayor legibilidad para configuraciones anidadas; estándar en proyectos Spring Cloud modernos |
| Inyección por constructor en el controlador | `@Autowired` en campo | Mejor para tests unitarios; recomendado por el equipo de Spring desde Spring 4.3 |
| Dockerfile multietapa (build + runtime) | Imagen única con JDK | La imagen final solo contiene el JRE Alpine; reduce el tamaño de imagen significativamente |
| Puerto 8080 | Cualquier otro puerto | Convención Spring Boot; se externalizará vía Config Server en etapas posteriores |

## Dependencias con otros servicios

En esta etapa el servicio no tiene dependencias externas y arranca de forma standalone.

Las siguientes dependencias se incorporarán en etapas futuras:

| Servicio | Etapa | Motivo |
|---|---|---|
| `config-server` | Etapa 3 | Externalización de la configuración |
| `eureka-server` | Etapa 4 | Registro y descubrimiento de servicio |
| `gateway-server` | Etapa 5 | Enrutamiento centralizado de peticiones |
| `organization-service` | Etapa 6 | Resolución del nombre de la organización por ID |