package co.edu.uniquindio.sga.application.dto.response;

import java.util.List;

// Resultado de búsqueda (tarjeta) y de cotización: valor estimado con su desglose (7.4, 7.5)
public record CotizacionDomoResponse(
        String idDomo,
        String nombre,
        int capacidadMaxima,
        boolean tieneJacuzzi,
        List<String> caracteristicas,
        int noches,
        long recargoOcupanteAdicional,
        long valorTotalEstancia,
        String moneda,
        List<DetalleNocheResponse> desglose
) {
}
