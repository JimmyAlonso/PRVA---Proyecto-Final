package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalTime;

// L-15: hora del día de entrada a partir de la cual puede declararse el no-show.
// JiyJu Glamping: 20:00 (Ficha A.4). El valor es configuración, nunca constante.
public record HoraLimiteNoShow(LocalTime hora) {

    public HoraLimiteNoShow {
        if (hora == null) {
            throw new ReglaDominioException("La hora límite para declarar no-show es obligatoria.");
        }
    }
}
