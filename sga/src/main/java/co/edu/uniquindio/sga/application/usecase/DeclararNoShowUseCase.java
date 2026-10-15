package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.PoliticaCancelacionRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.service.RetencionService;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.HoraLimiteNoShow;

import java.time.LocalDateTime;

// Disparador distinto de la cancelación: no se fusiona con CancelarReservaUseCase (Guía 06, 3.3)
public class DeclararNoShowUseCase {

    private final ReservaRepository reservaRepository;
    private final FolioRepository folioRepository;
    private final PoliticaCancelacionRepository politicaRepository;
    private final RetencionService retencionService;
    // L-15: configuración del alojamiento (20:00), nunca una constante
    private final HoraLimiteNoShow horaLimite;

    public DeclararNoShowUseCase(ReservaRepository reservaRepository, FolioRepository folioRepository,
                                 PoliticaCancelacionRepository politicaRepository, RetencionService retencionService,
                                 HoraLimiteNoShow horaLimite) {
        this.reservaRepository = reservaRepository;
        this.folioRepository = folioRepository;
        this.politicaRepository = politicaRepository;
        this.retencionService = retencionService;
        this.horaLimite = horaLimite;
    }

    public Reserva ejecutar(CodigoReserva codigo, LocalDateTime momentoActual) {
        Reserva reserva = Buscar.reserva(reservaRepository, codigo);
        Folio folio = Buscar.folio(folioRepository, codigo);
        PoliticaCancelacion politica = Buscar.politicaCongelada(politicaRepository, reserva);   // RN-13

        Dinero retencion = retencionService.porNoShow(reserva, politica);

        reserva.declararNoShow(momentoActual, horaLimite);                 // RN-08, L-15
        folio.liquidarTerminacion(reserva.getValorCongelado(), retencion, momentoActual.toLocalDate(),
                "no-show de la reserva " + codigo.valor());

        reservaRepository.guardar(reserva);
        folioRepository.guardar(folio);
        return reserva;
    }
}
