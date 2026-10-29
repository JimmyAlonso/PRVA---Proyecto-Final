package co.edu.uniquindio.sga.application.dto.response;

import java.util.List;

/** 7.4: temporada con sus rangos. La base no tiene rangos: cubre las fechas no asignadas a otra. */
public record TemporadaResponse(String id, String nombre, boolean base, List<PeriodoResponse> periodos) {
}
