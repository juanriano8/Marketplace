/**
 * Saneamiento de entradas del panel.
 *
 * React ya escapa el texto al renderizarlo, así que esto no sustituye al
 * escape de salida: elimina caracteres de control, normaliza espacios y
 * valida URLs para no guardar valores raros en la base de datos.
 */

/** Quita caracteres de control y colapsa espacios. Para campos de una línea. */
export function sanitizeText(value: string, maxLength: number): string {
  return value
    .replace(/[\u0000-\u0008\u000B\u000C\u000E-\u001F\u007F]/g, '')
    .replace(/\s+/g, ' ')
    .trim()
    .slice(0, maxLength);
}

/** Igual que `sanitizeText` pero conserva saltos de línea y tabulaciones. */
export function sanitizeMultiline(value: string, maxLength: number): string {
  return value
    .replace(/[\u0000-\u0008\u000B\u000C\u000E-\u001F\u007F]/g, '')
    .replace(/\r\n/g, '\n')
    .trim()
    .slice(0, maxLength);
}

/**
 * Valida la URL de imagen: solo http(s) o una ruta relativa que empiece por `/`.
 *
 * @returns la URL limpia, `''` si estaba vacía o `null` si no es válida
 */
export function sanitizeImageUrl(value: string): string | null {
  const url = value.trim();
  if (!url) return '';

  if (url.startsWith('//')) {
    // Las URLs protocol-relative cargarían recursos de otro dominio.
    return null;
  }

  if (url.startsWith('/')) {
    return url.slice(0, 255);
  }

  try {
    const parsed = new URL(url);
    if (parsed.protocol === 'http:' || parsed.protocol === 'https:') {
      return url.slice(0, 255);
    }
  } catch {
    // URL relativa sin barra inicial o valor no parseable.
  }

  return null;
}
