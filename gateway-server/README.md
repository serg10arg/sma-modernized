# gateway-server

## Descripción
Punto de entrada único del sistema. Enruta las peticiones externas hacia los
servicios de negocio (organization-service y licensing-service), resolviéndolos
dinámicamente vía Eureka con balanceo de carga, y añade una capa de trazabilidad
mediante un identificador de correlación.

## Etapa
Introducido en la Etapa 6. A partir de esta etapa todo el tráfico externo entra
por el gateway; los servicios de negocio dejan de exponerse directamente.

## Responsabilidades
- Enrutado dinámico hacia servicios registrados en Eureka mediante `lb://`.
- Dos rutas manuales con prefijos desambiguadores (`/organization`, `/licensing`)
  que reescriben al namespace interno compartido `/v1/organization`.
- Ruta automática por *discovery locator* (`/<service-id>/**`) para acceso
  directo por nombre de servicio.
- Generación del identificador `tmx-correlation-id` en un pre-filtro global
  (`TrackingFilter`) cuando la petición entrante no lo trae.
- Devolución del mismo identificador en la respuesta mediante un post-filtro
  global (`ResponseFilter`).

## Tecnologías
- Spring Cloud Gateway (reactivo, sobre WebFlux/Netty)
- Spring Cloud LoadBalancer (resolución `lb://` y balanceo)
- Eureka Client (descubrimiento de servicios)
- Spring Boot Actuator (endpoint `gateway` para inspección de rutas)

## Configuración
| Propiedad | Valor | Rol |
|---|---|---|
| `server.port` | `8072` | Puerto público del gateway |
| `EUREKA_SERVER_URI` | `http://localhost:8761/eureka/` (por defecto) | Localización de Eureka |
| `eureka.instance.prefer-ip-address` | `true` | Registro por IP (entornos Docker) |
| `spring.cloud.gateway.discovery.locator.enabled` | `true` | Rutas automáticas por service-id |
| `management.endpoint.gateway.enabled` | `true` | Habilita el endpoint Actuator del gateway |

Las dos rutas manuales reescriben al namespace compartido. El `\` escapa el `$`
para que Spring no interprete `${path}` como property placeholder:

```yaml
routes:
  - id: organization-service
    uri: lb://organization-service
    predicates:
      - Path=/organization/**
    filters:
      - RewritePath=/organization/(?<path>.*), /v1/organization/$\{path}
  - id: licensing-service
    uri: lb://licensing-service
    predicates:
      - Path=/licensing/**
    filters:
      - RewritePath=/licensing/(?<path>.*), /v1/organization/$\{path}
```

## Ejecución local
Requiere `eureka-server` y `config-server` arriba, y los servicios de negocio
registrados para poder enrutar.

- Con Maven: `mvn spring-boot:run -pl gateway-server`
- Con Docker: `docker compose up -d gateway-server` (o el sistema completo)

## API
El gateway no define endpoints de negocio propios: expone los de los servicios a
través de sus rutas. Ambos controladores viven bajo `/v1/organization`; los
prefijos del gateway desambiguan el destino.

### Entry points (rutas manuales)
| Método | Ruta en el gateway | Reescritura interna | Servicio |
|---|---|---|---|
| GET | `/organization/{organizationId}` | `/v1/organization/{organizationId}` | organization-service |
| GET | `/licensing/{organizationId}/license` | `/v1/organization/{organizationId}/license` | licensing-service |
| GET | `/licensing/{organizationId}/license/{licenseId}` | `/v1/organization/{organizationId}/license/{licenseId}` | licensing-service |
| POST | `/licensing/{organizationId}/license` | `/v1/organization/{organizationId}/license` | licensing-service |
| PUT | `/licensing/{organizationId}/license` | `/v1/organization/{organizationId}/license` | licensing-service |
| DELETE | `/licensing/{organizationId}/license/{licenseId}` | `/v1/organization/{organizationId}/license/{licenseId}` | licensing-service |

> Punto de atención: las operaciones sobre la raíz de colección de
> organizaciones (listar/crear) producen una reescritura a `/v1/organization/`
> con barra final. Spring 6 no hace *trailing-slash matching* por defecto, por lo
> que conviene acceder a esas operaciones por la ruta de *discovery locator*
> (`/organization-service/v1/organization`) o ajustar el patrón de ruta.

### Rutas automáticas (discovery locator)
`/<service-id>/**` reenvía al servicio quitando el prefijo de service-id:
- `/organization-service/v1/organization`
- `/licensing-service/v1/organization/{organizationId}/license`

### Ejemplo — obtener una licencia a través del gateway
El esquema completo del recurso `License` está documentado en
`licensing-service/README.md`. El controlador añade un bloque `_links` HATEOAS y
el gateway devuelve la cabecera de correlación.

```powershell
$resp = Invoke-RestMethod -Uri "http://localhost:8072/licensing/org-123/license/lic-456" `
  -Headers @{ "Accept-Language" = "es" } -ResponseHeadersVariable headers
$resp
$headers["tmx-correlation-id"]
```

Bloque `_links` que añade el controlador a la respuesta:
```json
{
  "_links": {
    "self":          { "href": "http://.../v1/organization/org-123/license/lic-456" },
    "createLicense": { "href": "http://.../v1/organization/org-123/license" },
    "updateLicense": { "href": "http://.../v1/organization/org-123/license" },
    "deleteLicense": { "href": "http://.../v1/organization/org-123/license/lic-456" }
  }
}
```

### Ejemplo — obtener una organización a través del gateway
Esquema del recurso `Organization` en `organization-service/README.md`.
```powershell
Invoke-RestMethod -Uri "http://localhost:8072/organization/org-123"
```

## Decisiones técnicas
| Decisión | Alternativas consideradas | Motivo |
|---|---|---|
| Spring Cloud Gateway reactivo | Zuul 1 (bloqueante, descontinuado); SCG MVC | Opción soportada y de mayor rendimiento sobre Netty; Zuul 1 está fuera de mantenimiento |
| Discovery locator + 2 rutas manuales | Solo locator; solo rutas manuales | El locator da acceso directo por service-id; las rutas manuales aportan URLs limpias y desambiguan el namespace compartido |
| Prefijos `/organization` y `/licensing` con `RewritePath` | Cambiar paths internos por servicio; un único prefijo | Ambos controladores comparten `/v1/organization`; los prefijos distinguen destino sin tocar el código de los servicios |
| Filtros globales pre/post (`TrackingFilter`/`ResponseFilter`) | `GatewayFilter` por ruta; sin correlación en el borde | La correlación debe cubrir todo el tráfico entrante; un filtro global garantiza cobertura uniforme |
| Endpoint Actuator `gateway` habilitado explícitamente | Dejarlo deshabilitado | Permite inspeccionar rutas en runtime; viene deshabilitado por endurecimiento de seguridad y se habilita de forma consciente |

## Dependencias con otros servicios
- **eureka-server**: imprescindible para resolver `lb://` y el discovery locator.
- **config-server**: provee la configuración centralizada del gateway.
- **organization-service** y **licensing-service**: destinos del enrutado.