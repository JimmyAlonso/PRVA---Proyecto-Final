import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import type { Domo } from '../services/types';
import { dinero, hoyIso, sumarDias } from './formato';
import { FotoDomo } from './Fotos';

export function dotacion(domo: Pick<Domo, 'tieneJacuzzi' | 'pesoMaximoMallaKg' | 'kitsFogata' | 'caracteristicas'>) {
  return {
    jacuzzi: domo.tieneJacuzzi,
    malla: (domo.pesoMaximoMallaKg ?? 0) > 0,
    fogata: (domo.kitsFogata ?? 0) > 0,
    proyector: domo.caracteristicas.some((c) => c.toLowerCase().includes('proyector')),
  };
}

export function tarifaDesde(domo: Domo): number {
  const valores = Object.values(domo.tarifasPorTemporada ?? {});
  return valores.length ? Math.min(...valores) : 0;
}

export function TarjetaDomo({ domo, destino }: { domo: Domo; destino?: string }) {
  const fuera = domo.estadoOperativo === 'FUERA_DE_SERVICIO';
  return (
    <article className="tarjeta-domo">
      <Link to={destino ?? `/domos/${domo.idDomo}`} className="tarjeta-domo__enlace">
        <div className="tarjeta-domo__marco">
          <FotoDomo idDomo={domo.idDomo} ancho={640} />
          {domo.tieneJacuzzi && <span className="sello">Jacuzzi a leña</span>}
        </div>
        <div className="tarjeta-domo__cuerpo">
          <div className="tarjeta-domo__titulo">
            <h3>{domo.nombre}</h3>
            <span className="codigo">{domo.idDomo}</span>
          </div>
          <p className="tarjeta-domo__capacidad">
            Hasta {domo.capacidadMaxima} personas, {domo.dormitorios} {domo.dormitorios === 1 ? 'dormitorio' : 'dormitorios'}
          </p>
          <ul className="etiquetas">
            {domo.caracteristicas.slice(0, 3).map((c) => (
              <li key={c}>{c}</li>
            ))}
          </ul>
          <p className="tarjeta-domo__precio">
            {fuera ? (
              <span className="texto-alerta">En mantenimiento por ahora</span>
            ) : (
              <>
                Desde <strong>{dinero(tarifaDesde(domo))}</strong> por persona la noche
              </>
            )}
          </p>
        </div>
      </Link>
    </article>
  );
}

/** Formulario de fechas y huéspedes; lleva a /buscar con los parámetros en la URL. */
export function Buscador({
  inicial,
  compacto = false,
}: {
  inicial?: { entrada?: string; salida?: string; personas?: number };
  compacto?: boolean;
}) {
  const navegar = useNavigate();
  const hoy = hoyIso();
  const [entrada, setEntrada] = useState(inicial?.entrada ?? sumarDias(hoy, 7));
  const [salida, setSalida] = useState(inicial?.salida ?? sumarDias(hoy, 9));
  const [personas, setPersonas] = useState(inicial?.personas ?? 2);

  const enviar = (e: FormEvent) => {
    e.preventDefault();
    navegar(`/buscar?entrada=${entrada}&salida=${salida}&personas=${personas}`);
  };

  return (
    <form className={`buscador ${compacto ? 'buscador--compacto' : ''}`} onSubmit={enviar}>
      <label>
        Llegada
        <input
          type="date"
          value={entrada}
          min={hoy}
          required
          onChange={(e) => {
            setEntrada(e.target.value);
            if (e.target.value >= salida) setSalida(sumarDias(e.target.value, 1));
          }}
        />
      </label>
      <label>
        Salida
        <input type="date" value={salida} min={sumarDias(entrada, 1)} required onChange={(e) => setSalida(e.target.value)} />
      </label>
      <label>
        Huéspedes
        <select value={personas} onChange={(e) => setPersonas(Number(e.target.value))}>
          {[1, 2, 3, 4].map((n) => (
            <option key={n} value={n}>
              {n} {n === 1 ? 'persona' : 'personas'}
            </option>
          ))}
        </select>
      </label>
      <button className="boton boton--fogata" type="submit">
        Ver domos libres
      </button>
    </form>
  );
}
