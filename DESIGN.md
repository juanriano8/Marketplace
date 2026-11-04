# DESIGN.md — Sistema de diseño del Marketplace

**Fuente de verdad visual:** Figma «Origen» → tokens extraídos en
[design/tokens-origen.css](design/tokens-origen.css) (849 nodos, 2026-10-09).
**Reglas de seguridad/UX:** [design/notebooklm/owasp-frontend.md](design/notebooklm/owasp-frontend.md).
**Implementación:** `frontend/src/app/globals.css` (bloque `@theme`) + `frontend/src/components/ui.tsx`.

> Este documento es el contrato visual. Si un color, radio o tamaño no está aquí, no debería
> aparecer en el código.

---

## 1. Principios

1. **Interfaz densa, no espaciosa.** El cuerpo es **12 px**; la app muestra mucha información por
   pantalla (catálogo, tablas de admin, inventario).
2. **Verde de marca como acento, no como fondo.** El `#17675B` se usa en CTAs, precios y estados
   activos; los fondos son neutros claros.
3. **Sin sombras fuertes.** La jerarquía se construye con bordes de 1 px y cambios de superficie.
4. **Todo deriva de tokens.** Un cambio de color se hace en `globals.css`, no en 40 componentes.
5. **Los 4 estados son obligatorios** en toda vista: carga, éxito, vacío y error con reintento.

---

## 2. Color

### Marca (verde)

| Token | Hex | Uso |
|---|---|---|
| `brand-50` | `#E8F2EC` | Fondo suave, badge de éxito, ítem activo de sidebar |
| `brand-100` | `#C6D6CE` | Borde tenue de marca |
| `brand-200` | `#A3C2B7` | Interpolado (hover suave) |
| `brand-300` | `#7FAE9F` | Interpolado |
| `brand-400` | `#4F8E7F` | Interpolado |
| `brand-500` | `#228274` | Interpolado |
| `brand-600` | **`#17675B`** | **Color principal**: botones, precios, enlaces |
| `brand-700` | `#14574D` | Hover del primario |
| `brand-800` | `#143D35` | Verde oscuro (texto sobre fondos claros) |
| `brand-900` | `#0F2F29` | Interpolado |

### Neutros

| Token | Hex | Uso |
|---|---|---|
| `ink-50` | `#F7F8F5` | **Fondo de página** |
| `ink-100` | `#E1E6DF` | **Bordes y separadores** |
| `ink-200` | `#CDD3CC` | Interpolado |
| `ink-300` | `#B4BBB4` | Interpolado (placeholder) |
| `ink-400` | `#8F978F` | Interpolado (texto deshabilitado) |
| `ink-500` | **`#69746E`** | **Texto secundario** (el más usado: 181 veces) |
| `ink-600` | `#4E5C56` | Interpolado |
| `ink-700` | `#3A4943` | Interpolado |
| `ink-800` | `#2A3833` | Interpolado |
| `ink-900` | **`#202C28`** | **Títulos y texto fuerte** |

### Estados

| Estado | Texto | Fondo | Uso |
|---|---|---|---|
| Éxito | `#17675B` | `#E8F2EC` | «Disponible», «Pagado», «Enviado» |
| Aviso | `#8B6018` | `#FBF2DE` | «Últimas unidades», «Pendiente» |
| Peligro | `#A94439` | `#FAEBE7` | «Agotado», «Cancelado», error |

### Superficies

| Token | Hex | Uso |
|---|---|---|
| `surface` | `#FFFFFF` | Tarjetas, tablas, modales |
| `surface-alt` | `#F0EEE7` | Bloques destacados (crema) |
| Página | `#F7F8F5` | Fondo general |

---

## 3. Tipografía

**Familia única:** **Inter** (400 / 500 / 600). Sin excepciones.

| Rol | Tamaño | Peso | Uso |
|---|---|---|---|
| Titular | 28–30 px | 600 | Hero, título de portada |
| Título de sección | 18 px | 600 | «Catálogo», «Mis compras» |
| Subtítulo | 15–16 px | 500 | Cabeceras de tarjeta |
| Destacado | 13 px | 500 | Precio, nombre de producto |
| **Cuerpo** | **12 px** | 400 | Texto general (el más usado) |
| Etiqueta | 11 px | 500 | Badges, metadatos |
| Metadato | 10 px | 400 | Fechas, IDs |
| Micro | 9 px | 600 | Badges diminutos |

> Mapeo Tailwind: `text-[11px]`, `text-[12px]`, `text-sm` (13), `text-lg` (18),
> `text-3xl` (30).

---

## 4. Radios y espaciado

| Token | Valor | Uso |
|---|---|---|
| `radius-sm` | 3 px | Chips pequeños |
| `radius-md` | 6 px | Inputs compactos |
| `radius-lg` | **8 px** | **Botones, inputs, tarjetas pequeñas (el más común)** |
| `radius-xl` | 12 px | Tarjetas medianas |
| `radius-2xl` | 16 px | Bloques grandes |
| `radius-pill` | 100 px | Badges y chips |

**Escala de espaciado (densa):** 4 · 8 · 12 · 16 · 24 · 32 px.
Gutter de grid: 16 px. Padding de tarjeta: 12–16 px.

---

## 5. Componentes

### Botón

