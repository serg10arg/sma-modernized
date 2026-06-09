# eureka-server

Servidor de registro y descubrimiento de servicios basado en Netflix Eureka.
Todos los microservicios del sistema se registran aquí al arrancar y consultan
este servidor para localizar otros servicios sin necesitar IPs o puertos fijos.

## Puerto por defecto
`8761` (configurado vía config-server)

## Ejecución local
```bash
mvn spring-boot:run
```

## Ejecución con Docker
```bash
docker build -t sma/eureka-server .
docker run -p 8761:8761 sma/eureka-server
```

## Endpoints útiles
| Endpoint | Descripción |
|---|---|
| `GET /` | Dashboard web de Eureka |
| `GET /eureka/apps` | Servicios registrados (XML) |
| `GET /actuator/health` | Estado del servidor |
