# ── Imagen base con Java 21 (JRE) ──────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copia el JAR generado por Maven
COPY target/eureka-server-0.0.1-SNAPSHOT.jar app.jar

# Puerto del servidor Eureka
EXPOSE 8761

# Healthcheck: verifica que el endpoint de salud de Actuator responde.
# Permite que otros servicios usen condition: service_healthy sobre Eureka.
HEALTHCHECK --interval=10s --timeout=5s --start-period=30s --retries=5 \
  CMD wget --no-verbose --tries=1 --spider \
  http://localhost:8761/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
