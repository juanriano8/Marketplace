#!/usr/bin/env bash
# ============================================================================
# Arranca la API leyendo las variables de backend/.env
# Para Linux / macOS / WSL (el equivalente a run.ps1 en Windows)
# ----------------------------------------------------------------------------
# Uso:
#   ./run.sh                -> perfil indicado en .env (cloud por defecto)
#   ./run.sh local          -> fuerza PostgreSQL local (Docker), ignora Cloud SQL
#   ./run.sh test           -> solo compila y ejecuta los tests
#
# Si da "Permission denied", ejecuta:  chmod +x run.sh gradlew
# ============================================================================
set -euo pipefail

BACKEND_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$BACKEND_DIR"

MODE="${1:-run}"
ENV_FILE="$BACKEND_DIR/.env"

# ---- 1. Java 21 ----------------------------------------------------------
# Si JAVA_HOME no apunta a un JDK valido, se busca uno instalado en las rutas
# habituales de Linux (apt, dnf, Adoptium), macOS (brew) y SDKMAN.
if [ -z "${JAVA_HOME:-}" ] || [ ! -x "${JAVA_HOME}/bin/java" ]; then
    for candidate in \
        /usr/libexec/java_home \
        /usr/lib/jvm/*21* \
        /opt/java/*21* \
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
    echo "ERROR: no se encontro un JDK. El proyecto exige Java 21."
    echo "  Debian/Ubuntu : sudo apt install openjdk-21-jdk"
    echo "  Fedora/RHEL   : sudo dnf install java-21-openjdk-devel"
    echo "  Arch          : sudo pacman -S jdk21-openjdk"
    echo "  macOS         : brew install --cask temurin@21"
    echo "  Cualquiera    : SDKMAN -> sdk install java 21.0.5-tem"
    exit 1
fi

# ---- 2. Cargar .env ------------------------------------------------------
# Se eliminan los retornos de carro antes de leerlo: si el archivo se copio
# desde Windows (o por USB), cada valor terminaria en \r y fallarian cosas como
# DB_PASSWORD (contrasena incorrecta) o SPRING_PROFILES_ACTIVE (perfil no valido).
if [ -f "$ENV_FILE" ]; then
    echo "Cargando variables desde $ENV_FILE"
    ENV_LIMPIO="$(mktemp)"
    tr -d '\r' < "$ENV_FILE" > "$ENV_LIMPIO"
    set -a
    # shellcheck disable=SC1090
    . "$ENV_LIMPIO"
    set +a
    rm -f "$ENV_LIMPIO"
else
    echo "AVISO: no existe .env (copia .env.example a .env si vas a usar Cloud SQL)"
fi

# ---- 3. Gradle -----------------------------------------------------------
GRADLE_ARGS=()
[ -n "${JAVA_HOME:-}" ] && GRADLE_ARGS+=("-Dorg.gradle.java.home=$JAVA_HOME")

# El wrapper necesita permiso de ejecucion. Si un clon o una copia desde
# Windows lo perdio, se recupera solo.
if [ ! -x ./gradlew ]; then
    echo "AVISO: ./gradlew no era ejecutable; aplicando chmod +x"
    chmod +x ./gradlew 2>/dev/null || true
fi

GRADLEW="./gradlew"
if [ ! -x ./gradlew ]; then
    # Ultimo recurso: ejecutarlo con bash, que no necesita el bit de ejecucion.
    GRADLEW="bash ./gradlew"
fi

# ---- 4. Ejecutar ---------------------------------------------------------
if [ "$MODE" = "test" ]; then
    echo "Ejecutando tests..."
    # ${arr[@]+"${arr[@]}"} evita el error "unbound variable" con bash 3.2 (macOS)
    exec $GRADLEW ${GRADLE_ARGS[@]+"${GRADLE_ARGS[@]}"} test
fi

if [ "$MODE" = "local" ]; then
    export SPRING_PROFILES_ACTIVE=local
    # El .env apunta a Cloud SQL y las variables de entorno TIENEN PRIORIDAD sobre
    # los valores por defecto de application.yml, asi que hay que sobrescribirlas
    # para usar el PostgreSQL de docker-compose.
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
echo "Arrancando Marketplace API  |  perfil=${SPRING_PROFILES_ACTIVE:-local}  puerto=$PORT"
echo "Swagger UI:  http://localhost:$PORT/swagger-ui.html"
echo "Health:      http://localhost:$PORT/actuator/health"

exec $GRADLEW ${GRADLE_ARGS[@]+"${GRADLE_ARGS[@]}"} bootRun
