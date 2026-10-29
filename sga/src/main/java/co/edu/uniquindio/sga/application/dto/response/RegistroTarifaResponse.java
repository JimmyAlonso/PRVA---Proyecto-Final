package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalDate;

/** 7.4 Histórico de tarifas: valor anterior y fecha en que fue reemplazado. */
public record RegistroTarifaResponse(String temporada, long valorPorOcupanteNoche, LocalDate reemplazadaEl) {
}
