import { useEffect, useState, type ReactNode } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useSesion } from '../auth/AuthContext';
import { escucharLentitud } from '../services/api';
import type { EstadoReserva, Rol } from '../services/types';
import { nombreEstado } from './formato';

export function Cargando({ texto = 'Cargando…' }: { texto?: string }) {
  return (
    <div className="cargando" role="status">
      <span className="cargando__punto" aria-hidden="true" />
      {texto}
    </div>
  );
}

export function Aviso({ tipo = 'error', children }: { tipo?: 'error' | 'exito' | 'info'; children: ReactNode }) {
  return (
    <div className={`aviso aviso--${tipo}`} role={tipo === 'error' ? 'alert' : 'status'}>
      {children}
    </div>
  );
}

export function EstadoReservaChip({ estado }: { estado: EstadoReserva }) {
  return <span className={`chip chip--${estado.toLowerCase()}`}>{nombreEstado[estado]}</span>;
}

/** Banda fija que avisa cuando el backend de Render se está despertando (plan gratuito). */
export function AvisoServidorDespertando() {
  const [lento, setLento] = useState(false);
  useEffect(() => escucharLentitud(setLento), []);
  if (!lento) return null;
  return (
    <div className="despertando" role="status">
      <span className="cargando__punto" aria-hidden="true" />
      Estamos despertando el servidor. La primera consulta del día puede tardar hasta un minuto.
    </div>
  );
}

/** Ruta protegida: exige sesión y, si se indican, uno de los roles. */
export function Protegida({ roles, children }: { roles?: Rol[]; children: ReactNode }) {
  const { usuario } = useSesion();
  const ubicacion = useLocation();
  if (!usuario) {
    return <Navigate to="/ingresar" replace state={{ volverA: ubicacion.pathname + ubicacion.search }} />;
  }
  if (roles && !roles.includes(usuario.rol)) {
    return (
      <section className="contenedor pagina-estrecha">
        <h1>Esta sección no es para tu cuenta</h1>
        <p className="texto-suave">Tu rol no tiene acceso aquí. Usa el menú para ir a las secciones disponibles.</p>
      </section>
    );
  }
  return <>{children}</>;
}

export function Paginador({ pagina, totalPaginas, cambiar }: { pagina: number; totalPaginas: number; cambiar: (p: number) => void }) {
  if (totalPaginas <= 1) return null;
  return (
    <nav className="paginador" aria-label="Paginación">
      <button className="boton boton--fantasma" disabled={pagina === 0} onClick={() => cambiar(pagina - 1)}>
        Anterior
      </button>
      <span>
        Página {pagina + 1} de {totalPaginas}
      </span>
      <button className="boton boton--fantasma" disabled={pagina + 1 >= totalPaginas} onClick={() => cambiar(pagina + 1)}>
        Siguiente
      </button>
    </nav>
  );
}
