import { api } from './api';
import type {
  Alojamiento, Cotizacion, CrearReservaRequest, Domo, EstadoReserva, Folio, MedioPago, Movimientos,
  NocheCalendario, Pagina, Politica, RegistroRequest, ReservaDetalle, ReservaResumen, Temporada, TokenResponse,
  Rol, Usuario,
} from './types';

// --- Público
export const obtenerAlojamiento = () => api.get<Alojamiento>('/alojamiento').then((r) => r.data);
export const listarDomos = (pagina = 0) =>
  api.get<Pagina<Domo>>('/domos', { params: { pagina } }).then((r) => r.data);
export const obtenerDomo = (id: string) => api.get<Domo>(`/domos/${id}`).then((r) => r.data);
export const listarTemporadas = () =>
  api.get<Pagina<Temporada>>('/temporadas', { params: { pagina: 0 } }).then((r) => r.data);
export const politicaVigente = () => api.get<Politica>('/politica-cancelacion/vigente').then((r) => r.data);

export const calendarioDomo = (id: string, desde: string, hasta: string) =>
  api.get<NocheCalendario[]>(`/domos/${id}/calendario`, { params: { desde, hasta } }).then((r) => r.data);

export const cotizar = (id: string, fechaEntrada: string, fechaSalida: string, fechasNacimiento: string[]) =>
  api.post<Cotizacion>(`/domos/${id}/cotizacion`, { fechaEntrada, fechaSalida, fechasNacimiento }).then((r) => r.data);

export interface FiltroDisponibles {
  fechaEntrada: string;
  fechaSalida: string;
  numeroOcupantes?: number;
  fechasNacimiento?: string[];
  conJacuzzi?: boolean;
  precioMaximo?: number;
}

export const buscarDisponibles = (f: FiltroDisponibles, pagina = 0) =>
  api
    .get<Pagina<Cotizacion>>('/domos/disponibles', {
      params: {
        fechaEntrada: f.fechaEntrada,
        fechaSalida: f.fechaSalida,
        numeroOcupantes: f.fechasNacimiento?.length ? undefined : f.numeroOcupantes,
        fechasNacimiento: f.fechasNacimiento?.length ? f.fechasNacimiento.join(',') : undefined,
        conJacuzzi: f.conJacuzzi || undefined,
        precioMaximo: f.precioMaximo || undefined,
        pagina,
      },
    })
    .then((r) => r.data);

// --- Autenticación
export const iniciarSesion = (correo: string, contrasena: string) =>
  api.post<TokenResponse>('/auth/login', { correo, contrasena }).then((r) => r.data);
export const registrarse = (datos: RegistroRequest) => api.post<Usuario>('/auth/registro', datos).then((r) => r.data);
export const miPerfil = () => api.get<Usuario>('/usuarios/me').then((r) => r.data);

// --- Reservas
export const crearReserva = (datos: CrearReservaRequest) =>
  api.post<ReservaDetalle>('/reservas', datos).then((r) => r.data);
export const misReservas = (pagina = 0) =>
  api.get<Pagina<ReservaResumen>>('/reservas/mias', { params: { pagina } }).then((r) => r.data);
export const obtenerReserva = (codigo: string) => api.get<ReservaDetalle>(`/reservas/${codigo}`).then((r) => r.data);
export const listarReservas = (filtros: { estado?: EstadoReserva | ''; idDomo?: string; documentoTitular?: string }, pagina = 0) =>
  api
    .get<Pagina<ReservaResumen>>('/reservas', {
      params: {
        estado: filtros.estado || undefined,
        idDomo: filtros.idDomo || undefined,
        documentoTitular: filtros.documentoTitular?.trim() || undefined,
        pagina,
      },
    })
    .then((r) => r.data);
export const movimientosDelDia = (fecha?: string) =>
  api.get<Movimientos>('/reservas/movimientos', { params: { fecha } }).then((r) => r.data);

export const confirmarReserva = (codigo: string, horaEstimadaLlegada?: string) =>
  api
    .post<ReservaDetalle>(`/reservas/${codigo}/confirmar`, horaEstimadaLlegada ? { horaEstimadaLlegada } : {})
    .then((r) => r.data);
export const cancelarReserva = (codigo: string, motivo: string) =>
  api.post<ReservaDetalle>(`/reservas/${codigo}/cancelar`, { motivo }).then((r) => r.data);
export const registrarLlegada = (codigo: string) =>
  api.post<ReservaDetalle>(`/reservas/${codigo}/check-in`).then((r) => r.data);
export const registrarSalida = (codigo: string, motivoAutorizacion?: string) =>
  api
    .post<ReservaDetalle>(`/reservas/${codigo}/check-out`, motivoAutorizacion ? { motivoAutorizacion } : {})
    .then((r) => r.data);
export const declararNoShow = (codigo: string) =>
  api.post<ReservaDetalle>(`/reservas/${codigo}/no-show`).then((r) => r.data);

// --- Folio
export const obtenerFolio = (codigo: string) => api.get<Folio>(`/reservas/${codigo}/folio`).then((r) => r.data);
export const registrarPago = (codigo: string, monto: number, medio: MedioPago) =>
  api.post<Folio>(`/reservas/${codigo}/folio/pagos`, { monto, medio }).then((r) => r.data);
export const registrarServicio = (codigo: string, codigoServicio: string, cantidad: number) =>
  api.post<Folio>(`/reservas/${codigo}/folio/servicios`, { codigoServicio, cantidad }).then((r) => r.data);
export const registrarDeposito = (codigo: string) =>
  api.post<Folio>(`/reservas/${codigo}/folio/deposito`).then((r) => r.data);
export const devolverDeposito = (codigo: string) =>
  api.post<Folio>(`/reservas/${codigo}/folio/deposito/devolucion`).then((r) => r.data);

// --- Operación diaria (personal de servicio)
export const pendientesPreparacion = () =>
  api.get<Pagina<Domo>>('/domos/pendientes-preparacion', { params: { pagina: 0 } }).then((r) => r.data);
export const iniciarPreparacion = (id: string) => api.post<Domo>(`/domos/${id}/iniciar-preparacion`).then((r) => r.data);
export const marcarPreparado = (id: string) => api.post<Domo>(`/domos/${id}/marcar-preparado`).then((r) => r.data);

// --- Administración de usuarios (solo ADMINISTRADOR)
export interface CrearUsuarioRequest {
  nombre: string;
  correo: string;
  contrasena: string;
  telefono: string;
  documento: string;
  fechaNacimiento: string;
  rol: Rol;
}
export const listarUsuarios = (pagina = 0) =>
  api.get<Pagina<Usuario>>('/usuarios', { params: { pagina } }).then((r) => r.data);
export const crearUsuario = (datos: CrearUsuarioRequest) => api.post<Usuario>('/usuarios', datos).then((r) => r.data);
export const asignarRol = (id: string, rol: Rol) => api.put<Usuario>(`/usuarios/${id}/rol`, { rol }).then((r) => r.data);
export const eliminarUsuario = (id: string) => api.delete(`/usuarios/${id}`).then((r) => r.data);
