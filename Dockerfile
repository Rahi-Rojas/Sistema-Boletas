# Etapa 1: Construcción (Maven)
FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución (Cambiamos la imagen base aquí)
FROM eclipse-temurin:17-jdk-alpine
COPY --from=build /target/productos-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]