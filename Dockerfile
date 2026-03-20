# ETAPA 1: Compilación (Usa Java 21 para generar el JAR)
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# ETAPA 2: Ejecución (Imagen ligera para la VM de Azure)
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
# Copiamos el JAR desde la etapa de compilación
COPY --from=build /app/target/*.jar app.jar
RUN mkdir -p /app/wallet
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]