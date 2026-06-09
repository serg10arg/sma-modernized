# config-server

## Descripción
Servidor de configuración centralizada del sistema sma-modernized. Actúa como
fuente única de verdad para las propiedades de todos los microservicios del sistema.
Cada servicio obtiene su configuración al arrancar consultando este servidor,
sin necesidad de llevar propiedades embebidas en su propio artefacto.

## Etapa
Introducido en la **Etapa 1** del proyecto.

## Responsabilidades
- Servir la configuración externalizada de cada microservicio del sistema
- Permitir cambios de configuración sin necesidad de recompilar ni redesplegar servicios
- Proveer un endpoint de salud (`/actuator/health`) para que Docker Compose
  verifique su disponibilidad antes de arrancar los servicios dependientes

## Tecnologías
- Java 21
- Spring Boot 3.3.x
- Spring Cloud Config Server 4.x
- Spring Boot Actuator

## Configuración

| Propiedad | Valor | Descripción |
|---|---|---|
| `server.port` | `8888` | Puerto en el que escucha el servidor |
| `spring.profiles.active` | `native` | Modo de almacenamiento: filesystem local |
| `spring.cloud.config.server.native.search-locations` | `classpath:/config` | Directorio donde se almacenan las configuraciones |

### Archivos de configuración por servicio
Los archivos se encuentran en `src/main/resources/config/`:

| Archivo | Servicio que lo consume |
|---|---|
| `eureka-server.yml` | eureka-server |
| `licensing-service.yml` | licensing-service |
| `organization-service.yml` | organization-service |

## Ejecución local

### Con Maven
```bash
mvn spring-boot:run -pl config-server
```

### Con Docker
```bash
# Desde la raíz del repositorio
docker-compose up config-server
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

### GET `/{application}/{profile}`
Devuelve la configuración de un servicio para un perfil dado.

**Ejemplo:** `GET /eureka-server/default`

**Response:**
```json
{
  "name": "eureka-server",
  "profiles": ["default"],
  "propertySources": [
    {
      "name": "classpath:/config/eureka-server.yml",
      "source": {
        "server.port": 8761,
        "eureka.client.register-with-eureka": false,
        "eureka.client.fetch-registry": false
      }
    }
  ]
}
```

## Decisiones técnicas

| Decisión tomada | Alternativas consideradas | Motivo de la elección |
|---|---|---|
| Perfil `native` (filesystem local) | Git remoto, HashiCorp Vault, Consul | Elimina dependencias externas en etapas iniciales; Git remoto se incorporará en etapas posteriores |
| Configuraciones en `classpath:/config` | Directorio externo del sistema de archivos | Se incluye en el JAR, simplifica el arranque sin montar volúmenes adicionales |
| Healthcheck vía `/actuator/health` | TCP port check | Garantiza que el servidor está listo para servir configuración, no solo que el puerto está abierto |

## Dependencias con otros servicios
El `config-server` no depende de ningún otro servicio del sistema.
Es el primer servicio que debe arrancar.