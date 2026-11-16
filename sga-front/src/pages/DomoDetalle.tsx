import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { CalendarioOcupacion } from '../components/Calendario';
import { Aviso, Cargando } from '../components/Comunes';
import { Cotizador, type EstanciaElegida } from '../components/Cotizador';
import { dotacion } from '../components/Domos';
import { dinero, hoyIso, nombreTemporada, sumarDias } from '../components/formato';
import { FOTOS, Foto, fotoDeDomo, urlFoto } from '../components/Fotos';
import { mensajeDeError } from '../services/api';
import { obtenerDomo } from '../services/sga';
import type { Domo } from '../services/types';

export default function DomoDetalle() {
  const { id = '' } = useParams();
  const [parametros] = useSearchParams();
  const navegar = useNavigate();
  const [domo, setDomo] = useState<Domo | null>(null);
  const [error, setError] = useState('');
  const hoy = hoyIso();
  const [estancia, setEstancia] = useState<EstanciaElegida>({
    entrada: parametros.get('entrada') ?? sumarDias(hoy, 7),
    salida: parametros.get('salida') ?? sumarDias(hoy, 9),
    adultos: Number(parametros.get('adultos') ?? parametros.get('personas') ?? 2) || 2,
    ninos: Number(parametros.get('ninos') ?? 0) || 0,
  });

  useEffect(() => {
    obtenerDomo(id)
      .then(setDomo)
      .catch((e) => setError(mensajeDeError(e)));
  }, [id]);

  // Clic en el calendario: primero la llegada, luego la salida
  const [eligiendoSalida, setEligiendoSalida] = useState(false);
  const elegirEnCalendario = (fecha: string) => {
    if (!eligiendoSalida || fecha <= estancia.entrada) {
      setEstancia({ ...estancia, entrada: fecha, salida: sumarDias(fecha, 1) });
      setEligiendoSalida(true);
    } else {
      setEstancia({ ...estancia, salida: fecha });
      setEligiendoSalida(false);
    }
  };

  const reservar = () => {
    navegar(
      `/reservar/${id}?entrada=${estancia.entrada}&salida=${estancia.salida}&adultos=${estancia.adultos}&ninos=${estancia.ninos}`,
    );
  };

  if (error) {
    return (
      <section className="contenedor pagina">
        <Aviso>{error}</Aviso>
        <Link to="/domos">Volver a los domos</Link>
      </section>
    );
  }
  if (!domo) return <section className="contenedor pagina"><Cargando /></section>;

  const dot = dotacion(domo);
  const galeria = [
    dot.jacuzzi && { id: FOTOS.jacuzzi, alt: 'Jacuzzi de madera calentado con leña', pie: 'Jacuzzi a leña' },
    dot.fogata && { id: FOTOS.fogata, alt: 'Fogata encendida en la noche', pie: 'Fogata privada' },
    dot.malla && { id: FOTOS.malla, alt: 'Malla tejida para descansar al aire libre', pie: 'Malla catamarán' },
    dot.proyector && { id: FOTOS.domoNoche, alt: 'Domo iluminado en la noche', pie: 'Cine en el domo' },
    { id: FOTOS.interiorDomo, alt: 'Techo de madera del domo visto desde adentro', pie: 'Así se ve el techo desde la cama' },
    { id: FOTOS.estrellas, alt: 'Cielo estrellado entre los árboles', pie: 'El cielo de la vereda' },
  ]
    .filter((g): g is { id: string; alt: string; pie: string } => Boolean(g))
    .slice(0, 4);
  return (
    <section className="contenedor pagina">
      <Link to="/domos" className="migas">Todos los domos</Link>
      <div className="detalle-domo">
        <div className="detalle-domo__principal">
          <div className="galeria">
            <img
              className="galeria__principal"
              src={urlFoto(fotoDeDomo(domo.idDomo).id, 1400, 900)}
              alt={fotoDeDomo(domo.idDomo).texto}
            />
            {galeria.map((g) => (
              <figure key={g.id} className="galeria__item">
                <Foto id={g.id} alt={g.alt} ancho={520} alto={380} />
                <figcaption>{g.pie}</figcaption>
              </figure>
            ))}
          </div>
          <div className="detalle-domo__titulo">
            <h1>{domo.nombre}</h1>
            <span className="codigo codigo--grande">{domo.idDomo}</span>
          </div>
          <p className="texto-amplio">{domo.descripcion}</p>
          {domo.estadoOperativo === 'FUERA_DE_SERVICIO' && (
            <Aviso tipo="info">Este domo está en mantenimiento. Puedes reservar fechas futuras si aparecen libres.</Aviso>
          )}
          <dl className="ficha">
            <div><dt>Capacidad</dt><dd>{domo.capacidadMaxima} personas</dd></div>
            <div><dt>Dormitorios</dt><dd>{domo.dormitorios}</dd></div>
            {dot.jacuzzi && (
              <div>
                <dt>Jacuzzi</dt>
                <dd>Sí, se calienta en {domo.horasPrecalentamientoJacuzzi ?? 3} horas</dd>
              </div>
            )}
            {dot.malla && <div><dt>Malla catamarán</dt><dd>hasta {domo.pesoMaximoMallaKg} kg</dd></div>}
            {dot.fogata && <div><dt>Fogata</dt><dd>{domo.kitsFogata} {domo.kitsFogata === 1 ? 'kit' : 'kits'} de leña incluidos</dd></div>}
          </dl>
          <ul className="etiquetas etiquetas--grandes">
            {domo.caracteristicas.map((c) => <li key={c}>{c}</li>)}
          </ul>
          <h2>Tarifas por persona y noche</h2>
          <ul className="tarifas">
            {Object.entries(domo.tarifasPorTemporada).map(([t, v]) => (
              <li key={t}>
                <span>{nombreTemporada[t] ?? t}</span>
                <strong>{dinero(v)}</strong>
              </li>
            ))}
          </ul>
          <h2>Ocupación</h2>
          <CalendarioOcupacion idDomo={domo.idDomo} entrada={estancia.entrada} salida={estancia.salida} elegir={elegirEnCalendario} />
        </div>
        <aside className="detalle-domo__lateral">
          <Cotizador idDomo={domo.idDomo} capacidad={domo.capacidadMaxima} estancia={estancia} cambiar={setEstancia} alReservar={reservar} />
        </aside>
      </div>
    </section>
  );
}
