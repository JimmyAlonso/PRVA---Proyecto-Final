package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.entity.Domo;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.repository.DomoRepository;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;
import co.edu.uniquindio.sga.domain.service.CotizadorEstanciaService;
import co.edu.uniquindio.sga.domain.service.DisponibilidadDomoService;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Cotizacion;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;
import co.edu.uniquindio.sga.domain.valueobject.UmbralEdadFacturable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * RN-14 y 7.5 Modificación: cambiar fechas, ocupantes o domo de una reserva PENDIENTE o
 * CONFIRMADA. Repite las seis validaciones de creación (la reserva no compite consigo misma),
 * recalcula con las tarifas vigentes y registra la diferencia como ajuste en el folio. La
 * política congelada no cambia (RN-22).
 */
@Service
@Transactional
public class ModificarReservaUseCase {

    private final ReservaRepository reservaRepository;
    private final DomoRepository domoRepository;
    private final FolioRepository folioRepository;
    private final TemporadaRepository temporadaRepository;
    private final DisponibilidadDomoService disponibilidadService;
    private final CotizadorEstanciaService cotizador;
    private final UmbralEdadFacturable umbral;

    public ModificarReservaUseCase(ReservaRepository reservaRepository, DomoRepository domoRepository,
                                   FolioRepository folioRepository, TemporadaRepository temporadaRepository,
                                   DisponibilidadDomoService disponibilidadService, CotizadorEstanciaService cotizador,
                                   UmbralEdadFacturable umbral) {
        this.reservaRepository = reservaRepository;
        this.domoRepository = domoRepository;
        this.folioRepository = folioRepository;
        this.temporadaRepository = temporadaRepository;
        this.disponibilidadService = disponibilidadService;
        this.cotizador = cotizador;
        this.umbral = umbral;
    }

    /** Los parámetros null conservan el valor actual de la reserva. */
    /**
     * Modificación desde la API: las fechas que no llegan se conservan y, si llegan acompañantes,
     * reemplazan a los actuales manteniendo al titular como ocupante (3.2).
     */
    public Reserva ejecutar(CodigoReserva codigo, IdentificacionDomo nuevoDomo, LocalDate nuevaEntrada,
                            LocalDate nuevaSalida, List<Ocupante> nuevosAcompanantes, LocalDateTime momentoActual) {
        Reserva reserva = Buscar.reserva(reservaRepository, codigo);
        return ejecutar(codigo, nuevoDomo, reserva.estanciaCon(nuevaEntrada, nuevaSalida),
                nuevosAcompanantes == null ? null : reserva.conAcompanantes(nuevosAcompanantes), momentoActual);
    }

    public Reserva ejecutar(CodigoReserva codigo, IdentificacionDomo nuevoDomo, Estancia nuevaEstancia,
                            List<Ocupante> nuevosOcupantes, LocalDateTime momentoActual) {
        Reserva reserva = Buscar.reserva(reservaRepository, codigo);
        Folio folio = Buscar.folio(folioRepository, codigo);

        IdentificacionDomo idDomo = nuevoDomo != null ? nuevoDomo : reserva.getDomo();
        Estancia estancia = nuevaEstancia != null ? nuevaEstancia : reserva.getEstancia();
        List<Ocupante> ocupantes = nuevosOcupantes != null ? nuevosOcupantes : reserva.getOcupantes();

        estancia.verificarQueNoEmpiezaAntesDe(momentoActual.toLocalDate());               // RN-04
        Domo domo = Buscar.domo(domoRepository, idDomo);
        List<Temporada> temporadas = temporadaRepository.buscarTodas();
        domo.verificarQueAceptaReservas(temporadas);
        disponibilidadService.verificarDisponibilidad(domo, estancia, ocupantes.size(), codigo);  // RN-01/02/07/20

        // RN-14: se recalcula con las tarifas vigentes al momento de la modificación
        Cotizacion nuevaCotizacion = cotizador.cotizar(domo, estancia, ocupantes, temporadas);
        Dinero valorAnterior = reserva.modificar(idDomo, domo.getCapacidad(), estancia, ocupantes, nuevaCotizacion,
                umbral, momentoActual.toLocalDate());
        folio.registrarAjuste(valorAnterior, reserva.getValorCongelado(), momentoActual.toLocalDate(),
                "reserva " + codigo.valor());

        reservaRepository.guardar(reserva);
        folioRepository.guardar(folio);
        return reserva;
    }
}
