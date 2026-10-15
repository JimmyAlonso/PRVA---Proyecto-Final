package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.util.List;

/**
 * Resultado de cotizar una estancia (7.4). Sirve para mostrar el valor antes de reservar y
 * para congelarlo en la reserva al crearla (3.5, RN-22).
 */
public record Cotizacion(List<DetalleNoche> noches) {

    public Cotizacion {
        if (noches == null || noches.isEmpty()) {
            throw new ReglaDominioException("La cotización debe tener al menos una noche.");
        }
        noches = List.copyOf(noches);
    }

    /** RN-05; 3.4: el redondeo al peso se aplica al total, nunca noche por noche. */
    public Dinero total() {
        return noches.stream()
                .map(DetalleNoche::subtotal)
                .reduce(Dinero.CERO, Dinero::sumar)
                .redondearAlPeso();
    }

    public Dinero totalRecargos() {
        return noches.stream()
                .map(DetalleNoche::recargoPersonaAdicional)
                .reduce(Dinero.CERO, Dinero::sumar)
                .redondearAlPeso();
    }
}
