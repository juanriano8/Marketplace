#!/usr/bin/env bash
# ============================================================================
# Verificacion del proyecto en Linux / macOS
# Equivalente a tools/verify.ps1 (que es solo para Windows)
# ----------------------------------------------------------------------------
# Comprueba: JDK, API viva, endpoints publicos, seguridad (401/403/400),
# conexion a la base de datos y login del administrador.
#
# Uso:
#   ./tools/verify.sh
#   BASE_URL=http://otra-ip:8080 ./tools/verify.sh
#
# No necesita jq ni Python: solo curl, grep y cut.
# ============================================================================
set -uo pipefail

BACKEND_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BASE_URL="${BASE_URL:-http://localhost:8080}"
ENV_FILE="$BACKEND_DIR/.env"
RESP="$(mktemp)"
trap 'rm -f "$RESP"' EXIT

OK=0
FAIL=0
VERDE=$'\033[32m'; ROJO=$'\033[31m'; AMAR=$'\033[33m'; CIAN=$'\033[36m'; GRIS=$'\033[90m'; FIN=$'\033[0m'

paso()  { printf '\n%s=== %s ===%s\n' "$CIAN" "$1" "$FIN"; }
bien()  { OK=$((OK + 1));   printf '  %s[OK]%s    %s\n' "$VERDE" "$FIN" "$1"; }
mal()   { FAIL=$((FAIL + 1)); printf '  %s[FALLO]%s %s\n' "$ROJO" "$FIN" "$1"; }
nota()  { printf '  %s%s%s\n' "$GRIS" "$1" "$FIN"; }

# Campo de texto de un JSON simple, sin depender de jq
json_texto() { grep -o "\"$2\":\"[^\"{]*\"" "$1" 2>/dev/null | head -1 | cut -d'"' -f4; }

# Peticion HTTP -> deja el cuerpo en $RESP y devuelve el codigo
peticion() {
    local metodo="$1" url="$2" token="${3:-}" cuerpo="${4:-}"
    local args=(-s -o "$RESP" -w '%{http_code}' -X "$metodo" "$url" --max-time 25)
    [ -n "$cuerpo" ] && args+=(-H 'Content-Type: application/json' -d "$cuerpo")
    [ -n "$token" ] && args+=(-H "Authorization: Bearer $token")
    curl "${args[@]}" 2>/dev/null
}

# check "nombre" METODO url esperado [token] [cuerpo]
check() {
    local nombre="$1" metodo="$2" url="$3" esperado="$4" token="${5:-}" cuerpo="${6:-}"
    local code
    code="$(peticion "$metodo" "$url" "$token" "$cuerpo")"
    if [ "$code" = "$esperado" ]; then
        bien "$(printf '%-46s %s' "$nombre" "$code")"
    else
        mal "$(printf '%-46s %s (esperaba %s)' "$nombre" "$code" "$esperado")"
        nota "respuesta: $(head -c 160 "$RESP" 2>/dev/null)"
    fi
}

printf '%s\n' "=================================================================="
printf ' VERIFICACION DEL PROYECTO MARKETPLACE (Linux/macOS)\n'
printf '%s\n' "=================================================================="

# ---------------------------------------------------------------- 1. Java
paso '1. Java 21'
if [ -n "${JAVA_HOME:-}" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    bien "JAVA_HOME = $JAVA_HOME"
elif command -v java >/dev/null 2>&1; then
    version="$(java -version 2>&1 | head -1)"
    case "$version" in
        *'"21'*|*'21.'*) bien "java del PATH: $version" ;;
        *) mal "Java encontrado no es la 21: $version" ;;
    esac
else
    mal 'No hay JDK. Instala Java 21 (ver GUIA_OTRO_PC_LINUX.md)'
fi

# ---------------------------------------------------------------- 2. API
paso "2. La API responde en $BASE_URL"
code="$(peticion GET "$BASE_URL/actuator/health")"
if [ "$code" = "200" ]; then
    bien "health -> $(cat "$RESP")"
else
    mal "La API no responde (HTTP $code). Arrancala con: ./run.sh"
    printf '\n%sRESULTADO: la API debe estar en marcha para el resto de comprobaciones.%s\n' "$AMAR" "$FIN"
    exit 1
fi

# ---------------------------------------------------------------- 3. Publicos
paso '3. Endpoints publicos (sin token)'
check 'GET /api/v1/products'              GET  "$BASE_URL/api/v1/products?page=0&size=5"        200
check 'GET /api/v1/reviews/product (404 ok)' GET "$BASE_URL/api/v1/reviews/product/00000000-0000-0000-0000-000000000000" 404

