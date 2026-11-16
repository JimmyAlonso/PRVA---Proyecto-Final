import axios, { AxiosError } from 'axios';

const CLAVE_SESION = 'jiyju.sesion';

// Guía 22 (6.1): la URL del API sale de VITE_API_URL; el timeout cubre el arranque en frío de Render
export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL?.trim() || '/api/v1',
  timeout: 70_000,
});

api.interceptors.request.use((config) => {
  const sesion = leerSesionGuardada();
  if (sesion?.token) {
    config.headers.Authorization = `Bearer ${sesion.token}`;
  }
  return config;
});

// --- Aviso de "servidor despertando": si una petición tarda más de 4 s se avisa a la interfaz
type Oyente = (lento: boolean) => void;
const oyentes = new Set<Oyente>();
let pendientesLentas = 0;

export function escucharLentitud(oyente: Oyente): () => void {
  oyentes.add(oyente);
  return () => {
    oyentes.delete(oyente);
  };
}

function avisar() {
  oyentes.forEach((o) => o(pendientesLentas > 0));
}

api.interceptors.request.use((config) => {
  const temporizador = window.setTimeout(() => {
    pendientesLentas += 1;
    (config as { _lenta?: boolean })._lenta = true;
    avisar();
  }, 4000);
  (config as { _temporizador?: number })._temporizador = temporizador;
  return config;
});

function terminar(config: unknown) {
  const c = config as { _temporizador?: number; _lenta?: boolean } | undefined;
  if (!c) return;
  if (c._temporizador) window.clearTimeout(c._temporizador);
  if (c._lenta) {
    pendientesLentas = Math.max(0, pendientesLentas - 1);
    c._lenta = false;
    avisar();
  }
}

api.interceptors.response.use(
  (respuesta) => {
    terminar(respuesta.config);
    return respuesta;
  },
  (error: AxiosError) => {
    terminar(error.config);
    if (error.response?.status === 401 && leerSesionGuardada()) {
      // Token vencido: se cierra la sesión local
      const url = error.config?.url ?? '';
      if (!url.includes('/auth/')) {
        localStorage.removeItem(CLAVE_SESION);
        window.dispatchEvent(new Event('jiyju:sesion-expirada'));
      }
    }
    return Promise.reject(error);
  },
);

// --- Sesión guardada en el navegador
export interface SesionGuardada {
  token: string;
  expiraEn: string;
  usuario: import('./types').Usuario;
}

export function leerSesionGuardada(): SesionGuardada | null {
  try {
    const crudo = localStorage.getItem(CLAVE_SESION);
    if (!crudo) return null;
    const sesion = JSON.parse(crudo) as SesionGuardada;
    if (new Date(sesion.expiraEn).getTime() <= Date.now()) {
      localStorage.removeItem(CLAVE_SESION);
      return null;
    }
    return sesion;
  } catch {
    return null;
  }
}

export function guardarSesion(sesion: SesionGuardada | null) {
  try {
    if (sesion) localStorage.setItem(CLAVE_SESION, JSON.stringify(sesion));
    else localStorage.removeItem(CLAVE_SESION);
  } catch {
    // almacenamiento no disponible: la sesión vive solo en memoria
  }
}

/** Convierte cualquier error del API en un mensaje legible para la interfaz. */
export function mensajeDeError(error: unknown): string {
  if (axios.isAxiosError(error)) {
    if (error.code === 'ECONNABORTED') {
      return 'El servidor tardó demasiado en responder. Intenta de nuevo en unos segundos.';
    }
    if (!error.response) {
      return 'No hay conexión con el servidor. Revisa tu internet e intenta de nuevo.';
    }
    const datos = error.response.data as { mensaje?: string; detalles?: string[] } | undefined;
    if (datos?.detalles && datos.detalles.length > 0) {
      return `${datos.mensaje ?? 'Revisa los datos'}: ${datos.detalles.join('; ')}`;
    }
    if (datos?.mensaje) return datos.mensaje;
    if (error.response.status === 401) return 'Tu sesión no es válida. Inicia sesión de nuevo.';
    if (error.response.status === 403) return 'Tu cuenta no tiene permiso para esta acción.';
    if (error.response.status === 404) return 'No encontramos lo que buscabas.';
  }
  return 'Ocurrió un error inesperado. Intenta de nuevo.';
}
