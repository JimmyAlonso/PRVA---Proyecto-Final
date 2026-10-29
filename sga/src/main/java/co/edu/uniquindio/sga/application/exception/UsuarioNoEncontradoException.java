package co.edu.uniquindio.sga.application.exception;

import co.edu.uniquindio.sga.domain.valueobject.IdUsuario;

public class UsuarioNoEncontradoException extends RecursoNoEncontradoException {

    public UsuarioNoEncontradoException(IdUsuario id) {
        super("No se encontró el usuario: " + id.valor());
    }
}
