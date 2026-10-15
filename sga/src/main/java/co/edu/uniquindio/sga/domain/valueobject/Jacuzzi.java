package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

public record Jacuzzi(int horasPrecalentamiento) {
    public Jacuzzi {
        if (horasPrecalentamiento <= 0) throw new ReglaDominioException("Las horas de precalentamiento deben ser mayores que cero.");
    }
}
