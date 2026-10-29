package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.port.NotificacionService;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.PlazoConfirmacion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RN-21 y 7.5: una reserva PENDIENTE que supera el plazo de confirmación (4 horas en JiyJu)
 * pasa a CANCELADA y libera la disponibilidad, sin intervención manual. Lo invoca una tarea
 * programada de la infraestructura. El vencimiento no es una cancelación del huésped: no
 * aplica retención.
 */
@Service
@Transactional
public class VencerReservasPendientesUseCase {

    private final ReservaRepository reservaRepository;
    private final FolioRepository folioRepository;
    private final NotificacionService notificacionService;
    private final PlazoConfirmacion plazo;

    public VencerReservasPendientesUseCase(ReservaRepository reservaRepository, FolioRepository folioRepository,
                                           NotificacionService notificacionService, PlazoConfirmacion plazo) {
        this.reservaRepository = reservaRepository;
        this.folioRepository = folioRepository;
        this.notificacionService = notificacionService;
        this.plazo = plazo;
    }

    /** @return las reservas que se vencieron en esta ejecución */
    public List<Reserva> ejecutar(LocalDateTime momentoActual) {
        List<Reserva> vencidas = reservaRepository.buscarPorEstado(EstadoReserva.PENDIENTE).stream()
                .filter(reserva -> reserva.venceElPlazo(plazo, momentoActual))
                .toList();
        for (Reserva reserva : vencidas) {
            reserva.vencerPorPlazo(plazo, momentoActual);
            folioRepository.obtenerPorReserva(reserva.getCodigo()).ifPresent(folio -> {
                liquidar(folio, reserva, momentoActual);
                folioRepository.guardar(folio);
            });
            reservaRepository.guardar(reserva);
            notificacionService.reservaCancelada(reserva);
        }
        return vencidas;
    }

    private void liquidar(Folio folio, Reserva reserva, LocalDateTime momentoActual) {
        folio.liquidarTerminacion(reserva.getValorCongelado(), Dinero.CERO, momentoActual.toLocalDate(),
                "vencimiento del plazo de confirmación de la reserva " + reserva.getCodigo().valor());
    }
}
