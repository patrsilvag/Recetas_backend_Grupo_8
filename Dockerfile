# 1. Imagen base con Java 17 (o la versión que uses en tu pom.xml)
FROM eclipse-temurin:17-jdk-alpine

# 2. Directorio de trabajo dentro del contenedor
WORKDIR /app

# 3. Copiar el archivo JAR generado (ajusta el nombre si es necesario)
# El asterisco ayuda si el nombre tiene la versión (ej: backend-0.0.1-SNAPSHOT.jar)
COPY target/*.jar app.jar

# 4. Crear carpeta para recibir el Wallet de Oracle Cloud
RUN mkdir -p /app/wallet

# 5. Exponer el puerto 8081 (Semana 2)
EXPOSE 8081

# 6. Comando para iniciar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]