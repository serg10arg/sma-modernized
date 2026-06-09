# Imagen base con Java 21
FROM eclipse-temurin:21-jre-alpine

# Directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiar el JAR generado por Maven
COPY target/config-server-0.0.1-SNAPSHOT.jar app.jar

# Puerto expuesto por el servicio
EXPOSE 8888

# Comando de arranque
ENTRYPOINT ["java", "-jar", "app.jar"]
