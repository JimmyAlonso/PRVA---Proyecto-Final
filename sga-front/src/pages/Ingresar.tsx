import { useState, type FormEvent } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { inicioSegunRol, useSesion } from '../auth/AuthContext';
import { Aviso } from '../components/Comunes';
import { FOTOS, Foto } from '../components/Fotos';
import { mensajeDeError } from '../services/api';

export default function Ingresar() {
  const { entrar } = useSesion();
  const navegar = useNavigate();
  const ubicacion = useLocation();
  const volverA = (ubicacion.state as { volverA?: string; registrado?: string } | null)?.volverA;
  const registrado = (ubicacion.state as { registrado?: string } | null)?.registrado;
  const [correo, setCorreo] = useState(registrado ?? '');
  const [contrasena, setContrasena] = useState('');
  const [error, setError] = useState('');
  const [enviando, setEnviando] = useState(false);

  const enviar = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    setEnviando(true);
    try {
      const usuario = await entrar(correo, contrasena);
      navegar(volverA ?? inicioSegunRol(usuario.rol), { replace: true });
    } catch (err) {
      setError(mensajeDeError(err));
    } finally {
      setEnviando(false);
    }
  };

  return (
    <section className="acceso">
      <div className="acceso__formulario">
      <h1>Ingresar</h1>
      {registrado && <Aviso tipo="exito">Tu cuenta quedó creada. Ingresa con tu correo y contraseña.</Aviso>}
      {volverA && !registrado && <Aviso tipo="info">Ingresa con tu cuenta para continuar.</Aviso>}
      <form className="formulario" onSubmit={enviar}>
        <label>
          Correo
          <input type="email" autoComplete="email" required value={correo} onChange={(e) => setCorreo(e.target.value)} />
        </label>
        <label>
          Contraseña
          <input type="password" autoComplete="current-password" required value={contrasena} onChange={(e) => setContrasena(e.target.value)} />
        </label>
        {error && <Aviso>{error}</Aviso>}
        <button className="boton boton--fogata boton--ancho" disabled={enviando}>
          {enviando ? 'Ingresando…' : 'Ingresar'}
        </button>
      </form>
      <p className="texto-suave">
        ¿Primera vez? <Link to="/registro" state={{ volverA }}>Crea tu cuenta de huésped</Link>
      </p>
      </div>
      <div className="acceso__foto">
        <Foto id={FOTOS.cocoraBruma} alt="Palmas de cera entre la bruma del Valle de Cocora" ancho={1100} alto={1400} eager />
        <p>Qué bueno verte de nuevo por aquí.</p>
      </div>
    </section>
  );
}
