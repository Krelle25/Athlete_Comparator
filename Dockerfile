# === Build stage ===
FROM maven:3.9.9-eclipse-temurin-21 AS build

# Arbejdsmappen inde i containeren
WORKDIR /app

# 1) Copy pom først for bedre cache
COPY pom.xml .

# Hent dependencies (så det kan caches lag-mæssigt)
RUN mvn -B dependency:go-offline

# 2) Copy resten af koden ind
COPY src ./src

# Byg jar-fil (uden tests for at gøre build hurtigere)
RUN mvn -B clean package -DskipTests


# === Runtime stage ===
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Kopiér jar fra build-staget
COPY --from=build /app/target/*.jar app.jar

# Spring Boot kører som default på 8080
EXPOSE 8080

# Start app'en
ENTRYPOINT ["java", "-jar", "app.jar"]