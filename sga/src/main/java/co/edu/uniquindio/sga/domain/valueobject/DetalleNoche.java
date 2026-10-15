package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDate;

// 7.4 Cotización: desglose noche por noche (fecha, temporada, tarifa, facturables, subtotal).
public record DetalleNoche(LocalDate fecha,
                           IdTemporada temporada,
                           String nombreTemporada,
                           Dinero tarifaAplicada,
                           int ocupantesFacturables,
                           Dinero recargoPersonaAdicional,
                           Dinero subtotal) {

    public DetalleNoche {
        if (fecha == null || temporada == null || tarifaAplicada == null
                || recargoPersonaAdicional == null || subtotal == null) {
            throw new ReglaDominioException("El detalle de la noche está incompleto.");
        }
    }
}
