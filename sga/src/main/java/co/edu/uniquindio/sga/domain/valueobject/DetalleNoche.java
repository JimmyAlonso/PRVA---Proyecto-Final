package co.edu.uniquindio.sga.domain.valueobject;

import java.time.LocalDate;
import java.util.List;

public record DetalleNoche(LocalDate fecha, IdTemporada temporada, Dinero tarifaAplicada,
                           int ocupantesFacturables, Dinero recargoPersonaAdicional, Dinero subtotal) {}

