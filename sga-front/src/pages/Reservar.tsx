import { useEffect, useState, type FormEvent } from 'react';
import { Link, useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { useSesion } from '../auth/AuthContext';
import { Aviso, Cargando } from '../components/Comunes';
import { grupoComoFechas } from '../components/Cotizador';
import { dotacion } from '../components/Domos';
import { dinero, hora12, hoyIso, noches, rango, sumarDias } from '../components/formato';
import { RetratoDomo } from '../components/Ilustraciones';
import { mensajeDeError } from '../services/api';
import { cotizar, crearReserva, obtenerAlojamiento, obtenerDomo } from '../services/sga';
import type { Alojamiento, Cotizacion, Domo, Ocupante } from '../services/types';

const vacio = (): Ocupante => ({ documento: '', nombre: '', fechaNacimiento: '' });

export default function Reservar() {
  const { id = '' } = useParams();
  const [p] = useSearchParams();
  const navegar = useNavigate();
  const { usuario } = useSesion();
  const esHuesped = usuario?.rol === 'HUESPED';
  const hoy = hoyIso();

  const [entrada, setEntrada] = useState(p.get('entrada') ?? sumarDias(hoy, 7));
  const [salida, setSalida] = useState(p.get('salida') ?? sumarDias(hoy, 9));
  const adultos = Math.max(1, Number(p.get('adultos') ?? 2) || 1);
  const ninos = Math.max(0, Number(p.get('ninos') ?? 0) || 0);

  const [domo, setDomo] = useState<Domo | null>(null);
  const [alojamiento, setAlojamiento] = useState<Alojamiento | null>(null);
  const [cotizacion, setCotizacion] = useState<Cotizacion | null>(null);
  const [titular, setTitular] = useState({
    documento: esHuesped ? usuario?.documento ?? '' : '',
    nombre: esHuesped ? usuario?.nombre ?? '' : '',
    fechaNacimiento: esHuesped ? usuario?.fechaNacimiento ?? '' : '',
    correo: esHuesped ? usuario?.correo ?? '' : '',
    telefono: esHuesped ? usuario?.telefono ?? '' : '',
  });
  const [acompanantes, setAcompanantes] = useState<Ocupante[]>(() =>
    Array.from({ length: adultos + ninos - 1 }, vacio),
  );
  const [hora, setHora] = useState('16:00');
  const [error, setError] = useState('');
  const [enviando, setEnviando] = useState(false);

  useEffect(() => {
    obtenerDomo(id).then(setDomo).catch((e) => setError(mensajeDeError(e)));
    obtenerAlojamiento().then(setAlojamiento).catch(() => undefined);
  }, [id]);

  // Solo se vuelve a cotizar cuando cambian las fechas o las fechas de nacimiento del grupo
  const claveGrupo = [titular.fechaNacimiento, ...acompanantes.map((a) => a.fechaNacimiento)].join(',');
  useEffect(() => {
    if (salida <= entrada) return;
    const fechas = claveGrupo.split(',').map((f, i) => {
      if (f) return f;
      const esAdulto = i < adultos;
      return grupoComoFechas(esAdulto ? 1 : 0, esAdulto ? 0 : 1)[0];
    });
    const temporizador = window.setTimeout(() => {
      cotizar(id, entrada, salida, fechas)
        .then(setCotizacion)
        .catch(() => setCotizacion(null));
    }, 400);
    return () => window.clearTimeout(temporizador);
  }, [id, entrada, salida, claveGrupo, adultos]);

  const cambiarAcompanante = (i: number, k: keyof Ocupante, v: string) =>
    setAcompanantes((lista) => lista.map((a, j) => (j === i ? { ...a, [k]: v } : a)));

  const capacidad = domo?.capacidadMaxima ?? 4;
  const totalPersonas = 1 + acompanantes.length;

  const enviar = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    setEnviando(true);
    try {
      const reserva = await crearReserva({
        idDomo: id,
        fechaEntrada: entrada,
        fechaSalida: salida,
        canalOrigen: esHuesped ? 'PORTAL' : 'DIRECTO',
        horaEstimadaLlegada: hora || undefined,
        titular: { ...titular, documento: titular.documento.trim(), nombre: titular.nombre.trim() },
        ocupantes: acompanantes.map((a) => ({ ...a, documento: a.documento.trim(), nombre: a.nombre.trim() })),
      });
      navegar(`/reservas/${reserva.codigo}`, { state: { creada: true } });
    } catch (err) {
      setError(mensajeDeError(err));
    } finally {
      setEnviando(false);
    }
  };

  if (!domo && !error) return <section className="contenedor pagina"><Cargando /></section>;

  const n = noches(entrada, salida);
  return (
    <section className="contenedor pagina">
      <Link to={`/domos/${id}`} className="migas">Volver al domo</Link>
      <h1>Reserva tu estancia</h1>
      <div className="reservar">
        <form className="formulario reservar__formulario" onSubmit={enviar}>
          <fieldset>
            <legend>Fechas</legend>
            <div className="formulario__par">
              <label>
                Llegada
                <input type="date" min={hoy} value={entrada} required onChange={(e) => {
                  setEntrada(e.target.value);
                  if (e.target.value >= salida) setSalida(sumarDias(e.target.value, 1));
                }} />
              </label>
              <label>
                Salida
                <input type="date" min={sumarDias(entrada, 1)} value={salida} required onChange={(e) => setSalida(e.target.value)} />
              </label>
            </div>
            <label>
              Hora estimada de llegada
              <input type="time" value={hora} min={alojamiento?.horaEntrada} onChange={(e) => setHora(e.target.value)} />
              {alojamiento && (
                <small className="texto-suave">
                  La llegada es desde las {hora12(alojamiento.horaEntrada)}.
                  {domo?.tieneJacuzzi && ` Con tu hora de llegada preparamos el jacuzzi (${domo.horasPrecalentamientoJacuzzi ?? 3} horas antes).`}
                </small>
              )}
            </label>
          </fieldset>

          <fieldset>
            <legend>Titular de la reserva</legend>
            {esHuesped && <p className="texto-suave">La reserva queda a tu nombre, con los datos de tu cuenta.</p>}
            <div className="formulario__par">
              <label>
                Nombre completo
                <input required readOnly={esHuesped} value={titular.nombre} onChange={(e) => setTitular({ ...titular, nombre: e.target.value })} />
              </label>
              <label>
                Documento
                <input required minLength={5} maxLength={15} readOnly={esHuesped} value={titular.documento} onChange={(e) => setTitular({ ...titular, documento: e.target.value })} />
              </label>
            </div>
            <div className="formulario__trio">
              <label>
                Fecha de nacimiento
                <input type="date" required max={hoy} readOnly={esHuesped} value={titular.fechaNacimiento} onChange={(e) => setTitular({ ...titular, fechaNacimiento: e.target.value })} />
              </label>
              <label>
                Correo
                <input type="email" required readOnly={esHuesped} value={titular.correo} onChange={(e) => setTitular({ ...titular, correo: e.target.value })} />
              </label>
              <label>
                Teléfono
                <input type="tel" required readOnly={esHuesped} value={titular.telefono} onChange={(e) => setTitular({ ...titular, telefono: e.target.value })} />
              </label>
            </div>
          </fieldset>

          <fieldset>
            <legend>Acompañantes</legend>
            {acompanantes.length === 0 && <p className="texto-suave">Viajas solo. Puedes agregar acompañantes si lo necesitas.</p>}
            {acompanantes.map((a, i) => (
              <div className="acompanante" key={i}>
                <p className="acompanante__titulo">
                  Persona {i + 2}
                  <button type="button" className="boton boton--fantasma boton--pequeno" onClick={() => setAcompanantes((l) => l.filter((_, j) => j !== i))}>
                    Quitar
                  </button>
                </p>
                <div className="formulario__trio">
                  <label>
                    Nombre
                    <input required value={a.nombre} onChange={(e) => cambiarAcompanante(i, 'nombre', e.target.value)} />
                  </label>
                  <label>
                    Documento
                    <input required minLength={5} maxLength={15} value={a.documento} onChange={(e) => cambiarAcompanante(i, 'documento', e.target.value)} />
                  </label>
                  <label>
                    Fecha de nacimiento
                    <input type="date" required max={hoy} value={a.fechaNacimiento} onChange={(e) => cambiarAcompanante(i, 'fechaNacimiento', e.target.value)} />
                  </label>
                </div>
              </div>
            ))}
            {totalPersonas < capacidad && (
              <button type="button" className="boton boton--contorno boton--pequeno" onClick={() => setAcompanantes((l) => [...l, vacio()])}>
                Agregar acompañante
              </button>
            )}
            <small className="texto-suave">
              Este domo recibe hasta {capacidad} personas. Los menores de {alojamiento?.umbralEdadFacturable ?? 12} años no pagan alojamiento.
            </small>
          </fieldset>

          {error && <Aviso>{error}</Aviso>}
          <button className="boton boton--fogata boton--ancho" disabled={enviando || totalPersonas > capacidad}>
            {enviando ? 'Creando tu reserva…' : 'Crear reserva'}
          </button>
        </form>

        {domo && (
          <aside className="resumen-reserva">
            <RetratoDomo id={domo.idDomo} {...dotacion(domo)} />
            <h2>{domo.nombre}</h2>
            <p>{rango(entrada, salida)}, {n} {n === 1 ? 'noche' : 'noches'}</p>
            <p>{totalPersonas} {totalPersonas === 1 ? 'persona' : 'personas'}</p>
            {cotizacion && (
              <p className="resumen-reserva__total">
                <span>Total estimado</span>
                <strong>{dinero(cotizacion.valorTotalEstancia)}</strong>
              </p>
            )}
            {alojamiento && cotizacion && (
              <p className="texto-suave">
                La reserva nace pendiente. Para confirmarla se paga un anticipo de{' '}
                {dinero(Math.round((cotizacion.valorTotalEstancia * alojamiento.anticipoPorcentaje) / 100))} ({alojamiento.anticipoPorcentaje} %) dentro de
                las {alojamiento.plazoConfirmacionHoras} horas siguientes.
              </p>
            )}
          </aside>
        )}
      </div>
    </section>
  );
}
