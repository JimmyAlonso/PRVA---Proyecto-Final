package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.entity.Domo;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.DomoRepository;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.service.LiquidacionSalidaService;
import co.edu.uniquindio.sga.domain.valueobject.AutorizacionCierre;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;

public class RegistrarSalidaUseCase {

    private final ReservaRepository reservaRepository;
    private final DomoRepository domoRepository;
    private final FolioRepository folioRepository;
    private final LiquidacionSalidaService liquidacionService;

    public RegistrarSalidaUseCase(ReservaRepository reservaRepository, DomoRepository domoRepository,
                                  FolioRepository folioRepository, LiquidacionSalidaService liquidacionService) {
        this.reservaRepository = reservaRepository;
        this.domoRepository = domoRepository;
        this.folioRepository = folioRepository;
        this.liquidacionService = liquidacionService;
    }

    /**
     * @param autorizacion opcional. Solo se exige si el saldo del folio no es cero (RN-17);
     *                     el caso de uso no decide si autorizar, solo la transporta.
     */
    public Reserva ejecutar(CodigoReserva codigo, AutorizacionCierre autorizacion) {
        Reserva reserva = Buscar.reserva(reservaRepository, codigo);
        Domo domo = Buscar.domo(domoRepository, reserva.getDomo());
        Folio folio = Buscar.folio(folioRepository, codigo);

        liquidacionService.registrarSalida(reserva, domo, folio, autorizacion);

        reservaRepository.guardar(reserva);
        domoRepository.guardar(domo);
        folioRepository.guardar(folio);
        return reserva;
    }
}
