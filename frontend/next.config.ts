import type { NextConfig } from 'next';

const API_URL = process.env.MARKETPLACE_API_URL ?? 'http://localhost:8080';

const nextConfig: NextConfig = {
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
