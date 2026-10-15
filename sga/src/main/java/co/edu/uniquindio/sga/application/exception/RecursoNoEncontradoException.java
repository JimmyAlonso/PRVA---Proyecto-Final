package co.edu.uniquindio.sga.application.exception;

// Base común de "no encontrado": la API la traduce a 404 en un solo lugar
public abstract class RecursoNoEncontradoException extends RuntimeException {

    protected RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
