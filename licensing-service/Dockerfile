# ── Etapa de construcción ──────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copia el parent pom para resolver dependencias del monorepo
COPY pom.xml .
COPY licensing-service/pom.xml licensing-service/

# Descarga dependencias antes de copiar el código fuente (caché de capas)
RUN mvn dependency:go-offline -pl licensing-service -am -q

# Copia el código fuente y construye el JAR
COPY licensing-service/src licensing-service/src
RUN mvn package -pl licensing-service -am -DskipTests -q

# ── Etapa de ejecución ─────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copia solo el JAR generado en la etapa anterior
COPY --from=build /workspace/licensing-service/target/*.jar app.jar

# Puerto que expone el servicio
EXPOSE 8080

# Healthcheck: verifica cada 10s que el endpoint de salud responde correctamente
HEALTHCHECK --interval=10s --timeout=5s --start-period=30s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider \
  http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
