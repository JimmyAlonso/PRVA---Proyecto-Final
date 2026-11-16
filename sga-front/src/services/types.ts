export type Rol = 'HUESPED' | 'RECEPCIONISTA' | 'ADMINISTRADOR' | 'PERSONAL_SERVICIO';
export type EstadoReserva = 'PENDIENTE' | 'CONFIRMADA' | 'EN_CURSO' | 'FINALIZADA' | 'CANCELADA' | 'NO_SHOW';
export type CanalOrigen = 'PORTAL' | 'DIRECTO' | 'EXTERNO';
export type EstadoOperativo = 'PREPARADO' | 'OCUPADO' | 'PENDIENTE_PREPARACION' | 'EN_PREPARACION' | 'FUERA_DE_SERVICIO';
export type MedioPago = 'TRANSFERENCIA' | 'TARJETA' | 'EFECTIVO';

export interface Pagina<T> {
  contenido: T[];
  pagina: number;
  tamano: number;
  totalElementos: number;
  totalPaginas: number;
}

export interface Usuario {
  id: string;
  nombre: string;
  correo: string;
  telefono: string;
  documento: string;
  fechaNacimiento: string;
  rol: Rol;
  fotoPerfil?: string | null;
}

export interface TokenResponse {
  token: string;
  tipo: string;
  expiraEn: string;
  usuario: Usuario;
}

export interface Servicio {
  codigo: string;
  nombre: string;
  valor: number;
  porPersona: boolean;
}

export interface Alojamiento {
  nombre: string;
  descripcion: string;
  ciudad: string;
  direccion: string;
  latitud: string;
  longitud: string;
  horaEntrada: string;
  horaSalida: string;
  normasConvivencia: string;
  umbralEdadFacturable: number;
  tiempoPreparacionHoras: number;
  plazoConfirmacionHoras: number;
  horaLimiteNoShow: string;
  anticipoPorcentaje: number;
  deposito: number;
  aceptaMascotas: boolean;
  servicios: Servicio[];
}

export interface Domo {
  idDomo: string;
  nombre: string;
  descripcion: string;
  dormitorios: number;
  capacidadMaxima: number;
  caracteristicas: string[];
  tieneJacuzzi: boolean;
  horasPrecalentamientoJacuzzi?: number | null;
  pesoMaximoMallaKg?: number | null;
  kitsFogata?: number | null;
  estadoOperativo: EstadoOperativo;
  activo: boolean;
  tarifasPorTemporada: Record<string, number>;
}

export interface DetalleNoche {
  fecha: string;
  temporada: string;
  tarifaPorOcupanteFacturable: number;
  ocupantesFacturables: number;
  recargoPersonaAdicional: number;
  subtotal: number;
}

export interface Cotizacion {
  idDomo: string;
  nombre: string;
  capacidadMaxima: number;
  tieneJacuzzi: boolean;
  caracteristicas: string[];
  noches: number;
  recargoOcupanteAdicional: number;
  valorTotalEstancia: number;
  moneda: string;
  desglose: DetalleNoche[];
}

export interface NocheCalendario {
  fecha: string;
  estado: 'LIBRE' | 'RESERVADA' | 'BLOQUEADA';
  referencia?: string | null;
}

export interface Temporada {
  id: string;
  nombre: string;
  base: boolean;
  periodos: { desde: string; hasta: string }[];
}

export interface Politica {
  version: number;
  vigenteDesde: string;
  tramos: { horasAntelacionMinima: number; porcentajeRetencion: number }[];
  retencionNoShow: number;
}

export interface ReservaResumen {
  codigo: string;
  estado: EstadoReserva;
  canalOrigen: CanalOrigen;
  idDomo: string;
  fechaEntrada: string;
  fechaSalida: string;
  nombreTitular: string;
  totalOcupantes: number;
  valorTotal: number;
}

export interface Ocupante {
  documento: string;
  nombre: string;
  fechaNacimiento: string;
  facturable?: boolean;
}

export interface ReservaDetalle {
  codigo: string;
  estado: EstadoReserva;
  canalOrigen: CanalOrigen;
  canalExterno?: string | null;
  idDomo: string;
  fechaEntrada: string;
  fechaSalida: string;
  noches: number;
  horaEstimadaLlegada?: string | null;
  titular: { documento: string; nombre: string; correo: string; telefono: string };
  ocupantes: Ocupante[];
  totalOcupantes: number;
  valorTotal: number;
  moneda: string;
  desglose: DetalleNoche[];
  versionPoliticaCancelacion: number;
  momentoCreacion: string;
  motivoCancelacion?: string | null;
}

export interface Movimientos {
  fecha: string;
  llegadas: ReservaResumen[];
  salidas: ReservaResumen[];
}

export interface Folio {
  codigoFolio: string;
  codigoReserva: string;
  cargos: { concepto: string; valor: number; esInverso: boolean; fecha: string; descripcion?: string | null }[];
  pagos: { idPago: string; monto: number; medio: MedioPago; fecha: string }[];
  totalCargos: number;
  totalPagos: number;
  saldo: number;
  depositoRegistrado: boolean;
  cerrado: boolean;
  autorizadoPor?: string | null;
  motivoAutorizacion?: string | null;
}

export interface CrearReservaRequest {
  idDomo: string;
  fechaEntrada: string;
  fechaSalida: string;
  canalOrigen: 'PORTAL' | 'DIRECTO';
  horaEstimadaLlegada?: string;
  titular: { documento: string; nombre: string; fechaNacimiento: string; correo: string; telefono: string };
  ocupantes: Ocupante[];
}

export interface RegistroRequest {
  nombre: string;
  correo: string;
  contrasena: string;
  telefono: string;
  documento: string;
  fechaNacimiento: string;
}
