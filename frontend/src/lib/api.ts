/**
 * Cliente HTTP del frontend.
 *
 * Todas las llamadas van a rutas relativas (`/api/v1/...`), que Next.js reenvía al
 * backend Spring Boot mediante el `rewrite` de `next.config.ts`. El navegador nunca
 * habla directamente con el puerto 8080, así que no hay CORS de por medio.
 */

export type ApiError = {
  status: number;
  title: string;
  detail: string;
  type?: string;
  invalidParams?: Record<string, string>;
};

export class ApiException extends Error {
  readonly status: number;
  readonly title: string;
  readonly detail: string;
  readonly invalidParams?: Record<string, string>;

  constructor(error: ApiError) {
    super(error.detail);
    this.name = 'ApiException';
    this.status = error.status;
    this.title = error.title;
    this.detail = error.detail;
    this.invalidParams = error.invalidParams;
  }

  /** Mensaje listo para mostrar: añade los errores de campo si los hay. */
  get displayMessage(): string {
    if (this.invalidParams && Object.keys(this.invalidParams).length > 0) {
      const campos = Object.entries(this.invalidParams)
        .map(([campo, mensaje]) => `${campo}: ${mensaje}`)
        .join(' · ');
      return `${this.detail} → ${campos}`;
    }
    return this.detail;
  }
}

type RequestOptions = {
  method?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';
  body?: unknown;
  /** Token JWT del usuario autenticado. */
  token?: string | null;
  /** Señal para cancelar la petición. */
  signal?: AbortSignal;
};

async function toApiError(response: Response): Promise<ApiError> {
  let title = `Error ${response.status}`;
  let detail = response.statusText || 'La petición falló';
  let type: string | undefined;
  let invalidParams: Record<string, string> | undefined;

  try {
    const problem = await response.json();
    if (problem && typeof problem === 'object') {
      title = problem.title ?? title;
      detail = problem.detail ?? detail;
      type = problem.type;
      invalidParams = problem.invalidParams;
    }
  } catch {
    // El backend siempre devuelve ProblemDetail JSON; si no se puede parsear, se usan los valores por defecto.
  }

  return { status: response.status, title, detail, type, invalidParams };
}

export async function apiFetch<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { method = 'GET', body, token, signal } = options;

  const headers: Record<string, string> = { Accept: 'application/json' };
  if (body !== undefined) {
    headers['Content-Type'] = 'application/json';
  }
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(path, {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
    signal,
    cache: 'no-store',
  });

  if (!response.ok) {
    throw new ApiException(await toApiError(response));
  }

  // 204 No Content (por ejemplo al eliminar una reseña)
  if (response.status === 204) {
    return undefined as T;
  }

  const text = await response.text();
  return (text ? JSON.parse(text) : undefined) as T;
}

/** Respuesta paginada de Spring Data. */
export type Page<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  empty: boolean;
  numberOfElements: number;
};

/** Convierte el objeto de error en un mensaje mostrable. */
export function errorMessage(error: unknown): string {
  if (error instanceof ApiException) {
    return error.displayMessage;
  }
  if (error instanceof Error) {
    return error.message;
  }
  return 'Ha ocurrido un error inesperado';
}
