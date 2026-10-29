package co.edu.uniquindio.sga.application.exception;

import co.edu.uniquindio.sga.domain.valueobject.IdBloqueo;

public class BloqueoNoEncontradoException extends RecursoNoEncontradoException {

    public BloqueoNoEncontradoException(IdBloqueo id) {
        super("No se encontró el bloqueo: " + id.valor());
    }
}
