#!/usr/bin/env bash
# ============================================================================
# Arranca la API leyendo las variables de backend/.env
# Para Linux / macOS / WSL (el equivalente a run.ps1 en Windows)
# ----------------------------------------------------------------------------
# Uso:
#   ./run.sh                -> perfil indicado en .env (cloud por defecto)
#   ./run.sh local          -> fuerza el perfil local
#   ./run.sh test           -> sólo compila y ejecuta los tests
# ============================================================================
set -euo pipefail

BACKEND_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$BACKEND_DIR"

MODE="${1:-run}"
ENV_FILE="$BACKEND_DIR/.env"

# ---- 1. Java 21 ----------------------------------------------------------
if [ -z "${JAVA_HOME:-}" ] || [ ! -x "${JAVA_HOME}/bin/java" ]; then
    for candidate in \
        /usr/libexec/java_home \
        /usr/lib/jvm/*21* \
        /Library/Java/JavaVirtualMachines/*21*/Contents/Home \
        "$HOME/.sdkman/candidates/java/"*21* ; do
        if [ -x "$candidate/bin/java" ]; then
            export JAVA_HOME="$candidate"
            break
        elif [ "$candidate" = "/usr/libexec/java_home" ] && [ -x "$candidate" ]; then
            detected="$("$candidate" -v 21 2>/dev/null || true)"
            if [ -n "$detected" ]; then
                export JAVA_HOME="$detected"
                break
            fi
        fi
    done
fi

if [ -n "${JAVA_HOME:-}" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    echo "JAVA_HOME = $JAVA_HOME"
else
    echo "ERROR: no se encontró un JDK. El proyecto exige Java 21."
    echo "  macOS : brew install --cask temurin@21"
    echo "  Ubuntu: sudo apt install temurin-21-jdk   (o usa SDKMAN: sdk install java 21-tem)"
    exit 1
fi

# ---- 2. Cargar .env ------------------------------------------------------
if [ -f "$ENV_FILE" ]; then
    echo "Cargando variables desde $ENV_FILE"
    set -a
    # shellcheck disable=SC1090
    source "$ENV_FILE"
    set +a
else
    echo "AVISO: no existe .env (cópialo de .env.example si vas a usar Cloud SQL)"
fi

GRADLE_ARGS=()
[ -n "${JAVA_HOME:-}" ] && GRADLE_ARGS+=("-Dorg.gradle.java.home=$JAVA_HOME")

# ---- 3. Ejecutar ---------------------------------------------------------
if [ "$MODE" = "test" ]; then
    echo "Ejecutando tests..."
    exec ./gradlew "${GRADLE_ARGS[@]}" test
fi

if [ "$MODE" = "local" ]; then
    export SPRING_PROFILES_ACTIVE=local
    # El .env apunta a Cloud SQL y las variables de entorno TIENEN PRIORIDAD sobre los valores por
    # defecto de application.yml, así que hay que sobrescribirlas para el PostgreSQL de docker-compose.
    echo "Perfil local: usando PostgreSQL en localhost (se ignoran los datos de Cloud SQL)"
    export DB_HOST=localhost
    export DB_PORT=5432
    export DB_NAME=marketplace_db
    export DB_USER=postgres
    export DB_PASSWORD=postgres
    export DB_SSLMODE=disable
    export JPA_DDL_AUTO=update
fi

PORT="${PORT:-8080}"
echo "Arrancando Marketplace API  |  perfil=$SPRING_PROFILES_ACTIVE  puerto=$PORT"
echo "Swagger UI:  http://localhost:$PORT/swagger-ui.html"
echo "Health:      http://localhost:$PORT/actuator/health"

exec ./gradlew "${GRADLE_ARGS[@]}" bootRun
