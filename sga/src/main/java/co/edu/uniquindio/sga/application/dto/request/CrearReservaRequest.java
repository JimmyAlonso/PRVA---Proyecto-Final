package co.edu.uniquindio.sga.application.dto.request;

import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * POST /api/v1/reservas (canales PORTAL y DIRECTO).
 * Corrección respecto del repositorio guía: @NotBlank no aplica a un enum (lanza error en
 * ejecución); para canalOrigen se usa @NotNull.
 */
public record CrearReservaRequest(
        @NotBlank(message = "La identificación del domo es obligatoria")
        String idDomo,

        @NotNull(message = "La fecha de entrada es obligatoria")
        LocalDate fechaEntrada,

        @NotNull(message = "La fecha de salida es obligatoria")
        LocalDate fechaSalida,

        @NotNull(message = "El canal de origen es obligatorio (PORTAL o DIRECTO)")
        CanalOrigen canalOrigen,

        // No es obligatoria al crear; sí para confirmar (RN-09)
        LocalTime horaEstimadaLlegada,

        @NotNull(message = "La reserva debe tener titular")
        @Valid
        TitularRequest titular,

        // Acompañantes. Puede venir vacía si el titular viaja solo.
        List<@Valid OcupanteRequest> ocupantes
) {
}
