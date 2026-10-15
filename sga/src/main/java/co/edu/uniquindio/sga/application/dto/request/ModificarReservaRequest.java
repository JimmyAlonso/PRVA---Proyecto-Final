package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;

/**
 * PUT /api/v1/reservas/{codigo} (RN-14). Todos los campos son opcionales: lo que no venga se
 * conserva. Si cambian las fechas deben venir ambas. Si vienen ocupantes, es la lista completa
 * de acompañantes (el titular se agrega solo).
 */
public record ModificarReservaRequest(
        String idDomo,
        LocalDate fechaEntrada,
        LocalDate fechaSalida,
        List<@Valid OcupanteRequest> ocupantes
) {
}
