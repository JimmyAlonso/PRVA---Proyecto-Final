package co.edu.uniquindio.sga.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** F-07: una versión de la política de cancelación. */
public record PoliticaCancelacionResponse(int version, LocalDate vigenteDesde, List<TramoRetencionResponse> tramos,
                                          BigDecimal retencionNoShow) {
}
