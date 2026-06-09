# eureka-server

## Descripción
Servidor de registro y descubrimiento de servicios del sistema sma-modernized.
Permite que los microservicios se registren al arrancar y se localicen entre sí
mediante un nombre lógico, sin necesidad de conocer IPs ni puertos fijos.

## Etapa
Introducido en la **Etapa 1** del proyecto.

## Responsabilidades
- Mantener el registro de todas las instancias de microservicios activas en el sistema
- Permitir que los servicios clientes descubran instancias disponibles por nombre lógico
- Exponer el dashboard web de Eureka para monitoreo visual del estado del sistema
- Obtener su propia configuración desde el `config-server` al arrancar

## Tecnologías
- Java 21
- Spring Boot 3.3.x
- Spring Cloud Netflix Eureka Server 4.x
- Spring Cloud Config Client 4.x
- Spring Boot Actuator

## Configuración
El `eureka-server` obtiene toda su configuración desde el `config-server`.
Las propiedades relevantes están en `config-server/src/main/resources/config/eureka-server.yml`:

| Propiedad | Valor | Descripción |
|---|---|---|
| `server.port` | `8761` | Puerto en el que escucha el servidor |
| `eureka.client.register-with-eureka` | `false` | El servidor no se registra en sí mismo |
| `eureka.client.fetch-registry` | `false` | El servidor no consulta su propio registro |
| `eureka.server.wait-time-in-ms-when-sync-empty` | `5` | Reduce el tiempo de espera inicial en desarrollo |

### Variable de entorno en Docker
| Variable | Valor en Docker Compose | Descripción |
|---|---|---|
| `SPRING_CONFIG_IMPORT` | `configserver:http://config-server:8888` | URL del config-server dentro de la red Docker |

## Ejecución local

### Prerrequisito
El `config-server` debe estar corriendo en `http://localhost:8888` antes de arrancar el `eureka-server`.

### Con Maven
```bash
# Terminal 1
mvn spring-boot:run -pl config-server

# Terminal 2
mvn spring-boot:run -pl eureka-server
```

### Con Docker
```bash
# Desde la raíz del repositorio
docker-compose up config-server eureka-server
```

## API

### GET `/actuator/health`
Verifica el estado del servidor.

**Response:**
```json
{
  "status": "UP"
}
```

---

### GET `/`
Dashboard web de Eureka. Muestra todas las instancias registradas en el sistema.
Acceder desde el navegador: `http://localhost:8761`

---

### GET `/eureka/apps`
Lista todas las aplicaciones registradas en formato XML/JSON.

**Ejemplo de response con un servicio registrado:**
```json
{
  "applications": {
    "application": [
      {
        "name": "LICENSING-SERVICE",
        "instance": [
          {
            "hostName": "172.18.0.4",
            "port": { "$": 8080, "@enabled": "true" },
            "status": "UP"
          }
        ]
      }
    ]
  }
}
```

## Decisiones técnicas

| Decisión tomada | Alternativas consideradas | Motivo de la elección |
|---|---|---|
| Eureka standalone (nodo único) | Eureka en par de alta disponibilidad, Consul, Zookeeper | Suficiente para desarrollo local; la comunidad Java mantiene Eureka como estándar de facto con Spring Cloud |
| Configuración delegada al `config-server` | Propiedades locales en `application.yml` | Consistente con el principio twelve-factor de configuración externalizada; todos los servicios siguen el mismo patrón |
| `register-with-eureka: false` | Dejar el valor por defecto (`true`) | Evita que el servidor se registre en sí mismo, lo que genera advertencias de error en los logs al no encontrar peers |

## Dependencias con otros servicios
- **config-server** — debe estar disponible en el arranque para obtener la configuración