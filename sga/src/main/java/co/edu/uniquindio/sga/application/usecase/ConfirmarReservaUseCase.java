package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.entity.Domo;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.port.NotificacionService;
import co.edu.uniquindio.sga.domain.repository.DomoRepository;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.service.ConfirmacionReservaService;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.HoraEstimadaLlegada;

/**
 * PENDIENTE → CONFIRMADA: RN-08, RN-09, anticipo del 50 % (L-11) y alistamiento del
 * Jacuzzi (L-21). Si llega la hora estimada de llegada, se registra antes de confirmar.
 */
public class ConfirmarReservaUseCase {

    private final ReservaRepository reservaRepository;
    private final DomoRepository domoRepository;
    private final FolioRepository folioRepository;
    private final ConfirmacionReservaService confirmacionService;
    private final NotificacionService notificacionService;

    public ConfirmarReservaUseCase(ReservaRepository reservaRepository, DomoRepository domoRepository,
                                   FolioRepository folioRepository, ConfirmacionReservaService confirmacionService,
                                   NotificacionService notificacionService) {
        this.reservaRepository = reservaRepository;
        this.domoRepository = domoRepository;
        this.folioRepository = folioRepository;
        this.confirmacionService = confirmacionService;
        this.notificacionService = notificacionService;
    }

    // Devuelve la reserva para que la API responda con su detalle actualizado (Guía 07)
    public Reserva ejecutar(CodigoReserva codigo, HoraEstimadaLlegada horaEstimadaLlegada) {
        Reserva reserva = Buscar.reserva(reservaRepository, codigo);
        Domo domo = Buscar.domo(domoRepository, reserva.getDomo());
        Folio folio = Buscar.folio(folioRepository, codigo);

        if (horaEstimadaLlegada != null) {
            reserva.registrarHoraEstimadaLlegada(horaEstimadaLlegada);
        }
        confirmacionService.confirmar(reserva, domo, folio);

        reservaRepository.guardar(reserva);
        notificacionService.reservaConfirmada(reserva);   // 7.8: notificar la confirmación
        return reserva;
    }
}
