import { useCallback, useEffect, useState, type FormEvent } from 'react';
import { useSesion } from '../auth/AuthContext';
import { Aviso, Cargando, Paginador } from '../components/Comunes';
import { hoyIso, nombreRol } from '../components/formato';
import { mensajeDeError } from '../services/api';
import { asignarRol, crearUsuario, eliminarUsuario, listarUsuarios } from '../services/sga';
import type { Pagina, Rol, Usuario } from '../services/types';

const ROLES: Rol[] = ['RECEPCIONISTA', 'PERSONAL_SERVICIO', 'ADMINISTRADOR', 'HUESPED'];
const vacio = { nombre: '', correo: '', contrasena: '', telefono: '', documento: '', fechaNacimiento: '', rol: 'RECEPCIONISTA' as Rol };

export default function Usuarios() {
  const { usuario: yo } = useSesion();
  const [pagina, setPagina] = useState(0);
  const [lista, setLista] = useState<Pagina<Usuario> | null>(null);
  const [datos, setDatos] = useState(vacio);
  const [error, setError] = useState('');
  const [exito, setExito] = useState('');
  const [ocupado, setOcupado] = useState(false);
  const [porEliminar, setPorEliminar] = useState<string>('');

  const cargar = useCallback(() => {
    listarUsuarios(pagina)
      .then(setLista)
      .catch((e) => setError(mensajeDeError(e)));
  }, [pagina]);

  useEffect(cargar, [cargar]);

  const campo = (k: Exclude<keyof typeof vacio, 'rol'>) => ({
    value: datos[k],
    onChange: (e: { target: { value: string } }) => setDatos((d) => ({ ...d, [k]: e.target.value })),
  });

  const contrasenaValida = /^(?=.*[A-Z])(?=.*\d).{8,}$/.test(datos.contrasena);

  const crear = async (e: FormEvent) => {
    e.preventDefault();
    if (!contrasenaValida) {
      setError('La contraseña necesita al menos 8 caracteres, una mayúscula y un número.');
      return;
    }
    setError('');
    setExito('');
    setOcupado(true);
    try {
      const nuevo = await crearUsuario({ ...datos, nombre: datos.nombre.trim(), correo: datos.correo.trim(), documento: datos.documento.trim() });
      setExito(`Cuenta creada: ${nuevo.correo} (${nombreRol[nuevo.rol]}).`);
      setDatos(vacio);
      cargar();
    } catch (err) {
      setError(mensajeDeError(err));
    } finally {
      setOcupado(false);
    }
  };

  const cambiarRol = async (u: Usuario, rol: Rol) => {
    setError('');
    setExito('');
    try {
      await asignarRol(u.id, rol);
      setExito(`${u.nombre} ahora es ${nombreRol[rol].toLowerCase()}.`);
      cargar();
    } catch (err) {
      setError(mensajeDeError(err));
    }
  };

  const eliminar = async (u: Usuario) => {
    setError('');
    setExito('');
    try {
      await eliminarUsuario(u.id);
      setExito(`Se eliminó la cuenta de ${u.nombre}.`);
      setPorEliminar('');
      cargar();
    } catch (err) {
      setError(mensajeDeError(err));
    }
  };

  return (
    <section className="contenedor pagina">
      <h1>Usuarios</h1>
      <p className="texto-amplio">Crea las cuentas del equipo: recepción, personal de servicio y otros administradores.</p>
      {error && <Aviso>{error}</Aviso>}
      {exito && <Aviso tipo="exito">{exito}</Aviso>}

      <div className="usuarios">
        <form className="formulario usuarios__nuevo" onSubmit={crear}>
          <h2>Nueva cuenta</h2>
          <label>
            Rol
            <select value={datos.rol} onChange={(e) => setDatos((d) => ({ ...d, rol: e.target.value as Rol }))}>
              {ROLES.map((r) => <option key={r} value={r}>{nombreRol[r]}</option>)}
            </select>
          </label>
          <label>
            Nombre completo
            <input required maxLength={150} {...campo('nombre')} />
          </label>
          <div className="formulario__par">
            <label>
              Documento
              <input required minLength={5} maxLength={15} {...campo('documento')} />
            </label>
            <label>
              Fecha de nacimiento
              <input type="date" required max={hoyIso()} {...campo('fechaNacimiento')} />
            </label>
          </div>
          <div className="formulario__par">
            <label>
              Correo
              <input type="email" required {...campo('correo')} />
            </label>
            <label>
              Teléfono
              <input type="tel" required maxLength={30} {...campo('telefono')} />
            </label>
          </div>
          <label>
            Contraseña inicial
            <input type="text" required autoComplete="off" {...campo('contrasena')} />
            <small className={datos.contrasena && !contrasenaValida ? 'texto-alerta' : 'texto-suave'}>
              Mínimo 8 caracteres, con una mayúscula y un número. Entrégasela a la persona.
            </small>
          </label>
          <button className="boton boton--fogata boton--ancho" disabled={ocupado}>
            {ocupado ? 'Creando…' : 'Crear cuenta'}
          </button>
        </form>

        <div className="usuarios__lista">
          <h2>Cuentas activas</h2>
          {!lista ? (
            <Cargando />
          ) : (
            <>
              <ul className="lista-usuarios">
                {lista.contenido.map((u) => (
                  <li key={u.id}>
                    <div>
                      <strong>{u.nombre}</strong>
                      <small>{u.correo}, {u.documento}</small>
                    </div>
                    <select
                      aria-label={`Rol de ${u.nombre}`}
                      value={u.rol}
                      disabled={u.id === yo?.id}
                      onChange={(e) => cambiarRol(u, e.target.value as Rol)}
                    >
                      {ROLES.map((r) => <option key={r} value={r}>{nombreRol[r]}</option>)}
                    </select>
                    {u.id === yo?.id ? (
                      <span className="texto-suave">Tu cuenta</span>
                    ) : porEliminar === u.id ? (
                      <span className="lista-usuarios__confirmar">
                        <button className="boton boton--peligro boton--pequeno" onClick={() => eliminar(u)}>Eliminar</button>
                        <button className="boton boton--fantasma boton--pequeno" onClick={() => setPorEliminar('')}>No</button>
                      </span>
                    ) : (
                      <button className="boton boton--fantasma boton--pequeno" onClick={() => setPorEliminar(u.id)}>Eliminar</button>
                    )}
                  </li>
                ))}
              </ul>
              <Paginador pagina={pagina} totalPaginas={lista.totalPaginas} cambiar={setPagina} />
            </>
          )}
        </div>
      </div>
    </section>
  );
}
