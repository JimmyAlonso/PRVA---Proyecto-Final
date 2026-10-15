package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

public record Deposito(Dinero monto) {
    public Deposito {
        if (monto == null || monto.esCero()) throw new ReglaDominioException("El depósito debe tener un monto mayor que cero.");
    }
}
