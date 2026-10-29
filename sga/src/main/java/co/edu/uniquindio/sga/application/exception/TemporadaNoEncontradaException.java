package co.edu.uniquindio.sga.application.exception;

import co.edu.uniquindio.sga.domain.valueobject.IdTemporada;

public class TemporadaNoEncontradaException extends RecursoNoEncontradoException {

    public TemporadaNoEncontradaException(IdTemporada id) {
        super("No se encontró la temporada: " + id.valor());
    }
}
