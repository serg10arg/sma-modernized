# sma-modernized

## Descripción del proyecto
Sistema de gestión de licencias y organizaciones construido con arquitectura de
microservicios usando Spring Boot 3 y Spring Cloud. Basado en los patrones del
libro *Microservices in Action* (2nd Edition) y adaptado al stack tecnológico moderno.

## Stack tecnológico

| Tecnología | Versión | Rol en el sistema |
|---|---|---|
| Java | 21 | Lenguaje de desarrollo |
| Spring Boot | 3.3.x | Framework base de cada microservicio |
| Spring Cloud | 2023.x | Infraestructura de microservicios |
| Spring Cloud Gateway | 4.x | API Gateway / punto de entrada |
| Spring Cloud LoadBalancer | 4.x | Balanceo de carga en cliente |
| Spring Cloud Config | 4.x | Configuración centralizada |
| Netflix Eureka | 4.x | Service discovery |
| Resilience4j | 2.x | Circuit breaker, retry, bulkhead |
| Micrometer Tracing + Zipkin | 1.x | Trazabilidad distribuida |
| Spring Security 6 + OAuth2 | 6.x | Seguridad y autorización |
| Maven | 3.9.x | Gestión de dependencias y build |
| Docker + Docker Compose | - | Contenerización y orquestación local |

## Arquitectura del sistema

![Arquitectura del sistema](docs/images/system.png)

## Servicios

| Servicio | Descripción | Puerto |
|---|---|---|
| config-server | Configuración centralizada (Spring Cloud Config) | 8888 |
| eureka-server | Registro y descubrimiento de servicios | 8761 |
| gateway-server | Punto de entrada único, enrutamiento y seguridad | 8072 |
| licensing-service | Gestión de licencias de software | 8080 |
| organization-service | Gestión de organizaciones clientes | 8081 |
| zipkin | Trazabilidad distribuida | 9411 |

## Ejecución del sistema completo

### Prerrequisitos
- Docker y Docker Compose instalados
- Java 21
- Maven 3.9+

### Levantar el sistema
```bash
# Copiar variables de entorno
cp .env.example .env

# Construir todos los módulos
mvn clean package -DskipTests

# Levantar con Docker Compose
docker-compose up -d
```

## Variables de entorno
Consulta el archivo `.env.example` en la raíz del proyecto para ver todas las
variables de entorno necesarias. Copia ese archivo como `.env` y ajusta los valores
según tu entorno.

## Estado del proyecto

| Etapa | Descripción | Estado |
|---|---|---|
| Paso 0 | Inicialización del repositorio | ✅ Completado |
| Etapa 1 | config-server + eureka-server | ⏳ Pendiente |
| Etapa 2 | licensing-service (CRUD básico) | ⏳ Pendiente |
| Etapa 3 | gateway-server | ⏳ Pendiente |
| Etapa 4 | organization-service + comunicación entre servicios | ⏳ Pendiente |
| Etapa 5 | Resilience4j (circuit breaker, retry, bulkhead) | ⏳ Pendiente |
| Etapa 6 | Trazabilidad distribuida (Micrometer + Zipkin) | ⏳ Pendiente |
| Etapa 7 | Seguridad OAuth2 + JWT | ⏳ Pendiente |
| Etapa 8 | Mensajería asíncrona con Kafka | ⏳ Pendiente |
