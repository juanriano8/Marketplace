/**
 * Categorías de producto.
 *
 * Provienen de la hoja `categorias` del dataset de referencia
 * (`marketplace_datos.csv`), que tiene 8 categorías principales y 26 subcategorías.
 * Se mantienen los nombres tal cual están en el dataset para que los datos de la
 * aplicación coincidan con el modelo de datos del proyecto.
 *
 * Si quieres cambiar los nombres (por ejemplo poner acentos), edita solo este archivo.
 */

export type GrupoCategorias = {
  /** Categoría principal, que se usa como título del desplegable. */
  grupo: string;
  /** Categoría principal + sus subcategorías. Todas son seleccionables. */
  opciones: string[];
};

export const CATEGORIAS: GrupoCategorias[] = [
  {
    grupo: 'Electronica',
    opciones: ['Electronica', 'Celulares', 'Computadoras', 'Audio', 'Camaras', 'Televisores'],
  },
  {
    grupo: 'Ropa',
    opciones: ['Ropa', 'Ropa de hombre', 'Ropa de mujer', 'Ropa infantil', 'Calzado'],
  },
  {
    grupo: 'Hogar',
    opciones: ['Hogar', 'Muebles', 'Electrodomesticos', 'Decoracion', 'Cocina'],
  },
  {
    grupo: 'Deportes',
    opciones: ['Deportes', 'Fitness', 'Ciclismo', 'Futbol', 'Running'],
  },
  {
    grupo: 'Belleza',
    opciones: ['Belleza', 'Cuidado facial', 'Maquillaje', 'Perfumeria'],
  },
  {
    grupo: 'Juguetes',
    opciones: ['Juguetes', 'Juguetes educativos', 'Juegos de mesa'],
  },
  {
    grupo: 'Mascotas',
    opciones: ['Mascotas', 'Alimento', 'Accesorios'],
  },
  {
    grupo: 'Oficina',
    opciones: ['Oficina', 'Papeleria', 'Muebles de oficina'],
  },
];

/** Todas las categorías en una sola lista. */
export const CATEGORIAS_PLANAS: string[] = CATEGORIAS.flatMap((g) => g.opciones);

/**
 * Devuelve los grupos de categorías añadiendo las que ya estén en uso y no
 * figuren en la lista base (por ejemplo productos antiguos con otra categoría),
 * dentro de un grupo "Otras".
 *
 * Así el desplegable nunca "pierde" la categoría de un producto existente al
 * editarlo.
 */
export function agruparCategorias(extra: Iterable<string> = []): GrupoCategorias[] {
  const conocidas = new Set(CATEGORIAS_PLANAS);
  const otras = [...new Set([...extra])]
    .filter((c) => c && !conocidas.has(c))
    .sort((a, b) => a.localeCompare(b, 'es'));

  if (otras.length === 0) {
    return CATEGORIAS;
  }

  return [...CATEGORIAS, { grupo: 'Otras (ya en uso)', opciones: otras }];
}
