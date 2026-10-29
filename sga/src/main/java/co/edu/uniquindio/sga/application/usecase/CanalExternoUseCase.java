package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.PoliticaCancelacionNoEncontradaException;
import co.edu.uniquindio.sga.application.exception.ReservaNoEncontradaException;
import co.edu.uniquindio.sga.domain.entity.Domo;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Novedad;
import co.edu.uniquindio.sga.domain.entity.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.port.NotificacionService;
import co.edu.uniquindio.sga.domain.repository.DomoRepository;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.GeneradorCodigoReserva;
import co.edu.uniquindio.sga.domain.repository.NovedadRepository;
import co.edu.uniquindio.sga.domain.repository.PoliticaCancelacionRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;
import co.edu.uniquindio.sga.domain.service.ConciliacionCanalExternoService;
import co.edu.uniquindio.sga.domain.service.ResultadoConciliacion;
import co.edu.uniquindio.sga.domain.valueobject.DatosReservaExterna;
import co.edu.uniquindio.sga.domain.valueobject.GravedadNovedad;
import co.edu.uniquindio.sga.domain.valueobject.IdNovedad;
import co.edu.uniquindio.sga.domain.valueobject.IdentificadorExterno;
import co.edu.uniquindio.sga.domain.valueobject.TipoNovedad;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 7.8 y 10.3.1, lado servidor: crear y cancelar reservas externas. (La consulta de
 * disponibilidad reutiliza BuscarDomosDisponiblesUseCase.) Escenarios exigidos: reserva
 * aceptada, reserva en conflicto rechazada y registrada (RN-18), y mensaje repetido que no
 * duplica la reserva (RN-19).
 */
@Service
@Transactional
public class CanalExternoUseCase {

    private final ConciliacionCanalExternoService conciliacionService;
    private final ReservaRepository reservaRepository;
    private final DomoRepository domoRepository;
    private final TemporadaRepository temporadaRepository;
    private final PoliticaCancelacionRepository politicaRepository;
    private final FolioRepository folioRepository;
    private final NovedadRepository novedadRepository;
    private final GeneradorCodigoReserva generadorCodigo;
    private final NotificacionService notificacionService;
    private final CancelarReservaUseCase cancelarReservaUseCase;

    public CanalExternoUseCase(ConciliacionCanalExternoService conciliacionService, ReservaRepository reservaRepository,
                               DomoRepository domoRepository, TemporadaRepository temporadaRepository,
                               PoliticaCancelacionRepository politicaRepository, FolioRepository folioRepository,
                               NovedadRepository novedadRepository, GeneradorCodigoReserva generadorCodigo,
                               NotificacionService notificacionService, CancelarReservaUseCase cancelarReservaUseCase) {
        this.conciliacionService = conciliacionService;
        this.reservaRepository = reservaRepository;
        this.domoRepository = domoRepository;
        this.temporadaRepository = temporadaRepository;
        this.politicaRepository = politicaRepository;
        this.folioRepository = folioRepository;
        this.novedadRepository = novedadRepository;
        this.generadorCodigo = generadorCodigo;
        this.notificacionService = notificacionService;
        this.cancelarReservaUseCase = cancelarReservaUseCase;
    }

    public ResultadoConciliacion recibirReserva(DatosReservaExterna datos, LocalDateTime momentoActual) {
        datos.estancia().verificarQueNoEmpiezaAntesDe(momentoActual.toLocalDate());   // RN-04
        Domo domo = Buscar.domo(domoRepository, datos.domo());
        PoliticaCancelacion politica = politicaRepository.obtenerVigenteEn(momentoActual.toLocalDate())
                .orElseThrow(() -> new PoliticaCancelacionNoEncontradaException("vigente en " + momentoActual.toLocalDate()));

        ResultadoConciliacion resultado = conciliacionService.conciliar(datos, domo, temporadaRepository.buscarTodas(),
                politica, generadorCodigo.generar(), momentoActual);

        switch (resultado.tipo()) {
            case CREADA -> {
                Reserva reserva = resultado.reserva();
                reservaRepository.guardar(reserva);
                folioRepository.guardar(Folio.abrir(reserva.getCodigo(), reserva.getValorCongelado(),
                        momentoActual.toLocalDate()));
                notificacionService.reservaCreada(reserva);
            }
            case CONFLICTO -> novedadRepository.guardar(new Novedad(      // RN-18: se registra para el administrador
                    new IdNovedad(UUID.randomUUID().toString()), datos.domo(), TipoNovedad.CONFLICTO_CANAL,
                    "Conflicto de canal: la reserva externa " + datos.identificador().valor() + " del canal "
                            + datos.identificador().canal() + " fue rechazada. " + resultado.motivo(),
                    GravedadNovedad.MODERADA, "canal:" + datos.identificador().canal(), momentoActual));
            case DUPLICADA -> {
                // RN-19: no se crea nada; se devuelve la reserva existente
            }
        }
        return resultado;
    }

    public Reserva cancelarReserva(IdentificadorExterno identificador, String motivo, LocalDateTime momentoActual) {
        Reserva reserva = reservaRepository.buscarPorIdentificadorExterno(identificador)
                .orElseThrow(() -> new ReservaNoEncontradaException(identificador));
        return cancelarReservaUseCase.ejecutar(reserva.getCodigo(), motivo, momentoActual);
    }
}
