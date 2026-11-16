import { useState, type FormEvent } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useSesion } from '../auth/AuthContext';
import { Aviso } from '../components/Comunes';
import { FOTOS, Foto } from '../components/Fotos';
import { hoyIso } from '../components/formato';
import { mensajeDeError } from '../services/api';
import { registrarse } from '../services/sga';

export default function Registro() {
  const navegar = useNavigate();
  const { entrar } = useSesion();
  const volverA = (useLocation().state as { volverA?: string } | null)?.volverA;
  const [datos, setDatos] = useState({ nombre: '', correo: '', contrasena: '', telefono: '', documento: '', fechaNacimiento: '' });
  const [error, setError] = useState('');
  const [enviando, setEnviando] = useState(false);

  const campo = (k: keyof typeof datos) => ({
    value: datos[k],
    onChange: (e: { target: { value: string } }) => setDatos((d) => ({ ...d, [k]: e.target.value })),
  });

  const contrasenaValida = /^(?=.*[A-Z])(?=.*\d).{8,}$/.test(datos.contrasena);

  const enviar = async (e: FormEvent) => {
    e.preventDefault();
    if (!contrasenaValida) {
      setError('La contraseña necesita al menos 8 caracteres, una mayúscula y un número.');
      return;
    }
    setError('');
    setEnviando(true);
    try {
      await registrarse({ ...datos, nombre: datos.nombre.trim(), correo: datos.correo.trim(), documento: datos.documento.trim() });
      try {
        await entrar(datos.correo, datos.contrasena);
        navegar(volverA ?? '/mis-reservas', { replace: true });
      } catch {
        navegar('/ingresar', { state: { registrado: datos.correo, volverA } });
      }
    } catch (err) {
      setError(mensajeDeError(err));
    } finally {
      setEnviando(false);
    }
  };

  return (
    <section className="acceso">
      <div className="acceso__formulario">
      <h1>Crea tu cuenta</h1>
      <p className="texto-suave">Con tu cuenta reservas en línea y consultas el estado de tus reservas.</p>
      <form className="formulario" onSubmit={enviar}>
        <label>
          Nombre completo
          <input required maxLength={150} autoComplete="name" {...campo('nombre')} />
        </label>
        <div className="formulario__par">
          <label>
            Documento de identidad
            <input required minLength={5} maxLength={15} inputMode="numeric" {...campo('documento')} />
          </label>
          <label>
            Fecha de nacimiento
            <input type="date" required max={hoyIso()} {...campo('fechaNacimiento')} />
          </label>
        </div>
        <div className="formulario__par">
          <label>
            Correo
            <input type="email" required autoComplete="email" {...campo('correo')} />
          </label>
          <label>
            Teléfono
            <input type="tel" required maxLength={30} autoComplete="tel" {...campo('telefono')} />
          </label>
        </div>
        <label>
          Contraseña
          <input type="password" required autoComplete="new-password" {...campo('contrasena')} />
          <small className={datos.contrasena && !contrasenaValida ? 'texto-alerta' : 'texto-suave'}>
            Mínimo 8 caracteres, con una mayúscula y un número.
          </small>
        </label>
        {error && <Aviso>{error}</Aviso>}
        <button className="boton boton--fogata boton--ancho" disabled={enviando}>
          {enviando ? 'Creando tu cuenta…' : 'Crear cuenta'}
        </button>
      </form>
      <p className="texto-suave">
        ¿Ya tienes cuenta? <Link to="/ingresar" state={{ volverA }}>Ingresa</Link>
      </p>
      </div>
      <div className="acceso__foto">
        <Foto id={FOTOS.fogataAmigos} alt="Amigos alrededor de una fogata en el bosque" ancho={1100} alto={1400} eager />
        <p>Tu próxima fogata empieza aquí.</p>
      </div>
    </section>
  );
}
