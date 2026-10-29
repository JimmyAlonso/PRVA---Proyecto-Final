package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDate;

/** 7.5: una noche del calendario de ocupación de un domo. La referencia es el código de la reserva o el id del bloqueo. */
public record NocheCalendario(LocalDate fecha, EstadoNoche estado, String referencia) {

    public NocheCalendario {
        if (fecha == null || estado == null) {
            throw new ReglaDominioException("La noche del calendario debe indicar fecha y estado.");
        }
    }
}
