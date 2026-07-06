# Stage 1: Build stage using Maven and Eclipse Temurin JDK 21
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy sources and package
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Hardened runtime stage using Eclipse Temurin JRE 21 Alpine
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Enforce security best practices: run as a non-root system user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy built artifact from build stage
COPY --from=build /app/target/decisionhub-backend-1.0.0-SNAPSHOT.jar app.jar

# Expose HTTP port
EXPOSE 8080

# Configure JVM parameters for memory management
ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:+UseContainerSupport", "-jar", "app.jar"]
