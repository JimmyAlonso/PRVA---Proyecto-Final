package co.edu.uniquindio.sga.application.dto.request;

import co.edu.uniquindio.sga.domain.valueobject.MotivoBloqueo;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

// 7.3 Bloqueos: rango de fechas (noches, ambos extremos incluidos) y motivo
public record RegistrarBloqueoRequest(
        @NotNull(message = "La fecha inicial es obligatoria") LocalDate desde,
        @NotNull(message = "La fecha final es obligatoria") LocalDate hasta,
        @NotNull(message = "El motivo es obligatorio (MANTENIMIENTO, USO_INTERNO o FUERA_DE_SERVICIO)") MotivoBloqueo motivo,
        String observacion
) {
}
