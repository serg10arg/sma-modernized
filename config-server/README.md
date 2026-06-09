# config-server

Servidor de configuración centralizada basado en Spring Cloud Config.
Sirve los archivos de configuración ubicados en `src/main/resources/config/`
a todos los microservicios del sistema mediante el perfil `native`.

## Puerto por defecto
`8888` (configurable vía variable de entorno `CONFIG_SERVER_PORT`)

## Ejecución local
```bash
mvn spring-boot:run
```

## Ejecución con Docker
```bash
docker build -t sma/config-server .
docker run -p 8888:8888 sma/config-server
```

## Endpoints útiles
| Endpoint | Descripción |
|---|---|
| `GET /{application}/{profile}` | Configuración de un servicio |
| `GET /actuator/health` | Estado del servidor |
