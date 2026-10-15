package co.edu.uniquindio.sga.application.exception;

import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;

// Equivale a ApartamentoNoEncontradoException del repositorio guía, con el término del equipo
public class DomoNoEncontradoException extends RecursoNoEncontradoException {

    public DomoNoEncontradoException(IdentificacionDomo identificacion) {
        super("No se encontró el domo con identificación: " + identificacion.valor());
    }
}
