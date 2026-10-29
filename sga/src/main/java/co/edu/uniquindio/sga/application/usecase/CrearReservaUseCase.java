package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.PoliticaCancelacionNoEncontradaException;
import co.edu.uniquindio.sga.domain.entity.Domo;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.entity.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.entity.TitularDeLaReserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.port.NotificacionService;
import co.edu.uniquindio.sga.domain.repository.DomoRepository;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.GeneradorCodigoReserva;
import co.edu.uniquindio.sga.domain.repository.PoliticaCancelacionRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;
import co.edu.uniquindio.sga.domain.service.CotizadorEstanciaService;
import co.edu.uniquindio.sga.domain.service.DisponibilidadDomoService;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.Cotizacion;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.HoraEstimadaLlegada;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;
import co.edu.uniquindio.sga.domain.valueobject.UmbralEdadFacturable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 7.5 Creación de reserva por los canales PORTAL o DIRECTO (el EXTERNO entra por
 * RecibirReservaExternaUseCase). Valida en el orden exigido: 1) salida posterior a entrada
 * (Estancia, RN-03), 2) entrada no anterior a hoy (RN-04), 3) domo activo y con tarifas
 * completas, 4) capacidad (RN-02), 5) sin solapamientos ni bloqueos (RN-01, RN-07),
 * 6) tiempo de preparación (RN-20). Luego congela valor y política (RN-22), abre el folio
 * (F-08) y notifica al titular. La reserva nace PENDIENTE.
 */
@Service
@Transactional
public class CrearReservaUseCase {

    private final ReservaRepository reservaRepository;
    private final DomoRepository domoRepository;
    private final FolioRepository folioRepository;
    private final TemporadaRepository temporadaRepository;
    private final PoliticaCancelacionRepository politicaRepository;
    private final GeneradorCodigoReserva generadorCodigo;
    private final DisponibilidadDomoService disponibilidadService;
    private final CotizadorEstanciaService cotizador;
    private final NotificacionService notificacionService;
    // L-09: configuración del alojamiento, nunca una constante
    private final UmbralEdadFacturable umbral;

    public CrearReservaUseCase(ReservaRepository reservaRepository, DomoRepository domoRepository,
                               FolioRepository folioRepository, TemporadaRepository temporadaRepository,
                               PoliticaCancelacionRepository politicaRepository, GeneradorCodigoReserva generadorCodigo,
                               DisponibilidadDomoService disponibilidadService, CotizadorEstanciaService cotizador,
                               NotificacionService notificacionService, UmbralEdadFacturable umbral) {
        this.reservaRepository = reservaRepository;
        this.domoRepository = domoRepository;
        this.folioRepository = folioRepository;
        this.temporadaRepository = temporadaRepository;
        this.politicaRepository = politicaRepository;
        this.generadorCodigo = generadorCodigo;
        this.disponibilidadService = disponibilidadService;
        this.cotizador = cotizador;
        this.notificacionService = notificacionService;
        this.umbral = umbral;
    }

    /** La hora estimada de llegada es opcional al crear; es obligatoria para confirmar (RN-09). */
    public Reserva ejecutar(IdentificacionDomo idDomo, Estancia estancia, HoraEstimadaLlegada horaEstimadaLlegada,
                            TitularDeLaReserva titular, List<Ocupante> ocupantes, CanalOrigen canalOrigen,
                            LocalDateTime momentoActual) {
        if (canalOrigen == CanalOrigen.EXTERNO) {
            throw new ReglaDominioException("Las reservas del canal EXTERNO se reciben por la integración de canales.");
        }
        // 1) RN-03 ya lo garantizó el constructor de Estancia. 2) RN-04
        estancia.verificarQueNoEmpiezaAntesDe(momentoActual.toLocalDate());

        // 3) domo activo y con tarifas completas
        Domo domo = Buscar.domo(domoRepository, idDomo);
        List<Temporada> temporadas = temporadaRepository.buscarTodas();
        domo.verificarQueAceptaReservas(temporadas);

        // 4), 5) y 6): RN-02, RN-01, RN-07 y RN-20
        disponibilidadService.verificarDisponibilidad(domo, estancia, ocupantes == null ? 0 : ocupantes.size(), null);

        // RN-05, RN-06, L-20
        Cotizacion cotizacion = cotizador.cotizar(domo, estancia, ocupantes, temporadas);

        PoliticaCancelacion politicaVigente = politicaRepository.obtenerVigenteEn(momentoActual.toLocalDate())
                .orElseThrow(() -> new PoliticaCancelacionNoEncontradaException("vigente en " + momentoActual.toLocalDate()));

        // RN-02, RN-04, 3.2 y RN-22 se verifican dentro de Reserva.crear(...)
        Reserva reserva = Reserva.crear(generadorCodigo.generar(), idDomo, domo.getCapacidad(), estancia, titular,
                ocupantes, canalOrigen, cotizacion, politicaVigente.getVersion(), umbral, momentoActual);
        if (horaEstimadaLlegada != null) {
            reserva.registrarHoraEstimadaLlegada(horaEstimadaLlegada);
        }

        reservaRepository.guardar(reserva);
        // F-08: el folio se abre al crear la reserva, con el cargo de alojamiento
        folioRepository.guardar(Folio.abrir(reserva.getCodigo(), reserva.getValorCongelado(), momentoActual.toLocalDate()));
        // I-05: la notificación nunca detiene la operación
        notificacionService.reservaCreada(reserva);
        return reserva;
    }
}
