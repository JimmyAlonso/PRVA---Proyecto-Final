import { useEffect, useState } from 'react';
import { Aviso, Cargando } from '../components/Comunes';
import { Buscador, TarjetaDomo } from '../components/Domos';
import { aFecha, nombreTemporada } from '../components/formato';
import { mensajeDeError } from '../services/api';
import { listarDomos, listarTemporadas } from '../services/sga';
import type { Domo, Temporada } from '../services/types';

const mesCorto = new Intl.DateTimeFormat('es-CO', { day: 'numeric', month: 'short' });

export default function Catalogo() {
  const [domos, setDomos] = useState<Domo[] | null>(null);
  const [temporadas, setTemporadas] = useState<Temporada[]>([]);
  const [error, setError] = useState('');

  useEffect(() => {
    listarDomos()
      .then((p) => setDomos(p.contenido))
      .catch((e) => setError(mensajeDeError(e)));
    listarTemporadas()
      .then((p) => setTemporadas(p.contenido))
      .catch(() => undefined);
  }, []);

  return (
    <section className="contenedor pagina">
      <h1>Domos de JiyJu</h1>
      <p className="texto-amplio">
        Las tarifas son por persona que paga y por noche, y cambian según la temporada.
      </p>
      <Buscador compacto />
      {error && <Aviso>{error}</Aviso>}
      {!domos && !error && <Cargando />}
      {domos && (
        <div className="rejilla-domos">
          {domos.map((d) => (
            <TarjetaDomo key={d.idDomo} domo={d} />
          ))}
        </div>
      )}
      {temporadas.length > 0 && (
        <div className="temporadas">
          <h2>Temporadas</h2>
          <ul>
            {temporadas.map((t) => (
              <li key={t.id}>
                <strong>{nombreTemporada[t.id] ?? t.nombre}</strong>
                <span>
                  {t.base
                    ? 'El resto del año'
                    : t.periodos
                        .slice(0, 4)
                        .map((p) => `${mesCorto.format(aFecha(p.desde))} a ${mesCorto.format(aFecha(p.hasta))}`)
                        .join(', ') + (t.periodos.length > 4 ? ', entre otros' : '')}
                </span>
              </li>
            ))}
          </ul>
        </div>
      )}
    </section>
  );
}
