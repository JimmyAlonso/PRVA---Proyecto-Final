package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Domo;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;

import java.time.LocalDate;

/**
 * Registro de llegada (check-in): RN-10, RN-11 y regla propia L-22 (depósito en el folio).
 *
 * <p>Dos agregados cambian en la misma operación (Reserva → EN_CURSO, Domo → OCUPADO).
 * Primero se verifica todo lo que puede fallar y solo después se modifican, para no dejar
 * uno cambiado y el otro no.
 *
 * <p>Corrección: antes recibía el DomoRepository y buscaba el domo. Ahora recibe los agregados
 * ya cargados; los repositorios los usa el caso de uso.
 */
public class EntregaDomoService {

    public void autorizarEntrega(Reserva reserva, Domo domo, Folio folio, LocalDate fechaActual) {
        if (reserva == null || domo == null || folio == null || fechaActual == null) {
            throw new ReglaDominioException("Reserva, domo, folio y fecha actual son obligatorios.");
        }
        if (!reserva.getDomo().equals(domo.getIdentificacion())) {
            throw new ReglaDominioException("El domo no corresponde al de la reserva.");
        }
        if (!folio.getReserva().equals(reserva.getCodigo())) {
            throw new ReglaDominioException("El folio no pertenece a la reserva.");
        }
        if (reserva.getEstado() != EstadoReserva.CONFIRMADA) {                                  // RN-10
            throw new ReglaDominioException("Solo se registra la llegada de una reserva CONFIRMADA.");
        }
        if (fechaActual.isBefore(reserva.getEstancia().fechaEntrada())) {                       // RN-10
            throw new ReglaDominioException("No se puede registrar la llegada antes de la fecha de entrada.");
        }
        if (!domo.puedeRecibirGrupo()) {                                                        // RN-11
            throw new ReglaDominioException("El domo no está PREPARADO para recibir al grupo (estado: "
                    + domo.getEstadoOperativo() + ").");
        }
        if (!folio.tieneDepositoRegistrado()) {                                                 // L-22
            throw new ReglaDominioException("L-22: el folio no registra el depósito; no se autoriza el ingreso.");
        }
    }

    public void registrarLlegada(Reserva reserva, Domo domo, Folio folio, LocalDate fechaActual) {
        autorizarEntrega(reserva, domo, folio, fechaActual);
        reserva.registrarLlegada(fechaActual);
        domo.marcarOcupado();
    }
}