# ---------------------------------------------------------------- 4. Seguridad
paso '4. Seguridad (debe rechazar)'
check 'GET /api/v1/admin/orders sin token'   GET  "$BASE_URL/api/v1/admin/orders"      401
check 'GET /api/v1/cart sin token'           GET  "$BASE_URL/api/v1/cart"              401
check 'GET /api/v1/seller/products sin token' GET "$BASE_URL/api/v1/seller/products"   401
check 'POST register/buyer email invalido'   POST "$BASE_URL/api/v1/auth/register/buyer" 400 '' \
      '{"email":"no-es-email","password":"Password123!"}'

# ---------------------------------------------------------------- 5. Base de datos
paso '5. Conexion a la base de datos'
if [ -f "$ENV_FILE" ]; then
    DB_HOST="$(grep -E '^DB_HOST=' "$ENV_FILE" | head -1 | cut -d= -f2- | tr -d '\r' || true)"
    DB_PORT="$(grep -E '^DB_PORT=' "$ENV_FILE" | head -1 | cut -d= -f2- | tr -d '\r' || true)"
    DB_PASSWORD="$(grep -E '^DB_PASSWORD=' "$ENV_FILE" | head -1 | cut -d= -f2- | tr -d '\r' || true)"
    DB_NAME="$(grep -E '^DB_NAME=' "$ENV_FILE" | head -1 | cut -d= -f2- | tr -d '\r' || true)"
    ADMIN_EMAIL="$(grep -E '^BOOTSTRAP_ADMIN_EMAIL=' "$ENV_FILE" | head -1 | cut -d= -f2- | tr -d '\r' || true)"
    ADMIN_PASS="$(grep -E '^BOOTSTRAP_ADMIN_PASSWORD=' "$ENV_FILE" | head -1 | cut -d= -f2- | tr -d '\r' || true)"
    DB_PORT="${DB_PORT:-5432}"

    if [ -z "$DB_HOST" ]; then
        mal 'No se pudo leer DB_HOST del .env'
    elif (exec 3<>"/dev/tcp/$DB_HOST/$DB_PORT") 2>/dev/null; then
        exec 3<&- 2>/dev/null || true
        bien "TCP a $DB_HOST:$DB_PORT accesible"
    else
        mal "No se alcanza $DB_HOST:$DB_PORT"
        nota 'Revisa que la red 0.0.0.0/0 siga autorizada en Cloud SQL, o que tu red no bloquee el 5432'
    fi

    # Login del administrador: prueba de extremo a extremo (base + BCrypt + JWT)
    if [ -n "$ADMIN_EMAIL" ] && [ -n "$ADMIN_PASS" ]; then
        code="$(peticion POST "$BASE_URL/api/v1/auth/login" '' \
                "{\"email\":\"$ADMIN_EMAIL\",\"password\":\"$ADMIN_PASS\"}")"
        if [ "$code" = "200" ]; then
            bien 'POST /api/v1/auth/login (admin) -> 200'
            TOKEN="$(json_texto "$RESP" accessToken)"
            if [ -n "$TOKEN" ]; then
                check 'GET /api/v1/admin/orders con token ADMIN' GET "$BASE_URL/api/v1/admin/orders" 200 "$TOKEN"
                nota 'Login OK: base de datos, hash BCrypt y JWT funcionando'
            fi
        else
            mal "Login del administrador -> HTTP $code"
            nota 'Si es 401, la contrasena del .env no coincide con la de la base'
        fi
    fi
else
    mal "No existe $ENV_FILE (copia .env.example a .env)"
fi

# ---------------------------------------------------------------- Resumen
printf '\n%s\n' "=================================================================="
if [ "$FAIL" -eq 0 ]; then
    printf '%s TODO CORRECTO - el proyecto esta listo para la demostracion%s\n' "$VERDE" "$FIN"
    printf '%s\n' "=================================================================="
    printf '\n  Panel web : http://localhost:3000\n'
    printf '  Swagger   : %s/swagger-ui.html\n\n' "$BASE_URL"
else
    printf '%s %d COMPROBACION(ES) FALLIDA(S)  (%d correctas)%s\n' "$ROJO" "$FAIL" "$OK" "$FIN"
    printf '%s\n' "=================================================================="
fi

[ "$FAIL" -eq 0 ]
