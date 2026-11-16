import type { EstadoReserva, Rol } from '../services/types';

const pesos = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 });
export const dinero = (valor: number | null | undefined) => pesos.format(valor ?? 0);

/** Convierte 'AAAA-MM-DD' en fecha local sin corrimiento por zona horaria. */
export function aFecha(iso: string): Date {
  const [a, m, d] = iso.slice(0, 10).split('-').map(Number);
  return new Date(a, m - 1, d);
}

export function iso(fecha: Date): string {
  const m = String(fecha.getMonth() + 1).padStart(2, '0');
  const d = String(fecha.getDate()).padStart(2, '0');
  return `${fecha.getFullYear()}-${m}-${d}`;
}

export function sumarDias(isoFecha: string, dias: number): string {
  const f = aFecha(isoFecha);
  f.setDate(f.getDate() + dias);
  return iso(f);
}

export const hoyIso = () => iso(new Date());

const corta = new Intl.DateTimeFormat('es-CO', { day: 'numeric', month: 'short' });
const larga = new Intl.DateTimeFormat('es-CO', { weekday: 'short', day: 'numeric', month: 'short', year: 'numeric' });
export const fechaCorta = (isoFecha: string) => corta.format(aFecha(isoFecha));
export const fechaLarga = (isoFecha: string) => larga.format(aFecha(isoFecha));
export const rango = (entrada: string, salida: string) => `${fechaCorta(entrada)} al ${fechaCorta(salida)}`;

export function noches(entrada: string, salida: string): number {
  return Math.round((aFecha(salida).getTime() - aFecha(entrada).getTime()) / 86_400_000);
}

export function hora12(hhmm: string | null | undefined): string {
  if (!hhmm) return '';
  const [h, m] = hhmm.split(':').map(Number);
  const sufijo = h >= 12 ? 'p. m.' : 'a. m.';
  const h12 = h % 12 === 0 ? 12 : h % 12;
  return `${h12}:${String(m).padStart(2, '0')} ${sufijo}`;
}

export const nombreEstado: Record<EstadoReserva, string> = {
  PENDIENTE: 'Pendiente de pago',
  CONFIRMADA: 'Confirmada',
  EN_CURSO: 'Hospedado',
  FINALIZADA: 'Finalizada',
  CANCELADA: 'Cancelada',
  NO_SHOW: 'No se presentó',
};

export const nombreRol: Record<Rol, string> = {
  HUESPED: 'Huésped',
  RECEPCIONISTA: 'Recepción',
  ADMINISTRADOR: 'Administración',
  PERSONAL_SERVICIO: 'Personal de servicio',
};

export const nombreTemporada: Record<string, string> = {
  BASE: 'Temporada base',
  ALTA: 'Temporada alta',
  PUENTES: 'Puentes y festivos',
};
