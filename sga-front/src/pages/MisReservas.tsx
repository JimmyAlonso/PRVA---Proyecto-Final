import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useSesion } from '../auth/AuthContext';
import { Aviso, Cargando, EstadoReservaChip, Paginador } from '../components/Comunes';
import { dinero, rango } from '../components/formato';
import { mensajeDeError } from '../services/api';
import { misReservas } from '../services/sga';
import type { Pagina, ReservaResumen } from '../services/types';

export default function MisReservas() {
  const { usuario } = useSesion();
  const [pagina, setPagina] = useState(0);
  const [datos, setDatos] = useState<Pagina<ReservaResumen> | null>(null);
  const [error, setError] = useState('');

  useEffect(() => {
    setDatos(null);
    misReservas(pagina)
      .then(setDatos)
      .catch((e) => setError(mensajeDeError(e)));
  }, [pagina]);

  return (
    <section className="contenedor pagina">
      <h1>Hola, {usuario?.nombre.split(' ')[0]}</h1>
      <p className="texto-amplio">Aquí ves tus reservas y en qué estado está cada una.</p>
      {error && <Aviso>{error}</Aviso>}
      {!datos && !error && <Cargando />}
      {datos && datos.contenido.length === 0 && (
        <div className="vacio">
          <h2>Aún no tienes reservas</h2>
          <p>Elige fechas y un domo para hacer la primera.</p>
          <Link className="boton boton--fogata" to="/buscar">Buscar domos libres</Link>
        </div>
      )}
      {datos && datos.contenido.length > 0 && (
        <>
          <ul className="lista-reservas">
            {datos.contenido.map((r) => (
              <li key={r.codigo}>
                <Link to={`/reservas/${r.codigo}`} className="fila-reserva">
                  <span className="fila-reserva__codigo">{r.codigo}</span>
                  <span>
                    <strong>{r.idDomo}</strong>, {rango(r.fechaEntrada, r.fechaSalida)}
                  </span>
                  <span>{r.totalOcupantes} personas</span>
                  <span className="num">{dinero(r.valorTotal)}</span>
                  <EstadoReservaChip estado={r.estado} />
                </Link>
              </li>
            ))}
          </ul>
          <Paginador pagina={pagina} totalPaginas={datos.totalPaginas} cambiar={setPagina} />
        </>
      )}
      <Link className="boton boton--contorno" to="/buscar">Hacer otra reserva</Link>
    </section>
  );
}
