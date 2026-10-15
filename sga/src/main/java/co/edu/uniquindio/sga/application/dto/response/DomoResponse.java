package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;

import java.util.List;
import java.util.Map;

public record DomoResponse(
        String idDomo,
        String nombre,
        String descripcion,
        int dormitorios,
        int capacidadMaxima,
        List<String> caracteristicas,
        boolean tieneJacuzzi,
        Integer horasPrecalentamientoJacuzzi,
        Integer pesoMaximoMallaKg,
        Integer kitsFogata,
        EstadoOperativo estadoOperativo,
        boolean activo,
        Map<String, Long> tarifasPorTemporada
) {
}
