import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { Aviso, Cargando } from '../components/Comunes';
import { Buscador } from '../components/Domos';
import { dinero, hoyIso, noches, rango, sumarDias } from '../components/formato';
import { FotoDomo } from '../components/Fotos';
import { CabeceraFoto, FOTOS } from '../components/Fotos';
import { mensajeDeError } from '../services/api';
import { buscarDisponibles } from '../services/sga';
import type { Cotizacion } from '../services/types';

export default function Buscar() {
  const [parametros] = useSearchParams();
  const hoy = hoyIso();
  const entrada = parametros.get('entrada') ?? sumarDias(hoy, 7);
  const salida = parametros.get('salida') ?? sumarDias(hoy, 9);
  const personas = Number(parametros.get('personas') ?? 2) || 2;
  const [conJacuzzi, setConJacuzzi] = useState(false);
  const [resultados, setResultados] = useState<Cotizacion[] | null>(null);
  const [error, setError] = useState('');

  useEffect(() => {
    setResultados(null);
    setError('');
    buscarDisponibles({ fechaEntrada: entrada, fechaSalida: salida, numeroOcupantes: personas, conJacuzzi })
      .then((p) => setResultados(p.contenido))
      .catch((e) => setError(mensajeDeError(e)));
  }, [entrada, salida, personas, conJacuzzi]);

  const n = noches(entrada, salida);

  return (
    <>
    <CabeceraFoto id={FOTOS.cocoraPalmas} alt="Palmas de cera contra el cielo azul" titulo="Domos libres">
      <p>
        {rango(entrada, salida)}, {n} {n === 1 ? 'noche' : 'noches'} para {personas} {personas === 1 ? 'persona' : 'personas'}.
      </p>
    </CabeceraFoto>
    <section className="contenedor pagina">
      <Buscador compacto inicial={{ entrada, salida, personas }} />
      <label className="interruptor">
        <input type="checkbox" checked={conJacuzzi} onChange={(e) => setConJacuzzi(e.target.checked)} />
        Solo domos con jacuzzi
      </label>
      {error && <Aviso>{error}</Aviso>}
      {!resultados && !error && <Cargando texto="Buscando domos libres…" />}
      {resultados && resultados.length === 0 && (
        <div className="vacio">
          <h2>No hay domos libres para esas fechas</h2>
          <p>Prueba moviendo un día la llegada o la salida, o revisa el calendario de cada domo.</p>
          <Link className="boton boton--contorno" to="/domos">Ver calendarios</Link>
        </div>
      )}
      {resultados && resultados.length > 0 && (
        <ul className="resultados">
          {resultados.map((c) => (
            <li key={c.idDomo} className="resultado">
              <FotoDomo idDomo={c.idDomo} ancho={520} className="foto-domo resultado__foto" />
              <div className="resultado__cuerpo">
                <h2>
                  {c.nombre} <span className="codigo">{c.idDomo}</span>
                </h2>
                <p className="texto-suave">Hasta {c.capacidadMaxima} personas</p>
                <ul className="etiquetas">
                  {c.caracteristicas.map((x) => <li key={x}>{x}</li>)}
                </ul>
              </div>
              <div className="resultado__precio">
                <p>
                  <strong>{dinero(c.valorTotalEstancia)}</strong>
                  <span>total por {c.noches} {c.noches === 1 ? 'noche' : 'noches'}</span>
                </p>
                <Link
                  className="boton boton--fogata"
                  to={`/domos/${c.idDomo}?entrada=${entrada}&salida=${salida}&adultos=${personas}`}
                >
                  Ver y reservar
                </Link>
              </div>
            </li>
          ))}
        </ul>
      )}
    </section>
    </>
  );
}
