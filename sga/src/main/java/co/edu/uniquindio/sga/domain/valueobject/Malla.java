package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

public record Malla(int pesoMaximoKg) {
    public Malla {
        if (pesoMaximoKg <= 0) throw new ReglaDominioException("El peso máximo de la malla debe ser mayor que cero.");
    }
}
