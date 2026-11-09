import type { Metadata } from 'next';
import { Inter } from 'next/font/google';
import './globals.css';
import { AuthProvider } from '@/lib/auth';
import { FavoritesProvider } from '@/lib/favorites';
import { AppShell } from '@/components/AppShell';

const inter = Inter({
  subsets: ['latin'],
  variable: '--font-inter',
  display: 'swap',
});

export const metadata: Metadata = {
  title: 'Marketplace — Panel Multi-Rol',
  description:
    'Panel web del marketplace: catálogo, carrito y compras para compradores, gestión de productos y despachos para vendedores, moderación para administradores.',
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="es" className={inter.variable}>
      <body className="min-h-screen font-sans">
        <AuthProvider>
          <FavoritesProvider>
            <AppShell>{children}</AppShell>
          </FavoritesProvider>
        </AuthProvider>
      </body>
    </html>
  );
}
