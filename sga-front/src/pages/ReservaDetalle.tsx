import { useCallback, useEffect, useState, type FormEvent, type ReactNode } from 'react';
import { Link, useLocation, useParams } from 'react-router-dom';
import { useSesion } from '../auth/AuthContext';
import { Aviso, Cargando, EstadoReservaChip } from '../components/Comunes';
import { DesgloseNoches } from '../components/Cotizador';
import { dinero, fechaCorta, fechaLarga, hora12, rango } from '../components/formato';
import { mensajeDeError } from '../services/api';
import {
  cancelarReserva, confirmarReserva, declararNoShow, devolverDeposito, obtenerAlojamiento, obtenerFolio, obtenerReserva,
  registrarDeposito, registrarLlegada, registrarPago, registrarSalida, registrarServicio,
} from '../services/sga';
import type { Alojamiento, Folio, MedioPago, ReservaDetalle as Reserva } from '../services/types';

function Accion({ titulo, descripcion, children }: { titulo: string; descripcion?: string; children: ReactNode }) {
  return (
    <div className="accion">
      <h3>{titulo}</h3>
      {descripcion && <p className="texto-suave">{descripcion}</p>}
      {children}
    </div>
  );
}

export default function ReservaDetalle() {
  const { codigo = '' } = useParams();
  const creada = (useLocation().state as { creada?: boolean } | null)?.creada;
  const { usuario, tieneRol } = useSesion();
  const esPersonal = tieneRol('RECEPCIONISTA', 'ADMINISTRADOR');
  const esAdmin = tieneRol('ADMINISTRADOR');

  const [reserva, setReserva] = useState<Reserva | null>(null);
  const [folio, setFolio] = useState<Folio | null>(null);
  const [alojamiento, setAlojamiento] = useState<Alojamiento | null>(null);
  const [error, setError] = useState('');
  const [errorAccion, setErrorAccion] = useState('');
  const [exito, setExito] = useState(creada ? 'Reserva creada.' : '');
  const [ocupado, setOcupado] = useState(false);

  const [monto, setMonto] = useState('');
  const [medio, setMedio] = useState<MedioPago>('TRANSFERENCIA');
  const [hora, setHora] = useState('');
  const [motivo, setMotivo] = useState('');
  const [servicio, setServicio] = useState('');
  const [cantidad, setCantidad] = useState(1);
  const [motivoCierre, setMotivoCierre] = useState('');
  const [confirmandoCancelacion, setConfirmandoCancelacion] = useState(false);
  const [medioEnLinea, setMedioEnLinea] = useState<MedioPago>('TARJETA');

  const recargar = useCallback(async () => {
    try {
      const [r, f] = await Promise.all([obtenerReserva(codigo), obtenerFolio(codigo).catch(() => null)]);
      setReserva(r);
      setFolio(f);
      setHora((h) => h || r.horaEstimadaLlegada?.slice(0, 5) || '16:00');
    } catch (e) {
      setError(mensajeDeError(e));
    }
  }, [codigo]);

  useEffect(() => {
    recargar();
    obtenerAlojamiento().then(setAlojamiento).catch(() => undefined);
  }, [recargar]);

  const ejecutar = async (accion: () => Promise<unknown>, mensaje: string) => {
    setErrorAccion('');
    setExito('');
    setOcupado(true);
    try {
      await accion();
      await recargar();
      setExito(mensaje);
    } catch (e) {
      setErrorAccion(mensajeDeError(e));
    } finally {
      setOcupado(false);
    }
  };

  if (error) {
    return (
      <section className="contenedor pagina">
        <Aviso>{error}</Aviso>
        <Link to={esPersonal ? '/panel' : '/mis-reservas'}>Volver</Link>
      </section>
    );
  }
  if (!reserva) return <section className="contenedor pagina"><Cargando /></section>;

  const anticipo = alojamiento ? Math.round((reserva.valorTotal * alojamiento.anticipoPorcentaje) / 100) : 0;
  const pagado = folio?.totalPagos ?? 0;
  const estado = reserva.estado;
  const cancelable = estado === 'PENDIENTE' || estado === 'CONFIRMADA';

  const esPropia = !esPersonal && reserva.titular.documento === usuario?.documento;
  const faltaAnticipo = Math.max(anticipo - pagado, 0);
  const saldo = folio?.saldo ?? 0;

  // Pago en línea del huésped: paga lo que falte del anticipo y confirma la reserva
  const pagarYConfirmar = () =>
    ejecutar(async () => {
      if (faltaAnticipo > 0) await registrarPago(codigo, faltaAnticipo, medioEnLinea);
      await confirmarReserva(codigo, hora);
    }, '¡Pago recibido! Tu reserva quedó confirmada.');

  const pagarSaldo = () =>
    ejecutar(() => registrarPago(codigo, saldo, medioEnLinea), `Pago de ${dinero(saldo)} recibido.`);

  const pagar = (e: FormEvent) => {
    e.preventDefault();
    const valor = Number(monto);
    if (!valor || valor <= 0) {
      setErrorAccion('Escribe un monto mayor que cero.');
      return;
    }
    ejecutar(() => registrarPago(codigo, valor, medio), `Pago de ${dinero(valor)} registrado.`).then(() => setMonto(''));
  };

  return (
    <section className="contenedor pagina">
      <Link to={esPersonal ? '/panel' : '/mis-reservas'} className="migas">
        {esPersonal ? 'Volver a recepción' : 'Mis reservas'}
      </Link>

      <header className="reserva-cabecera">
        <div>
          <p className="texto-suave">Reserva</p>
          <h1 className="reserva-cabecera__codigo">{reserva.codigo}</h1>
        </div>
        <EstadoReservaChip estado={estado} />
      </header>

      {exito && <Aviso tipo="exito">{exito}</Aviso>}

      {!esPersonal && estado === 'PENDIENTE' && alojamiento && (
        <Aviso tipo="info">
          Tu reserva está apartada. Para confirmarla paga el anticipo de <strong>{dinero(anticipo)}</strong> ({alojamiento.anticipoPorcentaje} %)
          dentro de las {alojamiento.plazoConfirmacionHoras} horas siguientes a su creación. Si no se paga a tiempo, la reserva vence sola.
        </Aviso>
      )}
      {!esPersonal && estado === 'CONFIRMADA' && alojamiento && (
        <Aviso tipo="exito">
          Tu reserva está confirmada. Te esperamos el {fechaLarga(reserva.fechaEntrada)} desde las {hora12(alojamiento.horaEntrada)}. Al llegar se
          entrega un depósito de {dinero(alojamiento.deposito)} que se devuelve a la salida.
        </Aviso>
      )}

      <div className="reserva-detalle">
        <div className="reserva-detalle__principal">
          <dl className="ficha ficha--reserva">
            <div><dt>Domo</dt><dd><Link to={`/domos/${reserva.idDomo}`}>{reserva.idDomo}</Link></dd></div>
            <div><dt>Fechas</dt><dd>{rango(reserva.fechaEntrada, reserva.fechaSalida)}, {reserva.noches} {reserva.noches === 1 ? 'noche' : 'noches'}</dd></div>
            <div><dt>Llegada estimada</dt><dd>{reserva.horaEstimadaLlegada ? hora12(reserva.horaEstimadaLlegada) : 'Sin definir'}</dd></div>
            <div><dt>Canal</dt><dd>{reserva.canalOrigen === 'PORTAL' ? 'Portal web' : reserva.canalOrigen === 'DIRECTO' ? 'Recepción' : `Externo (${reserva.canalExterno ?? ''})`}</dd></div>
            <div><dt>Titular</dt><dd>{reserva.titular.nombre}<br /><small>{reserva.titular.documento}, {reserva.titular.correo}, {reserva.titular.telefono}</small></dd></div>
            <div><dt>Creada</dt><dd>{fechaCorta(reserva.momentoCreacion.slice(0, 10))}</dd></div>
          </dl>
          {reserva.motivoCancelacion && <Aviso tipo="info">Motivo de la cancelación: {reserva.motivoCancelacion}</Aviso>}

          <h2>Quiénes se hospedan</h2>
          <ul className="ocupantes">
            {reserva.ocupantes.map((o) => (
              <li key={o.documento}>
                <span>{o.nombre}</span>
                <small>{o.documento}</small>
                <span className={o.facturable ? 'etiqueta-paga' : 'etiqueta-gratis'}>{o.facturable ? 'Paga' : 'No paga'}</span>
              </li>
            ))}
          </ul>

          <h2>Valor de la estancia</h2>
          <DesgloseNoches cotizacion={{ desglose: reserva.desglose, valorTotalEstancia: reserva.valorTotal, noches: reserva.noches }} />

          {folio && (
            <>
              <h2>Cuenta de la estancia</h2>
              <div className="folio">
                <div className="folio__totales">
                  <p><span>Cargos</span><strong>{dinero(folio.totalCargos)}</strong></p>
                  <p><span>Pagos</span><strong>{dinero(folio.totalPagos)}</strong></p>
                  <p className={folio.saldo > 0 ? 'folio__saldo folio__saldo--debe' : 'folio__saldo'}>
                    <span>{folio.saldo > 0 ? 'Saldo pendiente' : folio.saldo < 0 ? 'Saldo a favor' : 'Saldo'}</span>
                    <strong>{dinero(Math.abs(folio.saldo))}</strong>
                  </p>
                </div>
                {(folio.cargos.length > 0 || folio.pagos.length > 0) && (
                  <table>
                    <thead>
                      <tr><th scope="col">Fecha</th><th scope="col">Concepto</th><th scope="col" className="num">Valor</th></tr>
                    </thead>
                    <tbody>
                      {folio.cargos.map((c, i) => (
                        <tr key={`c${i}`}>
                          <td>{fechaCorta(c.fecha)}</td>
                          <td>{c.descripcion || c.concepto}</td>
                          <td className="num">{c.esInverso ? '−' : ''}{dinero(c.valor)}</td>
                        </tr>
                      ))}
                      {folio.pagos.map((p) => (
                        <tr key={p.idPago} className="fila-pago">
                          <td>{fechaCorta(p.fecha)}</td>
                          <td>Pago por {p.medio.toLowerCase()}</td>
                          <td className="num">−{dinero(p.monto)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
                <p className="texto-suave">
                  {folio.depositoRegistrado ? 'Depósito de garantía registrado.' : 'Sin depósito de garantía registrado.'}
                  {folio.cerrado && ' Cuenta cerrada.'}
                </p>
              </div>
            </>
          )}
        </div>

        <aside className="reserva-detalle__acciones">
          {errorAccion && <Aviso>{errorAccion}</Aviso>}

          {esPropia && estado === 'PENDIENTE' && alojamiento && (
            <Accion
              titulo="Pagar el anticipo"
              descripcion={`Pagas ${dinero(faltaAnticipo)} ahora y el resto (${dinero(Math.max(reserva.valorTotal - anticipo, 0))}) al llegar.`}
            >
              <div className="formulario formulario--compacto">
                <fieldset className="medios-pago">
                  <legend className="texto-suave">Medio de pago</legend>
                  <label className={`medio ${medioEnLinea === 'TARJETA' ? 'medio--activo' : ''}`}>
                    <input type="radio" name="medio" checked={medioEnLinea === 'TARJETA'} onChange={() => setMedioEnLinea('TARJETA')} />
                    Tarjeta débito o crédito
                  </label>
                  <label className={`medio ${medioEnLinea === 'TRANSFERENCIA' ? 'medio--activo' : ''}`}>
                    <input type="radio" name="medio" checked={medioEnLinea === 'TRANSFERENCIA'} onChange={() => setMedioEnLinea('TRANSFERENCIA')} />
                    Transferencia bancaria (PSE)
                  </label>
                </fieldset>
                <label>
                  ¿A qué hora crees que llegas?
                  <input type="time" value={hora} onChange={(e) => setHora(e.target.value)} />
                </label>
                <button className="boton boton--fogata boton--ancho" disabled={ocupado} onClick={pagarYConfirmar}>
                  {ocupado ? 'Procesando…' : `Pagar ${dinero(faltaAnticipo)} y confirmar`}
                </button>
                <small className="texto-suave">Pago simulado para el proyecto académico: no se cobra dinero real.</small>
              </div>
            </Accion>
          )}

          {esPropia && (estado === 'CONFIRMADA' || estado === 'EN_CURSO') && saldo > 0 && (
            <Accion titulo="Pagar el saldo" descripcion={`Te falta pagar ${dinero(saldo)}. Puedes hacerlo ahora o al llegar.`}>
              <div className="formulario formulario--compacto">
                <label>
                  Medio de pago
                  <select value={medioEnLinea} onChange={(e) => setMedioEnLinea(e.target.value as MedioPago)}>
                    <option value="TARJETA">Tarjeta débito o crédito</option>
                    <option value="TRANSFERENCIA">Transferencia bancaria (PSE)</option>
                  </select>
                </label>
                <button className="boton boton--contorno boton--ancho" disabled={ocupado} onClick={pagarSaldo}>
                  Pagar {dinero(saldo)}
                </button>
                <small className="texto-suave">Pago simulado: no se cobra dinero real.</small>
              </div>
            </Accion>
          )}

          {esPersonal && (estado === 'PENDIENTE' || estado === 'CONFIRMADA' || estado === 'EN_CURSO') && (
            <Accion
              titulo="Registrar pago"
              descripcion={estado === 'PENDIENTE' && alojamiento ? `Anticipo exigido: ${dinero(anticipo)}. Pagado: ${dinero(pagado)}.` : undefined}
            >
              <form className="formulario formulario--compacto" onSubmit={pagar}>
                <div className="formulario__par">
                  <label>
                    Monto
                    <input type="number" min={1} step={1} value={monto} placeholder={anticipo ? String(Math.max(anticipo - pagado, 0)) : ''} onChange={(e) => setMonto(e.target.value)} />
                  </label>
                  <label>
                    Medio
                    <select value={medio} onChange={(e) => setMedio(e.target.value as MedioPago)}>
                      <option value="TRANSFERENCIA">Transferencia</option>
                      <option value="TARJETA">Tarjeta</option>
                      <option value="EFECTIVO">Efectivo</option>
                    </select>
                  </label>
                </div>
                <button className="boton boton--contorno boton--ancho" disabled={ocupado}>Registrar pago</button>
              </form>
            </Accion>
          )}

          {esPersonal && estado === 'PENDIENTE' && (
            <Accion titulo="Confirmar reserva" descripcion="Exige el anticipo pagado y la hora estimada de llegada.">
              <div className="formulario formulario--compacto">
                <label>
                  Hora estimada de llegada
                  <input type="time" value={hora} onChange={(e) => setHora(e.target.value)} />
                </label>
                <button className="boton boton--fogata boton--ancho" disabled={ocupado} onClick={() => ejecutar(() => confirmarReserva(codigo, hora), 'Reserva confirmada.')}>
                  Confirmar reserva
                </button>
              </div>
            </Accion>
          )}

          {esPersonal && estado === 'CONFIRMADA' && (
            <Accion titulo="Llegada del huésped" descripcion={alojamiento ? `Primero se registra el depósito de ${dinero(alojamiento.deposito)}.` : undefined}>
              <div className="pila-botones">
                {!folio?.depositoRegistrado && (
                  <button className="boton boton--contorno boton--ancho" disabled={ocupado} onClick={() => ejecutar(() => registrarDeposito(codigo), 'Depósito registrado.')}>
                    Registrar depósito
                  </button>
                )}
                <button className="boton boton--fogata boton--ancho" disabled={ocupado} onClick={() => ejecutar(() => registrarLlegada(codigo), 'Llegada registrada. El huésped está hospedado.')}>
                  Registrar llegada
                </button>
                <button className="boton boton--fantasma boton--ancho" disabled={ocupado} onClick={() => ejecutar(() => declararNoShow(codigo), 'Se declaró que el huésped no se presentó.')}>
                  Declarar que no se presentó
                </button>
              </div>
            </Accion>
          )}

          {esPersonal && estado === 'EN_CURSO' && (
            <>
              {alojamiento && alojamiento.servicios.length > 0 && (
                <Accion titulo="Cargar servicio adicional">
                  <div className="formulario formulario--compacto">
                    <div className="formulario__par">
                      <label>
                        Servicio
                        <select value={servicio} onChange={(e) => setServicio(e.target.value)}>
                          <option value="">Elige uno</option>
                          {alojamiento.servicios.map((s) => (
                            <option key={s.codigo} value={s.codigo}>{s.nombre} ({dinero(s.valor)})</option>
                          ))}
                        </select>
                      </label>
                      <label>
                        Cantidad
                        <input type="number" min={1} value={cantidad} onChange={(e) => setCantidad(Number(e.target.value) || 1)} />
                      </label>
                    </div>
                    <button className="boton boton--contorno boton--ancho" disabled={ocupado || !servicio} onClick={() => ejecutar(() => registrarServicio(codigo, servicio, cantidad), 'Servicio cargado a la cuenta.')}>
                      Cargar a la cuenta
                    </button>
                  </div>
                </Accion>
              )}
              <Accion titulo="Salida del huésped" descripcion="Para cerrar, la cuenta debe quedar en cero o un administrador debe autorizarlo.">
                <div className="pila-botones">
                  {folio?.depositoRegistrado && (
                    <button className="boton boton--contorno boton--ancho" disabled={ocupado} onClick={() => ejecutar(() => devolverDeposito(codigo), 'Depósito devuelto.')}>
                      Devolver depósito
                    </button>
                  )}
                  {esAdmin && folio && folio.saldo !== 0 && (
                    <label>
                      Motivo para autorizar el cierre con saldo
                      <input value={motivoCierre} onChange={(e) => setMotivoCierre(e.target.value)} />
                    </label>
                  )}
                  <button className="boton boton--fogata boton--ancho" disabled={ocupado} onClick={() => ejecutar(() => registrarSalida(codigo, motivoCierre.trim() || undefined), 'Salida registrada y cuenta cerrada.')}>
                    Registrar salida
                  </button>
                </div>
              </Accion>
            </>
          )}

          {cancelable && (esPersonal || reserva.titular.documento === usuario?.documento) && (
            <Accion titulo="Cancelar reserva" descripcion={`Se aplica la política de cancelación vigente al reservar (versión ${reserva.versionPoliticaCancelacion}).`}>
              {!confirmandoCancelacion ? (
                <button className="boton boton--peligro-contorno boton--ancho" onClick={() => setConfirmandoCancelacion(true)}>
                  Cancelar reserva
                </button>
              ) : (
                <div className="formulario formulario--compacto">
                  <label>
                    Motivo
                    <input value={motivo} onChange={(e) => setMotivo(e.target.value)} placeholder="Ej.: cambio de planes" />
                  </label>
                  <div className="formulario__par">
                    <button className="boton boton--fantasma" onClick={() => setConfirmandoCancelacion(false)}>Mantener</button>
                    <button
                      className="boton boton--peligro"
                      disabled={ocupado || !motivo.trim()}
                      onClick={() => ejecutar(() => cancelarReserva(codigo, motivo.trim()), 'Reserva cancelada.').then(() => setConfirmandoCancelacion(false))}
                    >
                      Sí, cancelar
                    </button>
                  </div>
                </div>
              )}
            </Accion>
          )}

          {!esPersonal && !cancelable && (
            <p className="texto-suave">Esta reserva ya no admite cambios desde el portal. Si necesitas algo, escribe a recepción con el código.</p>
          )}
        </aside>
      </div>
    </section>
  );
}
