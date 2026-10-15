package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.Duration;
import java.time.LocalDate;

// RN-20: tiempo mínimo entre la salida de un grupo y la entrada del siguiente
// L-13: JiyJu Glamping usa 4 horas (Ficha A.4); el valor es configuración, nunca constante
public record TiempoPreparacion(int horas) {

    public TiempoPreparacion {
        if (horas < 0) {
            throw new ReglaDominioException("El tiempo de preparación no puede ser negativo.");
        }
    }

    /**
     * RN-20 y sección 3.3: entre la hora de salida del día {@code fechaSalidaAnterior} y la
     * hora de entrada del día {@code fechaEntradaSiguiente} debe caber el tiempo de preparación.
     * Si la salida y la entrada son el mismo día, la ventana es la que hay entre ambas horas.
     */
    public boolean seRespetaEntre(LocalDate fechaSalidaAnterior, LocalDate fechaEntradaSiguiente,
                                  HorarioAlojamiento horario) {
        if (fechaEntradaSiguiente.isBefore(fechaSalidaAnterior)) {
            return false;
        }
        long horasDisponibles = Duration.between(
                fechaSalidaAnterior.atTime(horario.horaSalida()),
                fechaEntradaSiguiente.atTime(horario.horaEntrada())).toHours();
        return horasDisponibles >= horas;
    }
}
