import { Link } from 'react-router-dom';

export default function NoEncontrada() {
  return (
    <section className="contenedor pagina-estrecha">
      <h1>Este sendero no lleva a ningún domo</h1>
      <p className="texto-suave">La dirección que abriste no existe en el sitio.</p>
      <Link className="boton boton--fogata" to="/">Volver al inicio</Link>
    </section>
  );
}
