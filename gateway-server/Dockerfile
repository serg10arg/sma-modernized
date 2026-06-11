# ── Etapa de construcción ──────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copia el parent pom para resolver dependencias del monorepo
COPY pom.xml .
# Los pom de los módulos hermanos son necesarios para que el reactor de Maven
# resuelva el agregador (el parent los declara en <modules>). No se compilan:
# con -pl gateway-server -am solo se construye este servicio y el parent.
COPY config-server/pom.xml config-server/
COPY eureka-server/pom.xml eureka-server/
COPY gateway-server/pom.xml gateway-server/
COPY licensing-service/pom.xml licensing-service/
COPY organization-service/pom.xml organization-service/

# Descarga dependencias antes de copiar el código fuente (caché de capas)
RUN mvn dependency:go-offline -pl gateway-server -am -q

# Copia el código fuente y construye el JAR
COPY gateway-server/src gateway-server/src
RUN mvn package -pl gateway-server -am -DskipTests -q

# ── Etapa de ejecución ─────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copia solo el JAR generado en la etapa anterior
COPY --from=build /workspace/gateway-server/target/*.jar app.jar

# Puerto que expone el gateway
EXPOSE 8072

# Healthcheck: verifica cada 10s que el endpoint de salud responde correctamente
HEALTHCHECK --interval=10s --timeout=5s --start-period=30s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider \
  http://localhost:8072/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
