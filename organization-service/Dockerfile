# ── Etapa de construcción ──────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
# Los pom de los módulos hermanos son necesarios para que el reactor de Maven
# resuelva el agregador (el parent los declara en <modules>). No se compilan:
# con -pl organization-service -am solo se construye este servicio y el parent.
COPY config-server/pom.xml config-server/
COPY eureka-server/pom.xml eureka-server/
COPY licensing-service/pom.xml licensing-service/
COPY organization-service/pom.xml organization-service/

RUN mvn dependency:go-offline -pl organization-service -am -q

COPY organization-service/src organization-service/src
RUN mvn package -pl organization-service -am -DskipTests -q

# ── Etapa de ejecución ─────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /workspace/organization-service/target/*.jar app.jar

EXPOSE 8081

HEALTHCHECK --interval=10s --timeout=5s --start-period=30s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider \
  http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
