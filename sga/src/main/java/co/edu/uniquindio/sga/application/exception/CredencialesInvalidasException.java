package co.edu.uniquindio.sga.application.exception;

/**
 * 7.2: correo o contraseña incorrectos, o cuenta eliminada. El mensaje es el mismo en todos los
 * casos para no revelar qué correos están registrados. La API la traduce a 401.
 */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("Correo o contraseña incorrectos.");
    }
}
