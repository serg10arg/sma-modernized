# sma-modernized

## Descripción del proyecto

Sistema de gestión de licencias y organizaciones construido con arquitectura
de microservicios usando Spring Boot y Spring Cloud.

El proyecto moderniza una aplicación monolítica de gestión de activos de software
(O-stock) descomponiéndola en servicios independientes, desplegables de forma
autónoma y diseñados siguiendo los principios cloud-native y la metodología
twelve-factor app. Se construye de forma incremental por etapas con el stack
tecnológico moderno de Spring.

## Stack tecnológico

| Tecnología | Versión | Rol dentro del sistema |
|---|---|---|
| Java | 21 | Lenguaje principal |
| Spring Boot | 3.3.x | Framework base de todos los servicios |
| Spring Cloud | 2023.x | Suite de patrones cloud-native |
| Spring Cloud Config | 4.x | Configuración centralizada |
| Spring Cloud Netflix Eureka | 4.x | Registro y descubrimiento de servicios |
| Spring Cloud Gateway | 4.x | Enrutamiento centralizado (API Gateway) |
| Spring Cloud LoadBalancer | 4.x | Balanceo de carga del lado cliente |
| Spring Data JPA | 3.3.x | Acceso a datos y mapeo objeto-relacional |
| PostgreSQL | 16 | Base de datos relacional |
| Resilience4j | 2.2.0 | Tolerancia a fallos: circuit breaker, retry, bulkhead, rate limiter y fallback |
| Keycloak | 26.x | Servidor de identidad (IdP): emite y firma los JWT |
| Spring Security 6 (OAuth2 Resource Server) | (gestionado por Spring Boot) | Validación de JWT y autorización por rol en los servicios de negocio |
| Spring Security 6 (OAuth2 Client) | (gestionado por Spring Boot) | Token de máquina (client_credentials) para la llamada interna entre servicios |
| Spring Cloud Stream + Kafka | 4.x | Mensajería asíncrona |
| Micrometer Tracing + Zipkin | - | Trazabilidad distribuida |
| Maven | 3.9.x | Construcción (monorepo multi-módulo) |
| Docker + Docker Compose | - | Contenerización y orquestación local |

## Arquitectura del sistema

Todo el tráfico externo entra por el **gateway-server** (8072), que enruta hacia los
servicios de negocio resueltos vía Eureka. **Keycloak** actúa como servidor de identidad:
emite los JWT que `organization-service` y `licensing-service` validan como *resource
servers*. La comunicación interna `licensing → organization` viaja autenticada con un
token de máquina (client_credentials).

![Arquitectura del sistema](docs/images/system.png)

## Servicios

| Servicio | Descripción | Puerto |
|---|---|---|
| config-server | Configuración centralizada | 8888 |
| eureka-server | Descubrimiento de servicios | 8761 |
| keycloak | Servidor de identidad (IdP), emite JWT | 8080 |
| gateway-server | Punto de entrada único y enrutado | 8072 |
| organization-service | Gestión de organizaciones (resource server) | Registrado en Eureka (sin puerto público) |
| licensing-service | Gestión de licencias (resource server) | Registrado en Eureka (sin puerto público) |
| zipkin | Trazabilidad distribuida | 9411 |

## Seguridad

El sistema usa OAuth2 sobre tokens JWT:

- **Keycloak** (realm `sma`) emite y firma los access tokens. Roles de realm: `USER` y `ADMIN`.
- **Resource servers**: `organization-service` y `licensing-service` validan el JWT por
  `issuer-uri` (firma, emisor y expiración) y autorizan por rol con `@PreAuthorize`.
  Los roles de Keycloak (`realm_access.roles`) se mapean a authorities `ROLE_*`.
- **Comunicación interna**: la llamada `licensing → organization` se autentica con el
  flujo `client_credentials` del cliente `sma-internal`; el token se inyecta en la
  petición saliente y se cachea hasta su expiración.
- **Gateway**: reenvía la cabecera `Authorization` hacia los servicios.

Reglas de autorización por defecto: lecturas (GET) requieren `USER`; escrituras
(POST/PUT/DELETE) requieren `ADMIN`.

## Ejecución del sistema completo

**Prerequisitos:**

- Docker y Docker Compose instalados
- Git
- (Opcional para desarrollo) Java 21 y Maven 3.9.x

**Pasos:**

```bash
# 1. Clonar el repositorio
git clone https://github.com/<tu-usuario>/sma-modernized.git
cd sma-modernized

# 2. Crear el archivo de variables de entorno
cp .env.example .env
# Editar .env con los valores apropiados para tu entorno

# 3. Levantar todo el sistema
docker compose up --build

# 4. Verificar que los servicios están en pie
docker compose ps
```

**Verificar el registro de servicios** en el dashboard de Eureka:
`http://localhost:8761`

**Detener el sistema:**

```bash
docker compose down
```

> Nota: la primera vez que se levanta el sistema, el contenedor de PostgreSQL crea
> las dos bases de datos mediante un script de inicialización. Si se necesita
> recrear ese script (por ejemplo, tras cambiarlo), hay que recrear el volumen con
> `docker compose down -v`, lo que borra los datos almacenados.

## Variables de entorno

El archivo `.env` en la raíz controla la configuración del sistema. Cópialo desde
`.env.example` y ajusta los valores.

| Variable | Descripción | Valor de ejemplo |
|---|---|---|
| `CONFIG_SERVER_URI` | URL del config-server para los servicios cliente | `http://config-server:8888` |
| `EUREKA_SERVER_URI` | URL del eureka-server para el registro de servicios | `http://eureka-server:8761/eureka/` |
| `ZIPKIN_URI` | URL del servidor Zipkin para trazabilidad | `http://zipkin:9411` |
| `DB_HOST` | Host de PostgreSQL | `postgres` |
| `DB_PORT` | Puerto de PostgreSQL | `5432` |
| `DB_NAME` | Base de datos del licensing-service | `sma_licensing` |
| `DB_USER` | Usuario de la base de datos | `sma_user` |
| `DB_PASSWORD` | Contraseña de la base de datos | `sma_password` |
| `ORG_DB_NAME` | Base de datos del organization-service | `sma_organization` |
| `ORG_DB_USER` | Usuario de la base de datos de organización | `sma_user` |
| `ORG_DB_PASSWORD` | Contraseña de la base de datos de organización | `sma_password` |

## Estado del proyecto

| Etapa | Descripción | Estado |
|---|---|---|
| 0 | Esqueleto monorepo | Completa |
| 1 | config-server + eureka-server | Completa |
| 2 | licensing-service (REST, i18n, HATEOAS) + Docker | Completa |
| 3 | Persistencia PostgreSQL (licensing-service) | Completa |
| 4 | organization-service + comunicación entre servicios | Completa |
| 5 | Resiliencia (Resilience4j) en licensing-service | Completa |
| 6 | gateway-server (enrutado + correlación) | Completa |
| 7 | Seguridad (OAuth2 + JWT con Keycloak) | Completa |
| 8 | Mensajería asíncrona (Kafka / Spring Cloud Stream) | Siguiente |
| 9 | Trazabilidad distribuida (Micrometer + Zipkin) | Pendiente |
