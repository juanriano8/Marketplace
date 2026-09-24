# ============================================================================
# Imagen de la API Marketplace (Spring Boot 3 + Java 21)
# Construcción multi-etapa: compila con el Gradle wrapper y ejecuta en un JRE
# mínimo, que es el formato que espera Cloud Run.
# ============================================================================

# ---------- Etapa 1: compilación ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Se copian primero los ficheros de Gradle para aprovechar la caché de capas
COPY gradlew ./
COPY gradle ./gradle
COPY settings.gradle build.gradle ./
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies --quiet || true

COPY src ./src
RUN ./gradlew --no-daemon clean bootJar -x test

# ---------- Etapa 2: ejecución ----------
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Usuario sin privilegios
RUN groupadd --system marketplace && useradd --system --gid marketplace marketplace
USER marketplace

COPY --from=build /workspace/build/libs/*.jar app.jar

# Cloud Run inyecta PORT (por defecto 8080)
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseG1GC", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
