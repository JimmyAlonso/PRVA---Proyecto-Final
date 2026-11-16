import { useEffect, useMemo, useState } from 'react';
import { calendarioDomo } from '../services/sga';
import type { NocheCalendario } from '../services/types';
import { Cargando } from './Comunes';
import { hoyIso, iso } from './formato';

const nombreMes = new Intl.DateTimeFormat('es-CO', { month: 'long', year: 'numeric' });
const DIAS = ['lu', 'ma', 'mi', 'ju', 'vi', 'sá', 'do'];

function Mes({ anio, mes, noches, entrada, salida, elegir }: {
  anio: number;
  mes: number;
  noches: Map<string, NocheCalendario['estado']>;
  entrada?: string;
  salida?: string;
  elegir?: (fecha: string) => void;
}) {
  const primero = new Date(anio, mes, 1);
  const vacios = (primero.getDay() + 6) % 7;
  const total = new Date(anio, mes + 1, 0).getDate();
  const hoy = hoyIso();
  const celdas: (string | null)[] = [...Array(vacios).fill(null)];
  for (let d = 1; d <= total; d++) celdas.push(iso(new Date(anio, mes, d)));

  return (
    <div className="mes">
      <p className="mes__nombre">{(() => { const t = nombreMes.format(primero); return t.charAt(0).toUpperCase() + t.slice(1); })()}</p>
      <div className="mes__rejilla" role="grid">
        {DIAS.map((d) => (
          <span key={d} className="mes__dia-semana">
            {d}
          </span>
        ))}
        {celdas.map((fecha, i) => {
          if (!fecha) return <span key={`v${i}`} />;
          const estado = noches.get(fecha) ?? 'LIBRE';
          const pasado = fecha < hoy;
          const elegido = (entrada && fecha === entrada) || (salida && fecha === salida);
          const enRango = entrada && salida && fecha > entrada && fecha < salida;
          const clases = [
            'mes__noche',
            `mes__noche--${estado.toLowerCase()}`,
            pasado ? 'mes__noche--pasada' : '',
            elegido ? 'mes__noche--elegida' : '',
            enRango ? 'mes__noche--rango' : '',
          ].join(' ');
          const titulo = estado === 'LIBRE' ? 'Noche libre' : estado === 'RESERVADA' ? 'Noche reservada' : 'Domo bloqueado';
          return (
            <button
              type="button"
              key={fecha}
              className={clases}
              title={titulo}
              aria-label={`${fecha}: ${titulo}`}
              disabled={pasado || !elegir}
              onClick={() => elegir?.(fecha)}
            >
              {Number(fecha.slice(8))}
            </button>
          );
        })}
      </div>
    </div>
  );
}

export function CalendarioOcupacion({ idDomo, entrada, salida, elegir }: {
  idDomo: string;
  entrada?: string;
  salida?: string;
  elegir?: (fecha: string) => void;
}) {
  const [desplazamiento, setDesplazamiento] = useState(0);
  const [noches, setNoches] = useState<Map<string, NocheCalendario['estado']> | null>(null);

  const meses = useMemo(() => {
    const base = new Date();
    const a = new Date(base.getFullYear(), base.getMonth() + desplazamiento, 1);
    const b = new Date(a.getFullYear(), a.getMonth() + 1, 1);
    return [a, b];
  }, [desplazamiento]);

  useEffect(() => {
    const desde = iso(meses[0]);
    const hasta = iso(new Date(meses[1].getFullYear(), meses[1].getMonth() + 1, 1));
    setNoches(null);
    calendarioDomo(idDomo, desde, hasta)
      .then((lista) => setNoches(new Map(lista.map((n) => [n.fecha, n.estado]))))
      .catch(() => setNoches(new Map()));
  }, [idDomo, meses]);

  return (
    <div className="calendario">
      <div className="calendario__barra">
        <button type="button" className="boton boton--fantasma boton--pequeno" disabled={desplazamiento === 0} onClick={() => setDesplazamiento((d) => d - 1)}>
          Meses anteriores
        </button>
        <ul className="leyenda">
          <li><span className="muestra muestra--libre" /> Libre</li>
          <li><span className="muestra muestra--reservada" /> Reservada</li>
          <li><span className="muestra muestra--bloqueada" /> Bloqueada</li>
        </ul>
        <button type="button" className="boton boton--fantasma boton--pequeno" onClick={() => setDesplazamiento((d) => d + 1)}>
          Meses siguientes
        </button>
      </div>
      {!noches ? (
        <Cargando texto="Consultando ocupación…" />
      ) : (
        <div className="calendario__meses">
          {meses.map((m) => (
            <Mes key={m.toISOString()} anio={m.getFullYear()} mes={m.getMonth()} noches={noches} entrada={entrada} salida={salida} elegir={elegir} />
          ))}
        </div>
      )}
      {elegir && <p className="texto-suave calendario__ayuda">Toca una noche libre para elegir la llegada y luego la salida.</p>}
    </div>
  );
}
