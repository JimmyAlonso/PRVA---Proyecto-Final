package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.entity.TitularDeLaReserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.util.List;

/**
 * 7.8 y 10.3.1: lo que envía un canal externo para crear una reserva. El valor NO lo manda
 * el canal: lo calcula el SGA con sus propias tarifas (RN-05).
 *
 * <p>Corrección: antes era un record genérico ({@code <RangoEstancia, Titular, ValorReserva>})
 * cuyos parámetros de tipo tapaban las clases reales y no compilaba al usarse.
 */
public record DatosReservaExterna(IdentificadorExterno identificador,
                                  IdentificacionDomo domo,
                                  Estancia estancia,
                                  TitularDeLaReserva titular,
                                  List<Ocupante> ocupantes,
                                  HoraEstimadaLlegada horaEstimadaLlegada) {

    public DatosReservaExterna {
        if (identificador == null || domo == null || estancia == null || titular == null) {
            throw new ReglaDominioException("Los datos de la reserva externa están incompletos.");
        }
        if (ocupantes == null || ocupantes.isEmpty()) {
            throw new ReglaDominioException("La reserva externa debe tener al menos un ocupante.");
        }
        ocupantes = List.copyOf(ocupantes);
    }
}
