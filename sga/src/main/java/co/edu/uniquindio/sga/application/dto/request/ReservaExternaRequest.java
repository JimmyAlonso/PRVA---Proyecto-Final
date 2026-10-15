package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 10.3.1: contrato del canal externo. El canal se identifica por la cabecera X-Canal-Id
 * (con su credencial X-Canal-Credencial); aquí llega el identificador propio del canal.
 * El valor no lo manda el canal: lo calcula el SGA.
 */
public record ReservaExternaRequest(
        @NotBlank(message = "El identificador externo de la reserva es obligatorio")
        String idTransaccionExterna,

        @NotBlank(message = "La identificación del domo es obligatoria")
        String idDomo,

        @NotNull(message = "La fecha de entrada es obligatoria")
        LocalDate fechaEntrada,

        @NotNull(message = "La fecha de salida es obligatoria")
        LocalDate fechaSalida,

        LocalTime horaEstimadaLlegada,

        @NotNull(message = "La reserva externa debe tener titular")
        @Valid
        TitularRequest titular,

        List<@Valid OcupanteRequest> ocupantes
) {
}
