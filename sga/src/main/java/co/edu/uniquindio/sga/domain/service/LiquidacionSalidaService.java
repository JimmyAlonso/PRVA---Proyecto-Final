package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Domo;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.DomoRepository;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;

import java.time.LocalDate;

// LiquidacionSalidaService.java — RN-16, RN-17 (coordina Reserva + Folio + Domo)
public class LiquidacionSalidaService {
    private final ReservaRepository reservaRepository;
    private final FolioRepository folioRepository;
    private final DomoRepository domoRepository;

    public LiquidacionSalidaService(ReservaRepository reservaRepository, FolioRepository folioRepository,
                                    DomoRepository domoRepository) {
        this.reservaRepository = reservaRepository;
        this.folioRepository = folioRepository;
        this.domoRepository = domoRepository;
    }

    /** autorizacionAdmin solo se exige si el saldo es distinto de cero. */
    public void liquidarSalida(Reserva reserva, Folio folio, LocalDate fechaSalida, String autorizacionAdmin) {
        if (reserva == null || folio == null || fechaSalida == null)
            throw new ReglaDominioException("Reserva, folio y fecha de salida son obligatorios.");
        if (!folio.getReserva().equals(reserva.getCodigo()))
            throw new ReglaDominioException("El folio no pertenece a la reserva.");

        boolean sinAutorizacion = autorizacionAdmin == null || autorizacionAdmin.isBlank();
        if (!folio.puedeCerrarse() && sinAutorizacion)                  // RN-17, antes de mutar nada
            throw new ReglaDominioException("El folio tiene saldo pendiente: se requiere autorización del administrador.");

        Domo domo = domoRepository.obtenerPorIdentificacion(reserva.getApartamento())
                .orElseThrow(() -> new ReglaDominioException("No existe el domo de la reserva."));

        reserva.registrarSalida(fechaSalida);     // EN_CURSO -> FINALIZADA
        folio.cerrar(autorizacionAdmin);
        domo.marcarPendientePreparacion();        // sección 7.6

        reservaRepository.guardar(reserva);
        folioRepository.guardar(folio);
        domoRepository.guardar(domo);
    }
}
