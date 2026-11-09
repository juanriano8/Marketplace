import type { NextConfig } from 'next';

// ---------------------------------------------------------------------------
// Destino del proxy
// ---------------------------------------------------------------------------
// En local apunta al backend Spring Boot de `localhost:8080`.
// En producción (Vercel) se define `MARKETPLACE_API_URL` con la URL pública
// de Render, por ejemplo https://marketplace-api.onrender.com.
//
// Para usar otro backend crea `frontend/.env.local` con:
//     MARKETPLACE_API_URL=https://tu-api.onrender.com
// y reinicia `npm run dev`. Ese archivo está en .gitignore.
const API_URL = process.env.MARKETPLACE_API_URL ?? 'http://localhost:8080';

const nextConfig: NextConfig = {
  // Empaqueta el panel en un servidor Node autonomo (.next/standalone), que es
  // lo que copia el Dockerfile a la imagen final. Reduce la imagen de ~1 GB a
  // ~150 MB y el arranque en frio de Cloud Run a unos segundos.
  output: 'standalone',
  // ---------------------------------------------------------------------------
  // Orígenes permitidos en desarrollo
  // ---------------------------------------------------------------------------
  // Next.js 16 bloquea por defecto las peticiones a /_next/** cuando el origen no
  // es localhost: devuelve 403 y el JavaScript nunca llega al navegador, así que
  // la página se queda "cargando" para siempre y ningún botón responde.
  //
  // Eso pasa cuando abres el panel con la URL que Next imprime como "- Network:",
  // por ejemplo http://192.168.56.1:3000 en vez de http://localhost:3000.
  //
  // Aquí se autorizan las IPs de red de los equipos donde se prueba el panel.
  // Si tu IP cambia (DHCP), añádela a esta lista y reinicia `npm run dev`.
  allowedDevOrigins: [
    '192.168.56.1', // adaptador host-only de VirtualBox en este PC
    '192.168.10.7', // red Wi-Fi de este PC
    '192.168.107.2', // segunda red virtual de este PC
    '192.168.10.22', // el PC con Linux, si se ejecuta allí el panel
  ],

  // Las peticiones a /api/v1/** las atiende el route handler
  // `app/api/v1/[...path]/route.ts`, que lee la cookie httpOnly y añade la
  // cabecera Authorization antes de reenviar al backend. No se declara un
  // rewrite para /api/v1 porque se aplicaría antes y saltaría ese proxy.
  // /actuator se mantiene como rewrite directo para el enlace de salud.
  async rewrites() {
    return [
      {
        source: '/actuator/:path*',
        destination: `${API_URL}/actuator/:path*`,
      },
    ];
  },
};

export default nextConfig;
