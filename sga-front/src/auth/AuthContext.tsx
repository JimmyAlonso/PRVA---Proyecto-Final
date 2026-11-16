import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { guardarSesion, leerSesionGuardada, type SesionGuardada } from '../services/api';
import { iniciarSesion } from '../services/sga';
import type { Rol, Usuario } from '../services/types';

interface ContextoSesion {
  usuario: Usuario | null;
  entrar: (correo: string, contrasena: string) => Promise<Usuario>;
  salir: () => void;
  tieneRol: (...roles: Rol[]) => boolean;
}

const Contexto = createContext<ContextoSesion | null>(null);

export function ProveedorSesion({ children }: { children: ReactNode }) {
  const [sesion, setSesion] = useState<SesionGuardada | null>(() => leerSesionGuardada());

  useEffect(() => {
    const alExpirar = () => setSesion(null);
    window.addEventListener('jiyju:sesion-expirada', alExpirar);
    return () => window.removeEventListener('jiyju:sesion-expirada', alExpirar);
  }, []);

  const entrar = useCallback(async (correo: string, contrasena: string) => {
    const respuesta = await iniciarSesion(correo.trim(), contrasena);
    const nueva: SesionGuardada = { token: respuesta.token, expiraEn: respuesta.expiraEn, usuario: respuesta.usuario };
    guardarSesion(nueva);
    setSesion(nueva);
    return respuesta.usuario;
  }, []);

  const salir = useCallback(() => {
    guardarSesion(null);
    setSesion(null);
  }, []);

  const valor = useMemo<ContextoSesion>(
    () => ({
      usuario: sesion?.usuario ?? null,
      entrar,
      salir,
      tieneRol: (...roles: Rol[]) => !!sesion && roles.includes(sesion.usuario.rol),
    }),
    [sesion, entrar, salir],
  );

  return <Contexto.Provider value={valor}>{children}</Contexto.Provider>;
}

export function useSesion(): ContextoSesion {
  const ctx = useContext(Contexto);
  if (!ctx) throw new Error('useSesion debe usarse dentro de ProveedorSesion');
  return ctx;
}

/** Página de inicio según el rol, tras iniciar sesión. */
export function inicioSegunRol(rol: Rol): string {
  switch (rol) {
    case 'RECEPCIONISTA':
    case 'ADMINISTRADOR':
      return '/panel';
    case 'PERSONAL_SERVICIO':
      return '/preparacion';
    default:
      return '/mis-reservas';
  }
}
