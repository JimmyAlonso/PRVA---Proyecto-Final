package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;

import java.time.LocalDate;

/**
 * 7.5 Listados: filtros opcionales (null = sin filtro). El rango de fechas selecciona las
 * reservas cuya estancia toca al menos un día entre {@code desde} y {@code hasta}.
 */
public record CriteriosBusquedaReserva(EstadoReserva estado,
                                       IdentificacionDomo domo,
                                       CanalOrigen canal,
                                       DocumentoIdentidad documentoTitular,
                                       LocalDate desde,
                                       LocalDate hasta) {

    public static CriteriosBusquedaReserva sinFiltros() {
        return new CriteriosBusquedaReserva(null, null, null, null, null, null);
    }

    public boolean cumple(Reserva reserva) {
        if (estado != null && reserva.getEstado() != estado) return false;
        if (domo != null && !reserva.getDomo().equals(domo)) return false;
        if (canal != null && reserva.getCanalOrigen() != canal) return false;
        if (documentoTitular != null && !reserva.getTitular().getDocumento().equals(documentoTitular)) return false;
        if (desde != null && !reserva.getEstancia().fechaSalida().isAfter(desde)) return false;
        if (hasta != null && reserva.getEstancia().fechaEntrada().isAfter(hasta)) return false;
        return true;
    }
}
