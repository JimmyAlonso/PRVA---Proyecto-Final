package co.edu.uniquindio.sga.application.dto.request;

import co.edu.uniquindio.sga.domain.valueobject.MedioPago;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

// RN-15: todo pago tiene medio y fecha. Si no llega fecha se usa la de hoy (Colombia).
public record RegistrarPagoRequest(
        @NotNull(message = "El monto es obligatorio")
        @Positive(message = "El monto debe ser mayor que cero")
        Long monto,

        @NotNull(message = "El medio de pago es obligatorio (TRANSFERENCIA, TARJETA o EFECTIVO)")
        MedioPago medio,

        LocalDate fecha
) {
}
