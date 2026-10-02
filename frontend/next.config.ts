import type { NextConfig } from 'next';

// ---------------------------------------------------------------------------
// Destino del proxy
// ---------------------------------------------------------------------------
// Por defecto apunta al backend DESPLEGADO EN CLOUD RUN, para que el panel
// funcione en cualquier PC (Windows o Linux) sin configurar nada: basta con
// clonar el repositorio y hacer `npm run dev`.
//
// Para usar un backend LOCAL en su lugar, crea `frontend/.env.local` con:
//     MARKETPLACE_API_URL=http://localhost:8080
// y reinicia `npm run dev`. Ese archivo está en .gitignore, así que es
// configuración solo de tu PC (por eso no viaja con `git pull`).
const API_URL =
  process.env.MARKETPLACE_API_URL ?? 'https://marketplace-api-805790031718.us-central1.run.app';

const nextConfig: NextConfig = {
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

  // El navegador sólo habla con Next.js (puerto 3000): las peticiones a /api/v1/**
  // se reenvían al backend Spring Boot (puerto 8080). Así no hay problemas de CORS
  // ni de puertos, y el navegador nunca necesita saber dónde está la API.
  async rewrites() {
    return [
      {
        source: '/api/v1/:path*',
        destination: `${API_URL}/api/v1/:path*`,
      },
      {
        source: '/actuator/:path*',
        destination: `${API_URL}/actuator/:path*`,
      },
    ];
  },
};

export default nextConfig;
