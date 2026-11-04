# Requisitos de seguridad para el frontend (NotebookLM)

**Fuente:** NotebookLM · *"Guía OWASP de Seguridad en Aplicaciones Web"*
**ID:** `9bb773f4-7b01-4be5-a382-7d3990d848b0`
**Extraído:** 2026-11-03 · consulta vía `notebooklm ask`

> Este documento resume la respuesta del notebook y sirve de fuente para el
> rediseño del frontend y para los prompts de Stitch. No sustituye a
> `DESIGN.md` (tokens visuales); esto es la capa de **seguridad y UX segura**.

## Resumen del notebook

Las fuentes (OWASP Cheat Sheets de CSP, XSS y DOM XSS, OWASP Top 10 y la guía de
Secure Coding) insisten en **defensa en profundidad**: los frameworks modernos
ayudan, pero no bastan. Hay que combinar **CSP estricta** con **codificación de
salida según el contexto** y **sanitización** (DOMPurify) en el punto donde se
renderiza el dato no confiable. Las CSP genéricas y los interceptores de
servidor no cubren los casos DOM-based.

---

## 1. Content Security Policy (CSP)

- **Entrega por cabecera HTTP** `Content-Security-Policy`, no con `<meta>`
  (la etiqueta no habilita todas las directivas, p. ej. `frame-ancestors`).
- **CSP estricta** basada en *nonces* por respuesta
  (`script-src 'nonce-{RANDOM}' 'strict-dynamic'`) o en *hashes*
  (`script-src 'sha256-...'`). Evitar `'unsafe-inline'` y `'unsafe-eval'`.
- Directivas de alto riesgo:
  - `object-src 'none'`
  - `base-uri 'none'`
  - `frame-ancestors 'none'` (o `'self'`) contra clickjacking
- **Trusted Types**: `require-trusted-types-for 'script'`.
- **Reporte**: `report-to` / `report-uri`.
- La CSP es **segunda capa**, nunca la única mitigación de XSS.

## 2. Prevención de XSS y DOM XSS

- Usar el **escape automático de React/JSX**; preferir `textContent` sobre
  asignación de HTML.
- **Prohibir `dangerouslySetInnerHTML`**. Si es imprescindible, sanitizar antes
  con **DOMPurify**.
- Prohibir `eval()`, `new Function()` y pasar cadenas a `setTimeout`/`setInterval`.
- **Codificación de salida según contexto**: cuerpo HTML, atributos, bloques JS,
  parámetros URL y CSS.
- No usar atributos de evento inline (`onclick`); registrar con `addEventListener`.
- Validar `href`/`src` contra una **allowlist de esquemas** (`http:`, `https:`)
  para bloquear `javascript:`.

## 3. Tokens y sesión

- **No guardar JWT/tokens en `localStorage` ni `sessionStorage`**: cualquier XSS
  los expone.
- Cookies de sesión con:
  - `HttpOnly`
  - `Secure`
  - `SameSite=Strict` (o `Lax`)
- Prefijo **`__Host-`** para la cookie de sesión.
- **TLS/HSTS** en toda la aplicación.
- **Regenerar el ID de sesión** al cambiar de privilegios (login) para evitar
  *session fixation*.
- Expiración absoluta y por inactividad **gestionadas en servidor**.

## 4. Validación y sanitización

- **Validar siempre en el servidor**; la validación de cliente se puede saltar.
  Usar *allowlists*.
- Validar estructura con **esquemas estrictos** (tipo, formato, longitud).
- Sanitizar HTML de usuario con **DOMPurify**.
- Deserializar con **`JSON.parse()`**, nunca `eval()`.

---

## Cómo encaja en el proyecto

| Requisito | Estado actual | Acción |
|---|---|---|
| CSP por cabecera | ✅ `SecurityConfig` (sin `sandbox`, para que Swagger funcione) | Revisar nonce/hash y `object-src`/`base-uri` |
| `dangerouslySetInnerHTML` | Por auditar en el frontend | Grep y eliminar o sanitizar |
| Token en `localStorage` | ⚠️ `lib/auth.tsx` lo usa | Migrar a cookie `HttpOnly` o justificar |
| Validación servidor | ✅ Bean Validation | Mantener |
| Sanitización HTML | ❌ No hay | Añadir DOMPurify si se renderiza HTML de usuario |

> Las citas [1]–[63] de la respuesta original apuntan a las fuentes del
> notebook; consúltalas con `notebooklm history --json` o desde el MCP.
