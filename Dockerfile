# Etapa 1: Construcción (Maven)
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
# Usamos -DskipTests para que la construcción sea rápida en el VPS
RUN mvn clean package -DskipTests
# Etapa 2: Ejecución
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
# El comodín *.jar ayuda por si el nombre cambia ligeramente
COPY --from=build /app/target/*.jar app.jar
# Crear la carpeta de imágenes dentro del contenedor
RUN mkdir -p /app/uploads
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]