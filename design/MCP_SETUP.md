# Configurar MCP (Stitch, Figma, NotebookLM) en otro PC

> ⚠️ **Este documento no contiene secretos.** Las claves reales viajan en un paquete aparte
> (`mcp-transfer-bundle.tar.gz`), que se copia por USB o almacenamiento privado — **nunca** por el repo.

## Requisitos en el PC destino

| Herramienta | Instalación |
|---|---|
| Node.js 20+ | `nvm install 22` o el paquete de tu distro |
| `uv` | `curl -LsSf https://astral.sh/uv/install.sh \| sh` |
| `notebooklm-py` | `uv tool install "notebooklm-py[browser,mcp,cookies]"` |
| Antigravity | <https://antigravity.google> |
| Repo | `git clone https://github.com/juanriano8/Marketplace.git` |

---

## 1. Stitch MCP

Archivo: **`~/.gemini/config/mcp_config.json`** (global) y, si usas la app,
`~/.gemini/antigravity/mcp_config.json`.

```json
{
  "mcpServers": {
    "stitch": {
      "serverUrl": "https://stitch.googleapis.com/mcp",
      "headers": {
        "X-Goog-Api-Key": "<TU_API_KEY>"
      }
    }
  }
}
```

- La **API key** se crea en Stitch → **⚙️ → API keys**.
- La API `stitch.googleapis.com` debe estar habilitada en tu proyecto de GCP:
  ```bash
  gcloud services enable stitch.googleapis.com --project=<PROJECT_ID>
  ```
- Verificación:
  ```bash
  npx @_davideast/stitch-mcp doctor
  npx @_davideast/stitch-mcp tool      # debe listar list_projects, generate_screen_from_text...
  ```

> Antigravity usa `serverUrl` (no `url`) para MCP por HTTP.

## 2. Figma

- **Token personal:** Figma → menú de cuenta → **Settings → Security → Personal access tokens**
  → *Generate new token* con scope **`file_read`**.
- Guárdalo en **`~/.figma-token`** (permisos `600`) o exporta `FIGMA_TOKEN`.
- Scripts (ya en el repo, en `design/`):
  ```bash
  node design/figma-check.mjs <file-key>
  node design/figma-deep.mjs <file-key>
  node design/figma-extract.mjs <file-key>
  ```
- El **file key** está en la URL del archivo:
  `https://www.figma.com/design/<FILE_KEY>/Marketplace`
- Los tokens ya extraídos están en [tokens-origen.css](tokens-origen.css).

## 3. NotebookLM

- Login (una vez):
  ```bash
  notebooklm login                       # descarga Chromium (~191 MB)
  # o, sin Chromium, usando la sesión de Firefox:
  notebooklm login --browser-cookies firefox
  ```
- Bloque MCP:
  ```json
  "notebooklm": {
    "command": "/home/TU_USUARIO/.local/bin/notebooklm-mcp"
  }
  ```
- Si copias el perfil `storage_state.json` del otro PC, evitas el login hasta que caduque la sesión.
- Cuaderno del proyecto: `9bb773f4-7b01-4be5-a382-7d3990d848b0`.

## 4. Dónde va cada archivo (Linux)

| Archivo | Ruta |
|---|---|
| Config MCP global (IDE) | `~/.gemini/config/mcp_config.json` |
| Config MCP app | `~/.gemini/antigravity/mcp_config.json` |
| Token de Figma | `~/.figma-token` |
| Perfil de NotebookLM | `~/.notebooklm/profiles/default/storage_state.json` |

## 5. Verificación

1. Antigravity → `Ctrl + Shift + P` → **Antigravity: Manage MCP Servers** → deben aparecer
   **`stitch`** y **`notebooklm`**.
2. `npx @_davideast/stitch-mcp tool` → lista de herramientas.
3. `notebooklm auth check --test` → `"status": "ok"`.

---

## Seguridad

- **Nunca** subas claves ni tokens al repositorio (es público).
- Rota la API key de Stitch y el token de Figma **después** de transferirlos.
- Borra el paquete de transferencia (y la papelera) cuando termines.
- Si el paquete se pierde, revoca las claves y genera nuevas.
