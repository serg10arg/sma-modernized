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
| Resilience4j | 2.x | Patrones de resiliencia (circuit breaker, retry, etc.) |
| Spring Security 6 + OAuth2 | 6.x | Autenticación y autorización |
| Spring Cloud Stream + Kafka | 4.x | Mensajería asíncrona |
| Micrometer Tracing + Zipkin | - | Trazabilidad distribuida |
| Maven | 3.9.x | Construcción (monorepo multi-módulo) |
| Docker + Docker Compose | - | Contenerización y orquestación local |

## Arquitectura del sistema

![Arquitectura del sistema](docs/images/system.png)


## Servicios

| Servicio | Descripción | Puerto |
|---|---|---|
| `config-server` | Configuración centralizada (Spring Cloud Config) | 8071 |
| `eureka-server` | Registro y descubrimiento de servicios | 8761 |
| `gateway-server` | API Gateway y enrutamiento | 8072 |
| `licensing-service` | Gestión de licencias de software | 8080 |
| `organization-service` | Gestión de organizaciones | 8081 |
| `zipkin` | Trazabilidad distribuida | 9411 |

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

**Levantar solo un servicio específico:**

```bash
docker compose up licensing-service
```

**Detener el sistema:**

```bash
docker compose down
```

## Variables de entorno

El archivo `.env` en la raíz del proyecto controla la configuración del sistema.
Cópialo desde `.env.example` y ajusta los valores según tu entorno.

| Variable | Descripción | Valor de ejemplo |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Perfil de Spring activo en todos los servicios | `default` |
| `CONFIG_SERVER_URI` | URL del config-server para los servicios cliente | `http://config-server:8071` |
| `EUREKA_SERVER_URI` | URL del eureka-server para el registro de servicios | `http://eureka-server:8761/eureka` |
| `ZIPKIN_URI` | URL del servidor Zipkin para trazabilidad | `http://zipkin:9411` |
| `DB_HOST` | Host de la base de datos PostgreSQL | `postgres` |
| `DB_PORT` | Puerto de la base de datos | `5432` |
| `DB_USER` | Usuario de la base de datos | `sma_user` |
| `DB_PASSWORD` | Contraseña de la base de datos | `changeme` |

## Estado del proyecto

| Etapa | Descripción | Estado |
|---|---|---|
| Paso 0 | Inicialización del monorepo | ✅ Completada |
| Etapa 1 | `config-server` + `eureka-server` | ✅ Completada |
| Etapa 2 | `licensing-service`: esqueleto REST + i18n + HATEOAS + healthcheck | ✅ Completada |
| Etapa 2b | Docker: Dockerfiles y docker-compose completo con healthchecks | 🔜 Pendiente |
| Etapa 3 | Config Server con Git + PostgreSQL + Spring Data JPA en licensing | 🔜 Pendiente |
| Etapa 4 | `organization-service` + comunicación entre servicios + correlationId | 🔜 Pendiente |
| Etapa 5 | Resilience4j: circuit breaker, retry, bulkhead, rate limiter, fallback | 🔜 Pendiente |
| Etapa 6 | `gateway-server`: rutas, pre-filter y post-filter | 🔜 Pendiente |
| Etapa 7 | Seguridad OAuth2 + Keycloak + JWT | 🔜 Pendiente |
| Etapa 8 | Mensajería asíncrona con Kafka + caché con Redis | 🔜 Pendiente |
| Etapa 9 | Trazabilidad distribuida con Micrometer + Zipkin | 🔜 Pendiente |
| Etapa 10 | Despliegue en cloud (AWS, CI/CD) | ⏸️ Fuera de alcance actual |
