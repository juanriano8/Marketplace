# ---------------------------------------------------------------------------
# Panel del Marketplace (Next.js 16) para Cloud Run y cualquier host de
# contenedores.
#
# Construccion multi-etapa:
#   1. deps    -> instala dependencias exactas del package-lock
#   2. build   -> next build con output: 'standalone'
#   3. runtime -> imagen minima: solo el servidor standalone y los estaticos
#
# Cloud Run inyecta PORT (8080 por defecto). `server.js` del bundle standalone
# lo respeta, por eso no se usa `next start -p`.
# ---------------------------------------------------------------------------

# ---------- Etapa 1: dependencias ----------
FROM node:22-alpine AS deps
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci

# ---------- Etapa 2: compilacion ----------
FROM node:22-alpine AS build
WORKDIR /app
ENV NEXT_TELEMETRY_DISABLED=1
COPY --from=deps /app/node_modules ./node_modules
COPY . .
# Las variables se leen en tiempo de ejecucion (route handlers), no se incrustan
# aqui. MARKETPLACE_API_URL se define como variable del servicio en Cloud Run.
RUN npm run build

# ---------- Etapa 3: ejecucion ----------
FROM node:22-alpine AS runtime
WORKDIR /app
ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1
ENV PORT=8080
ENV HOSTNAME=0.0.0.0

# Usuario sin privilegios.
RUN addgroup -S nextjs && adduser -S nextjs -G nextjs
USER nextjs

COPY --from=build --chown=nextjs:nextjs /app/public ./public
COPY --from=build --chown=nextjs:nextjs /app/.next/standalone ./
COPY --from=build --chown=nextjs:nextjs /app/.next/static ./.next/static

EXPOSE 8080

CMD ["node", "server.js"]
