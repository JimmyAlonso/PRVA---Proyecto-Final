package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.entity.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

public record DatosReservaExterna<RangoEstancia, Titular, ValorReserva>(
        CodigoReserva codigo,
        IdentificacionDomo domo,   // el tipo debe coincidir con lo que piden los repos
        RangoEstancia estancia,
        Titular titular,
        int ocupantes,
        ValorReserva valor,
        PoliticaCancelacion politica) {

    public DatosReservaExterna {
        if (codigo == null || domo == null || estancia == null || titular == null
                || valor == null || politica == null)
            throw new ReglaDominioException("Los datos de la reserva externa están incompletos.");
        if (ocupantes <= 0)
            throw new ReglaDominioException("Debe haber al menos un ocupante.");
    }
}
