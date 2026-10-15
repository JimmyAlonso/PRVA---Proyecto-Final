package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

public record Fogata(int kitsLena) {
    public Fogata {
        if (kitsLena < 1) throw new ReglaDominioException("La fogata debe incluir al menos un kit de leña.");
    }
}
