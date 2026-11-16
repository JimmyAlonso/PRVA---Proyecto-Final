import { useEffect, useState } from 'react';
import { Link, NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useSesion } from '../auth/AuthContext';
import { AvisoServidorDespertando } from './Comunes';
import { nombreRol } from './formato';
import { MarcaDomo } from './Ilustraciones';

export function Layout() {
  const { usuario, salir, tieneRol } = useSesion();
  const navegar = useNavigate();
  const ubicacion = useLocation();
  const [menuAbierto, setMenuAbierto] = useState(false);

  useEffect(() => {
    setMenuAbierto(false);
    window.scrollTo({ top: 0 });
  }, [ubicacion.pathname]);

  const cerrarSesion = () => {
    salir();
    navegar('/');
  };

  return (
    <div className="marco">
      <a className="saltar" href="#contenido">
        Saltar al contenido
      </a>
      <header className={`cabecera ${['/', '/domos', '/buscar'].includes(ubicacion.pathname) ? 'cabecera--sobre-paisaje' : ''}`}>
        <div className="contenedor cabecera__fila">
          <Link to="/" className="marca">
            <MarcaDomo />
            <span>JiyJu Glamping</span>
          </Link>
          <button
            className="menu-movil"
            aria-expanded={menuAbierto}
            aria-controls="navegacion"
            onClick={() => setMenuAbierto((a) => !a)}
          >
            {menuAbierto ? 'Cerrar' : 'Menú'}
          </button>
          <nav id="navegacion" className={`navegacion ${menuAbierto ? 'navegacion--abierta' : ''}`}>
            <NavLink to="/domos">Domos</NavLink>
            <NavLink to="/buscar">Disponibilidad</NavLink>
            {tieneRol('HUESPED') && <NavLink to="/mis-reservas">Mis reservas</NavLink>}
            {tieneRol('RECEPCIONISTA', 'ADMINISTRADOR') && <NavLink to="/panel">Recepción</NavLink>}
            {tieneRol('PERSONAL_SERVICIO', 'ADMINISTRADOR') && <NavLink to="/preparacion">Preparación</NavLink>}
            {usuario ? (
              <div className="navegacion__usuario">
                <span className="navegacion__nombre">
                  {usuario.nombre.split(' ')[0]}
                  <small>{nombreRol[usuario.rol]}</small>
                </span>
                <button className="boton boton--pequeno boton--contorno" onClick={cerrarSesion}>
                  Salir
                </button>
              </div>
            ) : (
              <div className="navegacion__usuario">
                <NavLink to="/ingresar">Ingresar</NavLink>
                <Link className="boton boton--pequeno boton--fogata" to="/registro">
                  Crear cuenta
                </Link>
              </div>
            )}
          </nav>
        </div>
      </header>
      <AvisoServidorDespertando />
      <main id="contenido">
        <Outlet />
      </main>
      <footer className="pie">
        <div className="contenedor pie__fila">
          <div>
            <p className="pie__marca">
              <MarcaDomo tamano={22} /> JiyJu Glamping
            </p>
            <p>Vereda Palestina, Salento, Quindío</p>
          </div>
          <p className="pie__nota">
            Proyecto final de Programación Avanzada, Universidad del Quindío.
            <br />
            July Tatiana Cartagena Mazo y Jimmy Alonso Arango.
            <br />
            Fotografías de <a href="https://unsplash.com" target="_blank" rel="noreferrer">Unsplash</a>.
          </p>
        </div>
      </footer>
    </div>
  );
}
