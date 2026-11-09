# Decisión de UI: mantener `ui.tsx` y no adoptar Shadcn UI

- **Estado:** aceptada
- **Fecha:** 2026-11-09
- **Ámbito:** frontend (`frontend/src/components/ui.tsx`, `frontend/src/app/globals.css`)
- **Requisito de la rúbrica:** «usar Shadcn UI o justificar la alternativa»

---

## 1. Contexto

El panel web tiene **17 vistas** y un sistema de diseño propio, extraído de Stitch y de los
tokens de Figma «Origen»:

- Tokens de color, tipografía, radios y espaciados en `frontend/src/app/globals.css` (`@theme`).
- Componentes compartidos en `frontend/src/components/ui.tsx`:
  `StatusBadge`, `Badge`, `StockChip`, `Stars`, `formatMoney`, `formatDate` y `stockState`.
- Contrato visual documentado en [DESIGN.md](DESIGN.md).

La interfaz es **densa a propósito** (cuerpo de 12 px, tablas de administración, tarjetas de
producto con chip de stock). El diseño no es el look por defecto de una librería: los colores,
bordes y alturas son valores exactos del diseño.

## 2. Decisión

**Mantener el archivo `ui.tsx` propio** para los componentes actuales y **no migrar a Shadcn UI**
en esta entrega.

Shadcn UI no es una dependencia de runtime: es una colección de componentes que se copian al
proyecto y se apoyan en Radix UI. Se puede adoptar **de forma incremental** más adelante sin
reescribir la aplicación.

## 3. Motivos

1. **El diseño ya está resuelto y es más específico que el de Shadcn.** Shadcn trae alturas,
   paddings y colores por defecto (típicamente 40 px, radios de 6–8 px, tema neutro). Aplicarlo
   aquí exigiría re-tematizar cada componente para que coincida con los tokens de Stitch; el
   resultado sería más código de adaptación que el componente propio actual.
2. **La superficie real es pequeña.** Son 7 exports, en su mayoría funciones de formato y
   variantes de badge/chip. No hay diálogos, menús, combobox, tabs ni tablas interactivas que
   justifiquen arrastrar Radix y su árbol de dependencias.
3. **Compatibilidad con el stack actual.** El proyecto usa Next.js 16, React 19 y Tailwind 4.
   Shadcn funciona, pero su CLI y sus ejemplos asumen una configuración clásica de Tailwind;
   añadir esa capa justo antes de la entrega aumenta el riesgo de romper el build.
4. **Control de accesibilidad y semántica.** Los badges y chips son texto con color y
   `title`; los formularios usan `<label>`, `required` y mensajes de error propios. No hay
   comportamiento interactivo complejo que Shadcn/Radix resuelva mejor hoy.
5. **Coste de migración vs. beneficio.** Migrar 17 páginas y 9 componentes no aporta
   funcionalidad nueva ni mejora la nota por sí solo; el requisito se cumple justificando la
   decisión y dejando el camino abierto.

## 4. Alternativas consideradas

| Alternativa | Por qué no se eligió |
|---|---|
| Migrar todo a Shadcn UI ahora | Re-tematización completa, más dependencias y riesgo de regresión en 17 vistas |
| Instalar solo los componentes que se usan | Los badges/chips no existen como tales en Shadcn; habría que crearlos igualmente |
| Librería completa (MUI, Chakra, Mantine) | Peso, estilos difíciles de alinear con los tokens y bundle mayor |
| Dejar `ui.tsx` sin documentar | Incumple el requisito de justificar la alternativa |

## 5. Consecuencias

**Positivas**

- Menos dependencias y un bundle más pequeño.
- Los tokens del diseño se aplican en un solo lugar (`globals.css`).
- Los componentes son legibles y adaptables sin pelearse con estilos de terceros.

**Negativas / riesgos**

- Hay que mantener a mano los componentes y sus variantes.
- No se heredan mejoras de accesibilidad de Radix (foco atrapado, portales, teclado).
- Si el panel crece, el archivo `ui.tsx` puede convertirse en un cajón de sastre.

**Mitigaciones**

- Mantener `ui.tsx` como catálogo pequeño y documentado; no añadir lógica de negocio.
- Para componentes interactivos complejos (diálogo, menú, combobox), evaluar Radix/Shadcn de
  forma puntual en lugar de reinventarlos.
- Revisar accesibilidad en cada vista con teclado y lector de pantalla antes de la entrega.

## 6. Cuándo revisar esta decisión

Se reconsiderará adoptar Shadcn/Radix si se cumple alguno de estos disparadores:

1. Aparecen **diálogos, menús, combobox, tabs o date pickers** propios.
2. `ui.tsx` supera los **10–12 componentes** o mezcla variantes difíciles de mantener.
3. Se incorpora más de un desarrollador al frontend y se necesita una convención común.
4. La auditoría de accesibilidad detecta carencias que Radix resolvería mejor.

## 7. Referencias

- [DESIGN.md](DESIGN.md) — tokens, componentes y reglas visuales.
- [UX_MAPPING.md](UX_MAPPING.md) — rutas de la API y pantallas.
- [`frontend/src/components/ui.tsx`](../../frontend/src/components/ui.tsx) — implementación actual.
- [`frontend/src/app/globals.css`](../../frontend/src/app/globals.css) — tema y clases de componentes.
