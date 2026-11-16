import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Aviso, Cargando } from '../components/Comunes';
import { Buscador, TarjetaDomo } from '../components/Domos';
import { dinero, hora12 } from '../components/formato';
import { PaisajeCocora } from '../components/Ilustraciones';
import { mensajeDeError } from '../services/api';
import { listarDomos, obtenerAlojamiento, politicaVigente } from '../services/sga';
import type { Alojamiento, Domo, Politica } from '../services/types';

export default function Inicio() {
  const [domos, setDomos] = useState<Domo[] | null>(null);
  const [alojamiento, setAlojamiento] = useState<Alojamiento | null>(null);
  const [politica, setPolitica] = useState<Politica | null>(null);
  const [error, setError] = useState('');

  useEffect(() => {
    listarDomos()
      .then((p) => setDomos(p.contenido))
      .catch((e) => setError(mensajeDeError(e)));
    obtenerAlojamiento().then(setAlojamiento).catch(() => undefined);
    politicaVigente().then(setPolitica).catch(() => undefined);
  }, []);

  return (
    <>
      <section className="portada">
        <PaisajeCocora />
        <div className="contenedor portada__contenido">
          <h1 className="portada__titulo">
            Duerme entre palmas de cera, bajo el cielo de Salento.
          </h1>
          <p className="portada__bajada">
            Cinco domos en la montaña del Quindío con jacuzzi a leña, malla catamarán y fogata propia. Elige tus
            fechas y mira cuáles están libres.
          </p>
          <Buscador />
        </div>
      </section>

      <section className="contenedor seccion">
        <div className="seccion__encabezado">
          <h2>Los domos</h2>
          <Link to="/domos" className="enlace-fuerte">
            Ver todos con tarifas
          </Link>
        </div>
        {error && <Aviso>{error}</Aviso>}
        {!domos && !error && <Cargando texto="Trayendo los domos…" />}
        {domos && (
          <div className="rejilla-domos">
            {domos.map((d) => (
              <TarjetaDomo key={d.idDomo} domo={d} />
            ))}
          </div>
        )}
      </section>

      {alojamiento && (
        <section className="banda-montana">
          <div className="contenedor antes-de-llegar">
            <div>
              <h2>Antes de llegar</h2>
              <p className="texto-amplio">{alojamiento.descripcion}</p>
              <p className="texto-suave">{alojamiento.direccion}</p>
            </div>
            <dl className="datos-estancia">
              <div>
                <dt>Llegada</dt>
                <dd>desde las {hora12(alojamiento.horaEntrada)}</dd>
              </div>
              <div>
                <dt>Salida</dt>
                <dd>hasta las {hora12(alojamiento.horaSalida)}</dd>
              </div>
              <div>
                <dt>Para confirmar</dt>
                <dd>
                  anticipo del {alojamiento.anticipoPorcentaje} % dentro de las {alojamiento.plazoConfirmacionHoras} horas
                  siguientes
                </dd>
              </div>
              <div>
                <dt>Niños</dt>
                <dd>menores de {alojamiento.umbralEdadFacturable} años no pagan alojamiento</dd>
              </div>
              <div>
                <dt>Depósito</dt>
                <dd>{dinero(alojamiento.deposito)} al llegar, se devuelve a la salida</dd>
              </div>
              <div>
                <dt>Mascotas</dt>
                <dd>{alojamiento.aceptaMascotas ? 'son bienvenidas' : 'no se admiten'}</dd>
              </div>
            </dl>
          </div>
          <div className="contenedor dos-columnas">
            <div>
              <h3>Para sumar a tu estancia</h3>
              <ul className="lista-servicios">
                {alojamiento.servicios.map((s) => (
                  <li key={s.codigo}>
                    <span>{s.nombre}</span>
                    <strong>
                      {dinero(s.valor)}
                      {s.porPersona ? ' por persona' : ''}
                    </strong>
                  </li>
                ))}
              </ul>
            </div>
            <div>
              <h3>Normas de convivencia</h3>
              <p>{alojamiento.normasConvivencia}</p>
              {politica && (
                <>
                  <h3>Si cancelas</h3>
                  <ul className="lista-politica">
                    {politica.tramos.map((t) => (
                      <li key={t.horasAntelacionMinima}>
                        {t.horasAntelacionMinima === 0
                          ? 'Con menos antelación'
                          : `Con ${Math.round(t.horasAntelacionMinima / 24)} días o más de antelación`}
                        : se retiene el {t.porcentajeRetencion} %
                      </li>
                    ))}
                    <li>Si no llegas: se retiene el {politica.retencionNoShow} %</li>
                  </ul>
                </>
              )}
            </div>
          </div>
        </section>
      )}
    </>
  );
}
