import { useEffect, useState } from 'react';
import { mensajeDeError } from '../services/api';
import { cotizar } from '../services/sga';
import type { Cotizacion } from '../services/types';
import { Aviso, Cargando } from './Comunes';
import { dinero, fechaCorta, hoyIso, nombreTemporada, sumarDias } from './formato';

/** Fechas de nacimiento representativas: adultos de 35 años y niños de 6 (la edad define si pagan). */
export function grupoComoFechas(adultos: number, ninos: number): string[] {
  const hoy = new Date();
  const adulto = `${hoy.getFullYear() - 35}-01-15`;
  const nino = `${hoy.getFullYear() - 6}-01-15`;
  return [...Array(adultos).fill(adulto), ...Array(ninos).fill(nino)];
}

export function DesgloseNoches({ cotizacion }: { cotizacion: Pick<Cotizacion, 'desglose' | 'valorTotalEstancia' | 'noches'> }) {
  return (
    <div className="desglose">
      <table>
        <thead>
          <tr>
            <th scope="col">Noche</th>
            <th scope="col">Temporada</th>
            <th scope="col">Pagan</th>
            <th scope="col" className="num">Subtotal</th>
          </tr>
        </thead>
        <tbody>
          {cotizacion.desglose.map((n) => (
            <tr key={n.fecha}>
              <td>{fechaCorta(n.fecha)}</td>
              <td>{nombreTemporada[n.temporada] ?? n.temporada}</td>
              <td>
                {n.ocupantesFacturables} × {dinero(n.tarifaPorOcupanteFacturable)}
                {n.recargoPersonaAdicional > 0 && <small> + recargo {dinero(n.recargoPersonaAdicional)}</small>}
              </td>
              <td className="num">{dinero(n.subtotal)}</td>
            </tr>
          ))}
        </tbody>
        <tfoot>
          <tr>
            <th scope="row" colSpan={3}>
              Total por {cotizacion.noches} {cotizacion.noches === 1 ? 'noche' : 'noches'}
            </th>
            <td className="num">{dinero(cotizacion.valorTotalEstancia)}</td>
          </tr>
        </tfoot>
      </table>
    </div>
  );
}

export interface EstanciaElegida {
  entrada: string;
  salida: string;
  adultos: number;
  ninos: number;
}

export function Cotizador({ idDomo, capacidad, estancia, cambiar, alReservar }: {
  idDomo: string;
  capacidad: number;
  estancia: EstanciaElegida;
  cambiar: (e: EstanciaElegida) => void;
  alReservar: () => void;
}) {
  const [cotizacion, setCotizacion] = useState<Cotizacion | null>(null);
  const [cargando, setCargando] = useState(false);
  const [error, setError] = useState('');
  const hoy = hoyIso();
  const total = estancia.adultos + estancia.ninos;

  useEffect(() => {
    if (!estancia.entrada || !estancia.salida || estancia.salida <= estancia.entrada) {
      setCotizacion(null);
      return;
    }
    let vigente = true;
    setCargando(true);
    setError('');
    cotizar(idDomo, estancia.entrada, estancia.salida, grupoComoFechas(estancia.adultos, estancia.ninos))
      .then((c) => vigente && setCotizacion(c))
      .catch((e) => {
        if (!vigente) return;
        setCotizacion(null);
        setError(mensajeDeError(e));
      })
      .finally(() => vigente && setCargando(false));
    return () => {
      vigente = false;
    };
  }, [idDomo, estancia.entrada, estancia.salida, estancia.adultos, estancia.ninos]);

  return (
    <div className="cotizador">
      <h2>Calcula tu estancia</h2>
      <div className="cotizador__campos">
        <label>
          Llegada
          <input
            type="date"
            min={hoy}
            value={estancia.entrada}
            onChange={(e) =>
              cambiar({
                ...estancia,
                entrada: e.target.value,
                salida: e.target.value >= estancia.salida ? sumarDias(e.target.value, 1) : estancia.salida,
              })
            }
          />
        </label>
        <label>
          Salida
          <input
            type="date"
            min={estancia.entrada ? sumarDias(estancia.entrada, 1) : hoy}
            value={estancia.salida}
            onChange={(e) => cambiar({ ...estancia, salida: e.target.value })}
          />
        </label>
        <label>
          Adultos
          <select value={estancia.adultos} onChange={(e) => cambiar({ ...estancia, adultos: Number(e.target.value) })}>
            {Array.from({ length: capacidad }, (_, i) => i + 1).map((n) => (
              <option key={n} value={n} disabled={n + estancia.ninos > capacidad}>
                {n}
              </option>
            ))}
          </select>
        </label>
        <label>
          Niños menores de 12
          <select value={estancia.ninos} onChange={(e) => cambiar({ ...estancia, ninos: Number(e.target.value) })}>
            {Array.from({ length: capacidad }, (_, i) => i).map((n) => (
              <option key={n} value={n} disabled={n + estancia.adultos > capacidad}>
                {n}
              </option>
            ))}
          </select>
        </label>
      </div>
      {total > capacidad && <Aviso>Este domo recibe máximo {capacidad} personas.</Aviso>}
      {cargando && <Cargando texto="Calculando…" />}
      {error && <Aviso>{error}</Aviso>}
      {cotizacion && !cargando && (
        <>
          <DesgloseNoches cotizacion={cotizacion} />
          <button className="boton boton--fogata boton--ancho" onClick={alReservar}>
            Reservar por {dinero(cotizacion.valorTotalEstancia)}
          </button>
        </>
      )}
    </div>
  );
}
