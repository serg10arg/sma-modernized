# Imagen base con Java 21
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/eureka-server-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "app.jar"]
