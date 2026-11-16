import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Aviso, Cargando } from '../components/Comunes';
import { Buscador, TarjetaDomo } from '../components/Domos';
import { dinero, hora12 } from '../components/formato';
import { FOTOS, Foto, urlFoto } from '../components/Fotos';
import { mensajeDeError } from '../services/api';
import { listarDomos, obtenerAlojamiento, politicaVigente } from '../services/sga';
import type { Alojamiento, Domo, Politica } from '../services/types';

const NOCHE = [
  {
    hora: '3:00 p. m.',
    titulo: 'Llegas y el domo ya te espera',
    texto: 'Subes por la vereda, dejas la maleta y lo primero que ves desde la cama es el valle abriéndose entre la niebla.',
    foto: FOTOS.cocoraNiebla,
    alt: 'Niebla sobre las montañas verdes del Quindío',
  },
  {
    hora: '6:30 p. m.',
    titulo: 'Se prende la fogata',
    texto: 'La leña está lista en tu zona privada. Se oyen los grillos, alguien pone música bajita y la conversación se alarga.',
    foto: FOTOS.fogataAmigos,
    alt: 'Amigos conversando alrededor de una fogata en el bosque',
  },
  {
    hora: '8:00 p. m.',
    titulo: 'Jacuzzi a leña bajo las estrellas',
    texto: 'Lo calentamos con tiempo según tu hora de llegada. El agua caliente, el aire frío de montaña y el cielo despejado.',
    foto: FOTOS.jacuzzi,
    alt: 'Jacuzzi de madera calentado con leña en medio del bosque',
  },
  {
    hora: '6:00 a. m.',
    titulo: 'Amanece entre las palmas de cera',
    texto: 'Un café de la región en la terraza mientras la neblina se levanta. El Valle de Cocora queda a pocos minutos.',
    foto: FOTOS.cocoraBruma,
    alt: 'Palmas de cera entre la bruma de la mañana',
  },
];

const ALREDEDOR = [
  {
    titulo: 'Valle de Cocora',
    texto: 'Las palmas de cera, el árbol nacional, crecen aquí hasta 60 metros. Hay caminatas de todos los niveles.',
    foto: FOTOS.cocoraCaballos,
    alt: 'Palmas de cera altísimas y caballos en el Valle de Cocora',
  },
  {
    titulo: 'Un Willys desde la plaza',
    texto: 'Los jeeps clásicos salen de la plaza de Salento hacia el valle. Ir de pie atrás es parte del paseo.',
    foto: FOTOS.willys,
    alt: 'Jeeps Willys de colores en la plaza de Salento',
  },
  {
    titulo: 'Calle Real de Salento',
    texto: 'Casas de bahareque con balcones de colores, artesanías y café de origen para llevar a casa.',
    foto: FOTOS.salentoCalle,
    alt: 'Calle de Salento con casas coloridas y balcones de madera',
  },
  {
    titulo: 'Cabalgatas por la montaña',
    texto: 'Recorridos a caballo por fincas cafeteras y miradores. Te ayudamos a reservar con guías de la zona.',
    foto: FOTOS.cabalgata,
    alt: 'Vista del valle desde un caballo en la montaña',
  },
];

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
        <picture className="portada__foto">
          <source media="(max-width: 700px)" srcSet={urlFoto(FOTOS.portada, 900, 1300)} />
          <img src={urlFoto(FOTOS.portada, 2000)} alt="Palmas de cera entre la niebla en el Valle de Cocora" fetchPriority="high" />
        </picture>
        <div className="contenedor portada__contenido">
          <p className="portada__lugar">Vereda Palestina, Salento, Quindío</p>
          <h1 className="portada__titulo">Duerme entre palmas de cera, bajo el cielo de Salento.</h1>
          <p className="portada__bajada">
            Cinco domos en la montaña, cada uno con su jacuzzi a leña, malla catamarán o fogata propia. Elige tus fechas
            y mira cuáles están libres.
          </p>
          <Buscador />
        </div>
      </section>

      <section className="contenedor seccion bienvenida">
        <div className="bienvenida__texto">
          <h2>Un lugar pequeño, a propósito</h2>
          <p className="texto-amplio">
            Somos solo cinco domos, separados entre sí para que cada grupo tenga su propio pedazo de montaña. No hay
            ruido de carretera ni filas en el desayuno: hay niebla en la mañana, colibríes en la tarde y un cielo lleno de
            estrellas cuando se apagan las luces.
          </p>
          <p>
            Te recibimos personalmente, te explicamos cómo prender la fogata y te contamos qué caminos tomar para llegar a
            Cocora antes que los buses de turistas.
          </p>
        </div>
        <div className="bienvenida__fotos">
          <Foto id={FOTOS.interiorDomo} alt="Techo de madera de un domo visto desde adentro" ancho={700} alto={900} className="bienvenida__alta" />
          <Foto id={FOTOS.malla} alt="Malla tejida para descansar al aire libre" ancho={600} alto={600} className="bienvenida__baja" />
        </div>
      </section>

      <section className="contenedor seccion">
        <div className="seccion__encabezado">
          <h2>Elige tu domo</h2>
          <Link to="/domos" className="enlace-fuerte">Ver tarifas por temporada</Link>
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

      <section className="noche">
        <div className="contenedor">
          <h2>Así se vive una noche aquí</h2>
          <ol className="noche__linea">
            {NOCHE.map((m) => (
              <li key={m.hora} className="momento">
                <Foto id={m.foto} alt={m.alt} ancho={640} alto={460} className="momento__foto" />
                <p className="momento__hora">{m.hora}</p>
                <h3>{m.titulo}</h3>
                <p>{m.texto}</p>
              </li>
            ))}
          </ol>
        </div>
      </section>

      <section className="contenedor seccion">
        <h2>Salento a la vuelta de la esquina</h2>
        <p className="texto-amplio">Estamos en plena zona cafetera, a pocos minutos del pueblo y del Valle de Cocora.</p>
        <div className="alrededor">
          {ALREDEDOR.map((a, i) => (
            <article key={a.titulo} className={`alrededor__item ${i === 0 ? 'alrededor__item--grande' : ''}`}>
              <Foto id={a.foto} alt={a.alt} ancho={i === 0 ? 1100 : 600} alto={i === 0 ? 800 : 440} />
              <div className="alrededor__texto">
                <h3>{a.titulo}</h3>
                <p>{a.texto}</p>
              </div>
            </article>
          ))}
        </div>
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

      <section className="cierre">
        <Foto id={FOTOS.fogataEstrellas} alt="Fogata bajo la Vía Láctea en la montaña" ancho={2000} alto={900} className="cierre__foto" />
        <div className="contenedor cierre__contenido">
          <h2>La fogata ya tiene tu nombre</h2>
          <p>Mira qué domos están libres para tus fechas y aparta el tuyo en dos minutos.</p>
          <Link className="boton boton--fogata" to="/buscar">Ver disponibilidad</Link>
        </div>
      </section>
    </>
  );
}
