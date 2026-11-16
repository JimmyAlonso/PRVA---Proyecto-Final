import { useCallback, useEffect, useState } from 'react';
import { Aviso, Cargando } from '../components/Comunes';
import { dotacion } from '../components/Domos';
import { RetratoDomo } from '../components/Ilustraciones';
import { mensajeDeError } from '../services/api';
import { iniciarPreparacion, marcarPreparado, pendientesPreparacion } from '../services/sga';
import type { Domo } from '../services/types';

const nombreEstadoDomo: Record<string, string> = {
  PENDIENTE_PREPARACION: 'Por preparar',
  EN_PREPARACION: 'En preparación',
};

export default function Preparacion() {
  const [domos, setDomos] = useState<Domo[] | null>(null);
  const [error, setError] = useState('');
  const [exito, setExito] = useState('');
  const [ocupado, setOcupado] = useState('');

  const cargar = useCallback(() => {
    pendientesPreparacion()
      .then((p) => setDomos(p.contenido))
      .catch((e) => setError(mensajeDeError(e)));
  }, []);

  useEffect(cargar, [cargar]);

  const avanzar = async (domo: Domo) => {
    setError('');
    setExito('');
    setOcupado(domo.idDomo);
    try {
      if (domo.estadoOperativo === 'PENDIENTE_PREPARACION') {
        await iniciarPreparacion(domo.idDomo);
        setExito(`${domo.idDomo} quedó en preparación.`);
      } else {
        await marcarPreparado(domo.idDomo);
        setExito(`${domo.idDomo} quedó listo para recibir huéspedes.`);
      }
      cargar();
    } catch (e) {
      setError(mensajeDeError(e));
    } finally {
      setOcupado('');
    }
  };

  return (
    <section className="contenedor pagina">
      <h1>Preparación de domos</h1>
      <p className="texto-amplio">Después de cada salida, el domo se desinfecta, se higieniza el jacuzzi y se monta la fogata antes de la siguiente llegada.</p>
      {error && <Aviso>{error}</Aviso>}
      {exito && <Aviso tipo="exito">{exito}</Aviso>}
      {!domos && !error && <Cargando />}
      {domos && domos.length === 0 && (
        <div className="vacio">
          <h2>Todos los domos están listos</h2>
          <p>Cuando un huésped salga, su domo aparecerá aquí para prepararlo.</p>
        </div>
      )}
      {domos && domos.length > 0 && (
        <ul className="lista-preparacion">
          {domos.map((d) => (
            <li key={d.idDomo}>
              <RetratoDomo id={d.idDomo} {...dotacion(d)} />
              <div>
                <h2>{d.nombre} <span className="codigo">{d.idDomo}</span></h2>
                <p className={`estado-domo estado-domo--${d.estadoOperativo.toLowerCase()}`}>{nombreEstadoDomo[d.estadoOperativo] ?? d.estadoOperativo}</p>
              </div>
              <button className="boton boton--fogata" disabled={ocupado === d.idDomo} onClick={() => avanzar(d)}>
                {d.estadoOperativo === 'PENDIENTE_PREPARACION' ? 'Empezar preparación' : 'Marcar como listo'}
              </button>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
