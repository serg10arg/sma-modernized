# gateway-server

## Descripción

Puerta de enlace (API Gateway) del sistema y único punto de entrada para los
clientes. Enruta las peticiones hacia los servicios de negocio descubiertos en
Eureka, y aplica lógica transversal mediante filtros: genera y propaga el
identificador de correlación de cada petición y lo devuelve al cliente. Es además
el primer eslabón de la traza distribuida.

## Etapa

Introducido en la **Etapa 6**. Ampliado en la **Etapa 9** (trazabilidad
distribuida y unificación del identificador de correlación con el traceId).

## Responsabilidades

- Exponer un único punto de entrada (`8072`) y enrutar hacia `licensing-service`
  y `organization-service`.
- Resolver destinos por descubrimiento (locator de Eureka) y mediante rutas
  manuales con reescritura de path.
- Reenviar la cabecera `Authorization` (Bearer JWT) a los servicios destino, que
  actúan como *resource servers* y validan el token.
- Garantizar que toda petición lleve un `tmx-correlation-id`: si no viene, lo
  genera; si viene, lo respeta.
- Devolver el `tmx-correlation-id` en la respuesta para que el cliente pueda
  correlacionar la transacción (y buscarla en Zipkin).
- Iniciar la traza distribuida: el span del gateway es el span raíz de cada
  transacción exportada a Zipkin.

## Tecnologías

- Java 21, Spring Boot 3.3.x
- Spring Cloud Gateway (reactivo, sobre WebFlux)
- Spring Cloud Netflix Eureka Client (descubrimiento)
- Spring Cloud Config Client (`spring.config.import`)
- Spring Boot Actuator
- Micrometer Tracing (bridge Brave) + Zipkin reporter

## Configuración

La configuración se obtiene del `config-server`. Propiedades relevantes:

| Propiedad | Descripción |
|---|---|
| `server.port` | `8072` |
| `eureka.client.*` | Registro y descubrimiento en el `eureka-server` |
| `spring.cloud.gateway.discovery.locator.enabled` | Habilita el enrutado automático por id de servicio en Eureka |
| `spring.cloud.gateway.routes` | Rutas manuales con reescritura de path (prefijos `/licensing` y `/organization`) |
| `management.tracing.sampling.probability` | `1.0` en dev |
| `management.zipkin.tracing.endpoint` | `http://zipkin:9411/api/v2/spans` |

## Ejecución local

- **Con Maven:** `mvn spring-boot:run -pl gateway-server` (requiere `config-server`,
  `eureka-server` y los servicios destino disponibles).
- **Con Docker:** forma parte de `docker compose up`. Expone el puerto `8072`, que
  es la única entrada pública al sistema.

## API

El gateway no expone endpoints propios de negocio: reenvía a los servicios. Dos
formas de acceso:

| Prefijo | Reescribe a | Uso |
|---|---|---|
| `/licensing/**` | `/v1/organization/**` en `licensing-service` | Operaciones de licencias |
| `/organization/**` | `/v1/organization/**` en `organization-service` | Operaciones de organización |
| `/{service-id}/**` (locator) | Ruta original del servicio | Acceso por descubrimiento, p. ej. `/organization-service/v1/organization` |

Ambos servicios comparten el espacio de nombres `/v1/organization/...`, por lo que
los prefijos `/licensing` y `/organization` se usan para desambiguar antes de la
reescritura.

> **Limitación conocida:** la ruta de colección con barra final (`/organization/`)
> tiene una limitación de enrutado. Para crear y listar organizaciones se utiliza
> la ruta de descubrimiento `/organization-service/v1/organization`.

Toda respuesta incluye la cabecera `tmx-correlation-id` con el identificador de
traza (formato traceId), que coincide con el de Zipkin.

## Decisiones técnicas

| Decisión tomada | Alternativas consideradas | Motivo de la elección |
|---|---|---|
| `tmx-correlation-id` = traceId de Micrometer | UUID propio independiente | Un único identificador de correlación coherente en todo el sistema (cabecera, eventos, Zipkin) |
| `Hooks.enableAutomaticContextPropagation()` en el arranque | Leer el contexto a mano con `Mono.deferContextual` | En WebFlux el contexto de traza vive en el Reactor Context; el hook restaura el `ThreadLocal` para que `Tracer.currentSpan()` funcione dentro de los filtros reactivos |
| Lógica del pre-filter envuelta en `Mono.defer(...)` | Ejecutarla en el cuerpo síncrono de `filter()` | Asegura que la lectura del traceId ocurra dentro de un operador, donde el contexto ya está restaurado |
| Pre-filter (genera/propaga) + post-filter (devuelve al cliente) | Un único filtro | Separa la responsabilidad de propagar hacia abajo de la de devolver hacia el cliente |
| Locator de descubrimiento + rutas manuales | Solo rutas manuales | El locator simplifica el acceso por id de servicio; las rutas manuales dan prefijos amigables y desambiguación |
| Validación del JWT delegada en los servicios destino | Validar en el gateway | Cada servicio de negocio es *resource server* y autoriza por rol; el gateway solo reenvía el token |

## Dependencias con otros servicios

- `config-server` — configuración centralizada.
- `eureka-server` — descubrimiento de los servicios destino.
- `licensing-service` y `organization-service` — destinos del enrutado.
- Keycloak — emisor de los tokens que el gateway reenvía (no los valida).
- Zipkin — recepción de los spans de traza.
