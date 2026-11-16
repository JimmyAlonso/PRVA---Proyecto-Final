import { useEffect, useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { Aviso, Cargando, EstadoReservaChip, Paginador } from '../components/Comunes';
import { dinero, fechaLarga, hoyIso, nombreEstado, rango } from '../components/formato';
import { mensajeDeError } from '../services/api';
import { listarDomos, listarReservas, movimientosDelDia } from '../services/sga';
import type { Domo, EstadoReserva, Movimientos, Pagina, ReservaResumen } from '../services/types';

function FilaReserva({ r }: { r: ReservaResumen }) {
  return (
    <li>
      <Link to={`/reservas/${r.codigo}`} className="fila-reserva">
        <span className="fila-reserva__codigo">{r.codigo}</span>
        <span>
          <strong>{r.nombreTitular}</strong>
          <small>{r.idDomo}, {rango(r.fechaEntrada, r.fechaSalida)}</small>
        </span>
        <span>{r.totalOcupantes} pers.</span>
        <span className="num">{dinero(r.valorTotal)}</span>
        <EstadoReservaChip estado={r.estado} />
      </Link>
    </li>
  );
}

export default function Panel() {
  const [fecha, setFecha] = useState(hoyIso());
  const [movimientos, setMovimientos] = useState<Movimientos | null>(null);
  const [domos, setDomos] = useState<Domo[]>([]);
  const [estado, setEstado] = useState<EstadoReserva | ''>('');
  const [idDomo, setIdDomo] = useState('');
  const [documento, setDocumento] = useState('');
  const [filtroDocumento, setFiltroDocumento] = useState('');
  const [pagina, setPagina] = useState(0);
  const [reservas, setReservas] = useState<Pagina<ReservaResumen> | null>(null);
  const [error, setError] = useState('');

  useEffect(() => {
    setMovimientos(null);
    movimientosDelDia(fecha)
      .then(setMovimientos)
      .catch((e) => setError(mensajeDeError(e)));
  }, [fecha]);

  useEffect(() => {
    listarDomos().then((p) => setDomos(p.contenido)).catch(() => undefined);
  }, []);

  useEffect(() => {
    setReservas(null);
    listarReservas({ estado, idDomo, documentoTitular: filtroDocumento }, pagina)
      .then(setReservas)
      .catch((e) => setError(mensajeDeError(e)));
  }, [estado, idDomo, filtroDocumento, pagina]);

  const buscarDocumento = (e: FormEvent) => {
    e.preventDefault();
    setPagina(0);
    setFiltroDocumento(documento);
  };

  return (
    <section className="contenedor pagina">
      <div className="seccion__encabezado">
        <h1>Recepción</h1>
        <Link className="boton boton--fogata" to="/buscar">Nueva reserva</Link>
      </div>
      {error && <Aviso>{error}</Aviso>}

      <div className="movimientos">
        <div className="movimientos__barra">
          <h2>Movimientos del {fechaLarga(fecha)}</h2>
          <input type="date" aria-label="Fecha de los movimientos" value={fecha} onChange={(e) => setFecha(e.target.value || hoyIso())} />
        </div>
        {!movimientos ? (
          <Cargando />
        ) : (
          <div className="dos-columnas">
            <div>
              <h3>Llegan ({movimientos.llegadas.length})</h3>
              {movimientos.llegadas.length === 0 ? (
                <p className="texto-suave">Nadie tiene llegada prevista este día.</p>
              ) : (
                <ul className="lista-reservas lista-reservas--compacta">{movimientos.llegadas.map((r) => <FilaReserva key={r.codigo} r={r} />)}</ul>
              )}
            </div>
            <div>
              <h3>Salen ({movimientos.salidas.length})</h3>
              {movimientos.salidas.length === 0 ? (
                <p className="texto-suave">Nadie tiene salida prevista este día.</p>
              ) : (
                <ul className="lista-reservas lista-reservas--compacta">{movimientos.salidas.map((r) => <FilaReserva key={r.codigo} r={r} />)}</ul>
              )}
            </div>
          </div>
        )}
      </div>

      <h2>Todas las reservas</h2>
      <div className="filtros">
        <label>
          Estado
          <select value={estado} onChange={(e) => { setPagina(0); setEstado(e.target.value as EstadoReserva | ''); }}>
            <option value="">Todos</option>
            {(Object.keys(nombreEstado) as EstadoReserva[]).map((k) => (
              <option key={k} value={k}>{nombreEstado[k]}</option>
            ))}
          </select>
        </label>
        <label>
          Domo
          <select value={idDomo} onChange={(e) => { setPagina(0); setIdDomo(e.target.value); }}>
            <option value="">Todos</option>
            {domos.map((d) => <option key={d.idDomo} value={d.idDomo}>{d.idDomo}, {d.nombre}</option>)}
          </select>
        </label>
        <form onSubmit={buscarDocumento} className="filtros__documento">
          <label>
            Documento del titular
            <input value={documento} onChange={(e) => setDocumento(e.target.value)} placeholder="Ej.: 1094000001" />
          </label>
          <button className="boton boton--contorno">Buscar</button>
        </form>
      </div>
      {!reservas ? (
        <Cargando />
      ) : reservas.contenido.length === 0 ? (
        <p className="vacio-linea">No hay reservas con esos filtros.</p>
      ) : (
        <>
          <ul className="lista-reservas">{reservas.contenido.map((r) => <FilaReserva key={r.codigo} r={r} />)}</ul>
          <Paginador pagina={pagina} totalPaginas={reservas.totalPaginas} cambiar={setPagina} />
        </>
      )}
    </section>
  );
}
