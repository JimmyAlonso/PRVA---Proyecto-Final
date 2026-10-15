package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;
import java.util.List;

// 7.4 Cotización: basta con las fechas de nacimiento del grupo (la edad se calcula, 3.2)
public record CotizarEstanciaRequest(
        @NotNull(message = "La fecha de entrada es obligatoria") LocalDate fechaEntrada,
        @NotNull(message = "La fecha de salida es obligatoria") LocalDate fechaSalida,
        @NotEmpty(message = "Indique la fecha de nacimiento de cada integrante del grupo")
        List<@NotNull @Past LocalDate> fechasNacimiento
) {
}
