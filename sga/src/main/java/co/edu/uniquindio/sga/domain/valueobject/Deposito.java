package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

// Glosario propio y L-22: monto retenido en el folio para cubrir daños a las instalaciones.
// El valor es configuración del alojamiento (sga.alojamiento.deposito).
public record Deposito(Dinero monto) {

    public Deposito {
        if (monto == null || monto.esCero()) {
            throw new ReglaDominioException("El depósito debe tener un monto mayor que cero.");
        }
    }
}
