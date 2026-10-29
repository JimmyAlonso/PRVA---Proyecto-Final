package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.port.NotificacionService;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.PoliticaCancelacionRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.service.RetencionService;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 7.5 Cancelación: desde PENDIENTE o CONFIRMADA, antes del registro. La retención se calcula
 * con la política congelada (RN-13) y queda como penalidad en el folio; lo pagado de más
 * queda como saldo a favor. La disponibilidad se libera de inmediato (RN-12).
 */
@Service
@Transactional
public class CancelarReservaUseCase {

    private final ReservaRepository reservaRepository;
    private final FolioRepository folioRepository;
    private final PoliticaCancelacionRepository politicaRepository;
    private final RetencionService retencionService;
    private final NotificacionService notificacionService;

    public CancelarReservaUseCase(ReservaRepository reservaRepository, FolioRepository folioRepository,
                                  PoliticaCancelacionRepository politicaRepository, RetencionService retencionService,
                                  NotificacionService notificacionService) {
        this.reservaRepository = reservaRepository;
        this.folioRepository = folioRepository;
        this.politicaRepository = politicaRepository;
        this.retencionService = retencionService;
        this.notificacionService = notificacionService;
    }

    public Reserva ejecutar(CodigoReserva codigo, String motivo, LocalDateTime momentoActual) {
        Reserva reserva = Buscar.reserva(reservaRepository, codigo);
        Folio folio = Buscar.folio(folioRepository, codigo);
        PoliticaCancelacion politica = Buscar.politicaCongelada(politicaRepository, reserva);

        Dinero retencion = retencionService.porCancelacion(reserva, politica, momentoActual);

        reserva.cancelar(motivo);                                          // RN-08, RN-12
        folio.liquidarTerminacion(reserva.getValorCongelado(), retencion, momentoActual.toLocalDate(),
                "cancelación de la reserva " + codigo.valor());

        reservaRepository.guardar(reserva);
        folioRepository.guardar(folio);
        notificacionService.reservaCancelada(reserva);                     // 7.8
        return reserva;
    }
}
