package co.edu.uniquindio.sga.application.exception;

import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.IdentificadorExterno;

// Corrección: antes se llamaba ReservaNoEncontradadException ("dad" de más)
public class ReservaNoEncontradaException extends RecursoNoEncontradoException {

    public ReservaNoEncontradaException(CodigoReserva codigo) {
        super("No se encontró la reserva con código: " + codigo.valor());
    }

    public ReservaNoEncontradaException(IdentificadorExterno identificador) {
        super("No se encontró la reserva externa " + identificador.valor() + " del canal " + identificador.canal());
    }
}
