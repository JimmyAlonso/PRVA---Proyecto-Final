package co.edu.uniquindio.sga.application.dto.response;

import java.util.List;

/**
 * 7.3: al cambiar la capacidad, las reservas ya creadas no se afectan, pero el sistema advierte
 * al administrador y las lista.
 */
public record DomoActualizadoResponse(DomoResponse domo, List<ReservaResumenResponse> reservasPorEncimaDeLaCapacidad,
                                      String advertencia) {
}