| Variante | Fondo | Texto | Borde | Uso |
|---|---|---|---|---|
| Primario | `brand-600` | blanco | — | Acción principal («Añadir», «Pagar») |
| Secundario | `brand-50` | `brand-700` | `brand-100` | Acción alternativa |
| Fantasma | transparente | `ink-700` | `ink-100` | Acción terciaria |
| Peligro | `danger` | blanco | — | Eliminar / cancelar |

- Radio **8 px**, alto 36–40 px, padding `12px 16px`, `font-weight: 500`.
- **Deshabilitado:** fondo `ink-100`, texto `ink-400`, `cursor: not-allowed`.
- **Foco visible:** anillo de 2 px `brand-600` con offset 2 px.

### Input / Select / Textarea

- Fondo `surface`, borde 1 px `ink-100`, radio **8 px**, alto 38 px.
- Placeholder `ink-400`.
- **Foco:** borde `brand-600` + anillo suave.
- **Error:** borde `danger`, mensaje de 11 px `danger` debajo.
- Siempre con `<label>` asociado (accesibilidad).

### Tarjeta de producto

- Fondo `surface`, borde 1 px `ink-100`, radio **12 px**, sin sombra.
- Imagen 4:3, radio 8 px arriba.
- Nombre (13 px, 600, `ink-900`) · Precio (13 px, 600, `brand-600`).
- Badge de stock en píldora.
- CTA de ancho completo.

### Badge

- Píldora (`radius-pill`), 9–11 px, 600.
- `success` / `warning` / `danger` según la tabla de estados.

### Tabla (admin)

- Cabecera `ink-50`, texto 11 px 600 `ink-500`.
- Filas con separador 1 px `ink-100`, hover `ink-50`.
- Números alineados a la derecha; acciones a la izquierda.
- En móvil: scroll horizontal, nunca romper el layout.

### Navbar

- Fondo `surface`, borde inferior 1 px `ink-100`, alto 56–64 px.
- Logo a la izquierda, buscador centrado, carrito + cuenta a la derecha.
- Enlace activo: texto `brand-600` + borde inferior de 2 px.

---

## 6. Los 4 estados (obligatorios en las 15 páginas)

| Estado | Qué mostrar |
|---|---|
| **Carga** | *Skeletons* con la forma del contenido (no un spinner suelto) |
| **Éxito** | Contenido real |
| **Vacío** | Icono + mensaje + acción («Aún no hay productos» → «Ver todo») |
| **Error** | Mensaje claro + **botón «Reintentar»** que vuelve a lanzar el fetch |

> El botón de reintento es el hueco más visible de la rúbrica
> ([PENDIENTES.md §2.2](PENDIENTES.md#L51)). Debe existir en **todas** las vistas.

---

## 7. Accesibilidad

- Contraste mínimo **4.5:1** para texto normal (los tokens ya lo cumplen).
- **Nunca** usar color como único indicador de estado: añadir texto o icono.
- Foco visible en todos los interactivos (no `outline: none` sin reemplazo).
- Formularios con `label`, `aria-invalid` y mensaje de error asociado.
- Navegación completa con teclado; `Esc` cierra modales.

---

## 8. Seguridad en la UI (del notebook OWASP)

| Regla | Implicación de diseño |
|---|---|
| Nada de `dangerouslySetInnerHTML` | El HTML de usuario se muestra como texto |
| Sin tokens en `localStorage` | Pendiente: migrar a cookie `HttpOnly` (ver PENDIENTES #7) |
| Validación en servidor | El frontend valida para UX, pero nunca es la única barrera |
| Errores sin filtrar datos | Mostrar mensajes genéricos; el detalle va al log |
| CSP estricta | Sin scripts inline; todo evento con `addEventListener` |

---

## 9. Mapeo a Tailwind

En `frontend/src/app/globals.css`:

```css
@theme {
  --color-brand-50: #e8f2ec;
  --color-brand-100: #c6d6ce;
  --color-brand-600: #17675b;
  --color-brand-800: #143d35;
  --color-ink-50: #f7f8f5;
  --color-ink-100: #e1e6df;
  --color-ink-500: #69746e;
  --color-ink-900: #202c28;
  --color-surface: #ffffff;
  --color-surface-alt: #f0eee7;
  --color-success: #17675b;
  --color-success-soft: #e8f2ec;
  --color-warning: #8b6018;
  --color-warning-soft: #fbf2de;
  --color-danger: #a94439;
  --color-danger-soft: #faebe7;
  --font-sans: 'Inter', ui-sans-serif, system-ui, sans-serif;
  --radius-sm: 3px;
  --radius-md: 6px;
  --radius-lg: 8px;
  --radius-xl: 12px;
  --radius-2xl: 16px;
  --radius-pill: 100px;
}
```

---

## 10. Do / Don't

| ✅ Do | ❌ Don't |
|---|---|
| Usar `bg-brand-600` para CTAs | Usar el verde como fondo de página |
| Bordes de 1 px `ink-100` | Sombras difusas tipo Material |
| Cuerpo 12 px | Texto de 16 px en tablas |
| Skeletons con la forma real | Spinner centrado en toda la página |
| Un botón primario por vista | Varios CTAs verdes compitiendo |
| Texto + color para el estado | Solo color (daltonismo) |

---

**Última actualización:** 2026-11-03 · basado en los tokens de Figma «Origen».
