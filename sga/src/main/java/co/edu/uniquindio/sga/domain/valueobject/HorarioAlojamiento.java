package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.Duration;
import java.time.LocalTime;

/**
 * L-12: horas de entrada (check-in) y salida (check-out) del alojamiento.
 * JiyJu Glamping: entrada 15:00, salida 11:00 (Ficha A.4).
 */
public record HorarioAlojamiento(LocalTime horaEntrada, LocalTime horaSalida) {

    public HorarioAlojamiento {
        if (horaEntrada == null || horaSalida == null) {
            throw new ReglaDominioException("El alojamiento debe definir hora de entrada y hora de salida.");
        }
    }

    /**
     * Sección 3.3: horas disponibles entre la salida de un grupo y la entrada del siguiente
     * el mismo día. Si la hora de entrada no es posterior a la de salida, no hay ventana.
     */
    public long horasVentanaMismoDia() {
        if (!horaEntrada.isAfter(horaSalida)) {
            return 0;
        }
        return Duration.between(horaSalida, horaEntrada).toMinutes() / 60;
    }
}
