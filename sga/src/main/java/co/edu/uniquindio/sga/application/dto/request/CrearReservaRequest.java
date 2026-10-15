package co.edu.uniquindio.sga.application.dto.request;

import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record CrearReservaRequest(
        @NotBlank(message = "La identificacion del apartamento es obligatoria")
        String identificacionApartamento,

        @NotNull(message = "La fecha de entrada es obligatoria")
        LocalDate fechaEntrada,

        @NotNull(message = "La fecha de salida es obligatoria")
        LocalDate fechaSalida,

        @NotNull(message = "El canal de origen es obligatorio")
        CanalOrigen canalOrigen,

        //No es obligatorio
        LocalTime horaEstimadaLlegada,

        @NotNull(message = "La reserva debe tener titular")
        @Valid
        OcupanteRequest titular,

        @NotEmpty(message = "La reserva debe tener al menos un ocupante")
        List<@Valid OcupanteRequest> ocupantes
) {
}