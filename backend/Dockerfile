# =============================================
# FASE 3.2 â€” DOCKERFILE BACKEND SPRING BOOT
# =============================================

# =============================================
# ETAPA 1 â€” BUILD
# =============================================
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copia pom.xml primero para aprovechar el cache de Maven
COPY pom.xml .

# PreparaciÃ³n de dependencias Maven (descarga de dependencias)
RUN mvn dependency:go-offline -B

# Copia el cÃ³digo fuente
COPY src/ ./src/

# CompilaciÃ³n sin ejecutar tests
RUN mvn clean package -DskipTests -B

# =============================================
# ETAPA 2 â€” RUNTIME
# =============================================
FROM eclipse-temurin:21-jre AS runtime

WORKDIR /app

# Copia solo el JAR generado desde la etapa build
COPY --from=build /app/target/*.jar app.jar

# Expone el puerto 8080
EXPOSE 8080

# Comando de arranque
CMD ["java", "-jar", "app.jar"]

