package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.entity.Domo;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.DomoRepository;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.service.EntregaDomoService;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Check-in: dos agregados cambian (Reserva → EN_CURSO, Domo → OCUPADO). La regla que los
 * relaciona (RN-10, RN-11, L-22) la coordina EntregaDomoService; el caso de uso solo trae los
 * agregados y los guarda. @Transactional garantiza que ambos queden guardados o ninguno.
 */
@Service
@Transactional
public class RegistrarLlegadaUseCase {

    private final ReservaRepository reservaRepository;
    private final DomoRepository domoRepository;
    private final FolioRepository folioRepository;
    private final EntregaDomoService entregaService;

    public RegistrarLlegadaUseCase(ReservaRepository reservaRepository, DomoRepository domoRepository,
                                   FolioRepository folioRepository, EntregaDomoService entregaService) {
        this.reservaRepository = reservaRepository;
        this.domoRepository = domoRepository;
        this.folioRepository = folioRepository;
        this.entregaService = entregaService;
    }

    public Reserva ejecutar(CodigoReserva codigo, LocalDate fechaActual) {
        Reserva reserva = Buscar.reserva(reservaRepository, codigo);
        Domo domo = Buscar.domo(domoRepository, reserva.getDomo());
        Folio folio = Buscar.folio(folioRepository, codigo);

        entregaService.registrarLlegada(reserva, domo, folio, fechaActual);

        reservaRepository.guardar(reserva);
        domoRepository.guardar(domo);
        return reserva;
    }
}
