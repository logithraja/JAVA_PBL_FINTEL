# Stage 1: Build the Spring Boot application
FROM maven:3.9.9-eclipse-temurin-17-alpine AS builder
WORKDIR /workspace

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy sources and build package
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Minimal, secure JRE runtime
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Install curl for container health check
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd -r fintel && useradd -r -g fintel fintel

# Copy compiled jar from builder
COPY --from=builder /workspace/target/*.jar /app/fintel-server.jar
RUN chown -R fintel:fintel /app

USER fintel

EXPOSE 8080

HEALTHCHECK --interval=20s --timeout=5s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/fintel-server.jar"]
