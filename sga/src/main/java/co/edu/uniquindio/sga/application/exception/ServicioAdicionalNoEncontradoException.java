package co.edu.uniquindio.sga.application.exception;

public class ServicioAdicionalNoEncontradoException extends RecursoNoEncontradoException {

    public ServicioAdicionalNoEncontradoException(String codigo) {
        super("No existe el servicio adicional con código: " + codigo);
    }
}
