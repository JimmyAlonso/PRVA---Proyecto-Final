package co.edu.uniquindio.sga.domain.service;
// EntregaDomoService.java — RN-10, RN-11, L-22
// Decide SI se entrega. Después el caso de uso llama reserva.registrarLlegada() y domo.marcarOcupado().

import co.edu.uniquindio.sga.domain.entity.Domo;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.DomoRepository;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;

import java.time.LocalDate;

public class EntregaDomoService {

        private final DomoRepository domoRepository;

        public EntregaDomoService(DomoRepository domoRepository) { this.domoRepository = domoRepository; }

        public void autorizarEntrega(Reserva reserva, Folio folio, LocalDate fechaActual) {
            if (reserva == null || folio == null || fechaActual == null)
                throw new ReglaDominioException("Reserva, folio y fecha actual son obligatorios.");
            if (!folio.getReserva().equals(reserva.getCodigo()))
                throw new ReglaDominioException("El folio no pertenece a la reserva.");
            if (reserva.getEstado() != EstadoReserva.CONFIRMADA)                                  // RN-10
                throw new ReglaDominioException("Solo se registra la llegada de una reserva CONFIRMADA.");
            if (fechaActual.isBefore(reserva.getEstancia().fechaEntrada()))                       // RN-10
                throw new ReglaDominioException("No se puede registrar la llegada antes de la fecha de entrada.");

            Domo domo = domoRepository.obtenerPorIdentificacion(reserva.getApartamento())
                    .orElseThrow(() -> new ReglaDominioException("No existe el domo de la reserva."));
            if (!domo.puedeRecibirGrupo())                                                        // RN-11
                throw new ReglaDominioException("El domo no está PREPARADO para recibir al grupo.");
            if (!folio.tieneDepositoRegistrado())                                                 // L-22
                throw new ReglaDominioException("El folio no registra el depósito: no se autoriza el ingreso.");
        }
    }
