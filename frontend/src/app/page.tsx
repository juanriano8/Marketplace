import Link from 'next/link';

const ROLE_CARDS = [
  {
    href: '/productos',
    title: 'Comprador',
    description: 'Explora el catálogo, añade productos al carrito, paga y deja reseñas de tus compras.',
    icon: '🛍️',
    accent: 'border-brand-200 bg-brand-50',
  },
  {
    href: '/vendedor',
    title: 'Vendedor',
    description: 'Publica productos, gestiona tu inventario y registra los despachos de tus sub-órdenes.',
    icon: '🏪',
    accent: 'border-emerald-200 bg-emerald-50',
  },
  {
    href: '/admin',
    title: 'Administrador',
    description: 'Aprueba vendedores y productos, supervisa el stock y consulta todas las transacciones.',
    icon: '🛡️',
    accent: 'border-amber-200 bg-amber-50',
  },
];

export default function HomePage() {
  return (
    <div className="space-y-10">
      <section className="card overflow-hidden">
        <div className="bg-gradient-to-br from-brand-600 to-brand-800 px-6 py-12 text-white sm:px-10">
          <p className="text-xs font-semibold uppercase tracking-wider text-brand-100">
            Proyecto fullstack · Spring Boot + Next.js
          </p>
          <h1 className="mt-2 max-w-2xl text-3xl font-bold sm:text-4xl">
            Marketplace multi-rol con API REST y base de datos en Google Cloud
          </h1>
          <p className="mt-3 max-w-2xl text-sm text-brand-50">
            Backend en Spring Boot 3 con arquitectura hexagonal, autenticación JWT, control de acceso
            por roles y antisobreventa con bloqueo pesimista. Este panel consume esa misma API.
          </p>
          <div className="mt-6 flex flex-wrap gap-3">
            <Link href="/productos" className="btn btn-secondary">
              Ver catálogo
            </Link>
            <Link
              href="/registro"
              className="btn border-white/40 bg-white/10 text-white hover:bg-white/20"
            >
              Crear una cuenta
            </Link>
          </div>
        </div>
      </section>

      <section>
        <h2 className="mb-4 text-lg font-bold text-ink-900">Tres roles, tres paneles</h2>
        <div className="grid gap-4 md:grid-cols-3">
          {ROLE_CARDS.map((card) => (
            <Link
              key={card.href}
              href={card.href}
              className={`card border p-5 transition hover:-translate-y-0.5 hover:shadow-md ${card.accent}`}
            >
              <span className="text-2xl">{card.icon}</span>
              <h3 className="mt-3 font-bold text-ink-900">{card.title}</h3>
              <p className="mt-1 text-sm text-ink-600">{card.description}</p>
            </Link>
          ))}
        </div>
      </section>

      <section className="grid gap-4 md:grid-cols-2">
        <div className="card p-5">
          <h3 className="font-bold text-ink-900">Qué se puede hacer aquí</h3>
          <ul className="mt-2 space-y-1 text-sm text-ink-600">
            <li>· Registro e inicio de sesión con JWT por rol</li>
            <li>· Catálogo público con paginación y stock real</li>
            <li>· Carrito, checkout y pago (pasarela simulada)</li>
            <li>· Órdenes divididas en sub-órdenes por vendedor</li>
            <li>· Reseñas solo para compras verificadas</li>
            <li>· Moderación de productos y verificación de vendedores</li>
            <li>· Auditoría del ledger de inventario</li>
          </ul>
        </div>
        <div className="card p-5">
          <h3 className="font-bold text-ink-900">Cómo probarlo</h3>
          <ol className="mt-2 space-y-1 text-sm text-ink-600">
            <li>
              1. Entra como administrador con las credenciales del backend (<code>.env</code>).
            </li>
            <li>2. Registra un vendedor y apruébalo desde el panel de administración.</li>
            <li>3. Inicia sesión como vendedor y publica un producto.</li>
            <li>4. Apruébalo como administrador para que aparezca en el catálogo.</li>
            <li>5. Regístrate como comprador, añádelo al carrito y paga.</li>
            <li>6. Como vendedor, registra el despacho; como comprador, deja tu reseña.</li>
          </ol>
          <p className="mt-3 text-xs text-ink-500">
            El backend debe estar escuchando en <code>http://localhost:8080</code>.
          </p>
        </div>
      </section>
    </div>
  );
}
