# Fleet Billing — Dockerfile (Phase 1)
#
# This is a two-stage build:
#
# STAGE 1 — BUILD:  Uses a full JDK image to compile the project with Maven.
#                   The Maven cache is stored in /root/.m2 so repeated builds
#                   are faster (especially in CI/CD pipelines).
#
# STAGE 2 — RUNTIME: Uses a smaller JRE image (no compiler tools) to run the app.
#                    Only the compiled JAR from Stage 1 is copied here.
#                    This keeps the final image small and secure.
#
# Usage:
#   docker build -t fleet-billing:latest .
#   docker run -p 8080:8080 \
#     -e DB_URL=jdbc:mysql://host.docker.internal:3306/fleet_billing \
#     -e DB_USERNAME=root \
#     -e DB_PASSWORD=yourpassword \
#     fleet-billing:latest

# ─────────────────────────────────────────────────────────────────────────────
# STAGE 1: Build
# ─────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Copy Maven wrapper and pom.xml first (layer caching: dependencies are
# downloaded only when pom.xml changes, not on every source code change)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies without building (cached layer)
RUN ./mvnw dependency:go-offline -B

# Copy source code and build the JAR (skip tests in Docker build;
# tests should run separately in the CI pipeline)
COPY src ./src
RUN ./mvnw package -DskipTests -B

# ─────────────────────────────────────────────────────────────────────────────
# STAGE 2: Runtime
# ─────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

# Create a non-root user for security best practices
RUN addgroup -S fleetbilling && adduser -S fleetbilling -G fleetbilling

WORKDIR /app

# Copy only the compiled JAR from the build stage
COPY --from=build /app/target/fleet-billing-*.jar app.jar

# Change ownership to the non-root user
RUN chown fleetbilling:fleetbilling app.jar

USER fleetbilling

# Expose the application port
EXPOSE 8080

# Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]
