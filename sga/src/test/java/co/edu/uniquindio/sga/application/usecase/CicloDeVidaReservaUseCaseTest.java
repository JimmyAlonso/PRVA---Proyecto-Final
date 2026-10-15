package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.ReservaNoEncontradaException;
import co.edu.uniquindio.sga.domain.entity.Domo;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.CriteriosBusquedaReserva;
import co.edu.uniquindio.sga.domain.service.ConciliacionCanalExternoService;
import co.edu.uniquindio.sga.domain.service.ConfirmacionReservaService;
import co.edu.uniquindio.sga.domain.service.CotizadorEstanciaService;
import co.edu.uniquindio.sga.domain.service.DisponibilidadDomoService;
import co.edu.uniquindio.sga.domain.service.EntregaDomoService;
import co.edu.uniquindio.sga.domain.service.LiquidacionSalidaService;
import co.edu.uniquindio.sga.domain.service.ResultadoConciliacion;
import co.edu.uniquindio.sga.domain.service.RetencionService;
import co.edu.uniquindio.sga.domain.valueobject.AutorizacionCierre;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.DatosReservaExterna;
import co.edu.uniquindio.sga.domain.valueobject.Deposito;
import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.HoraEstimadaLlegada;
import co.edu.uniquindio.sga.domain.valueobject.HoraLimiteNoShow;
import co.edu.uniquindio.sga.domain.valueobject.IdentificadorExterno;
import co.edu.uniquindio.sga.domain.valueobject.MedioPago;
import co.edu.uniquindio.sga.domain.valueobject.ParametrosOperacion;
import co.edu.uniquindio.sga.domain.valueobject.PlazoConfirmacion;
import co.edu.uniquindio.sga.domain.valueobject.Porcentaje;
import co.edu.uniquindio.sga.domain.valueobject.TiempoPreparacion;
import co.edu.uniquindio.sga.domain.valueobject.TipoNovedad;
import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.infraestructure.notificacion.NotificacionLogAdapter;
import co.edu.uniquindio.sga.infraestructure.persistence.inmemory.BloqueoRepositoryInMemory;
import co.edu.uniquindio.sga.infraestructure.persistence.inmemory.DomoRepositoryInMemory;
import co.edu.uniquindio.sga.infraestructure.persistence.inmemory.FolioRepositoryInMemory;
import co.edu.uniquindio.sga.infraestructure.persistence.inmemory.GeneradorCodigoReservaInMemory;
import co.edu.uniquindio.sga.infraestructure.persistence.inmemory.NovedadRepositoryInMemory;
import co.edu.uniquindio.sga.infraestructure.persistence.inmemory.PoliticaCancelacionRepositoryInMemory;
import co.edu.uniquindio.sga.infraestructure.persistence.inmemory.ReservaRepositoryInMemory;
import co.edu.uniquindio.sga.infraestructure.persistence.inmemory.TemporadaRepositoryInMemory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static co.edu.uniquindio.sga.Fixtures.ANA;
import static co.edu.uniquindio.sga.Fixtures.DOM_01;
import static co.edu.uniquindio.sga.Fixtures.DOM_03;
import static co.edu.uniquindio.sga.Fixtures.HORARIO;
import static co.edu.uniquindio.sga.Fixtures.HOY;
import static co.edu.uniquindio.sga.Fixtures.LUIS;
import static co.edu.uniquindio.sga.Fixtures.RECARGO;
import static co.edu.uniquindio.sga.Fixtures.SOFIA_NINA;
import static co.edu.uniquindio.sga.Fixtures.TITULAR;
import static co.edu.uniquindio.sga.Fixtures.UMBRAL;
import static co.edu.uniquindio.sga.Fixtures.domoBasico;
import static co.edu.uniquindio.sga.Fixtures.domoFamiliar;
import static co.edu.uniquindio.sga.Fixtures.estancia;
import static co.edu.uniquindio.sga.Fixtures.pesos;
import static co.edu.uniquindio.sga.Fixtures.politicaFicha;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Ciclo de vida completo por los casos de uso, con los adaptadores en memoria (I-02, I-08):
 * crear → confirmar → check-in → check-out, cancelación, no-show, vencimiento, modificación y
 * canal externo. Basado en la prueba del repositorio guía, adaptado a los domos de JiyJu.
 */
class CicloDeVidaReservaUseCaseTest {

    private static final LocalDateTime AHORA = HOY.atTime(9, 0);
    // Familiar DOM-03 en temporada base: 2 adultos facturables + 1 niña → 2 × 250.000 + recargo 25 % por 1 adicional
    private static final Estancia ESTANCIA = estancia("2026-10-10", "2026-10-12");
    private static final long VALOR_ESPERADO = 2 * (2 * 250_000 + 62_500);

    private ReservaRepositoryInMemory reservaRepository;
    private DomoRepositoryInMemory domoRepository;
    private FolioRepositoryInMemory folioRepository;
    private NovedadRepositoryInMemory novedadRepository;
    private NotificacionLogAdapter notificaciones;
    private Domo familiar;

    private CrearReservaUseCase crear;
    private ConsultarReservaUseCase consultar;
    private ConsultarReservasUseCase listar;
    private ConfirmarReservaUseCase confirmar;
    private ModificarReservaUseCase modificar;
    private CancelarReservaUseCase cancelar;
    private DeclararNoShowUseCase noShow;
    private VencerReservasPendientesUseCase vencer;
    private RegistrarLlegadaUseCase llegada;
    private RegistrarSalidaUseCase salida;
    private GestionarFolioUseCase folio;
    private CanalExternoUseCase canalExterno;

    @BeforeEach
    void configurar() {
        reservaRepository = new ReservaRepositoryInMemory();
        domoRepository = new DomoRepositoryInMemory();
        folioRepository = new FolioRepositoryInMemory();
        novedadRepository = new NovedadRepositoryInMemory();
        notificaciones = new NotificacionLogAdapter();
        var temporadaRepository = new TemporadaRepositoryInMemory();
        var politicaRepository = new PoliticaCancelacionRepositoryInMemory();
        var generador = new GeneradorCodigoReservaInMemory();

        for (Temporada temporada : co.edu.uniquindio.sga.Fixtures.temporadas()) {
            temporadaRepository.guardar(temporada);
        }
        familiar = domoFamiliar();
        domoRepository.guardar(familiar);
        domoRepository.guardar(domoBasico());
        politicaRepository.guardar(politicaFicha());

        ParametrosOperacion parametros = new ParametrosOperacion(UMBRAL, new TiempoPreparacion(4),
                new PlazoConfirmacion(4), new HoraLimiteNoShow(LocalTime.of(20, 0)), Porcentaje.de(50),
                new Deposito(pesos(100_000)), RECARGO);
        Alojamiento alojamiento = new Alojamiento("JiyJu Glamping", null, "Salento", null, new BigDecimal("4.63"),
                new BigDecimal("-75.57"), HORARIO, null, parametros, false);

        var disponibilidad = new DisponibilidadDomoService(reservaRepository, new BloqueoRepositoryInMemory(),
                parametros.tiempoPreparacion(), HORARIO);
        var cotizador = new CotizadorEstanciaService(UMBRAL, RECARGO);
        var retencion = new RetencionService(HORARIO);

        crear = new CrearReservaUseCase(reservaRepository, domoRepository, folioRepository, temporadaRepository,
                politicaRepository, generador, disponibilidad, cotizador, notificaciones, UMBRAL);
        consultar = new ConsultarReservaUseCase(reservaRepository);
        listar = new ConsultarReservasUseCase(reservaRepository);
        confirmar = new ConfirmarReservaUseCase(reservaRepository, domoRepository, folioRepository,
                new ConfirmacionReservaService(parametros.anticipo(), HORARIO), notificaciones);
        modificar = new ModificarReservaUseCase(reservaRepository, domoRepository, folioRepository, temporadaRepository,
                disponibilidad, cotizador, UMBRAL);
        cancelar = new CancelarReservaUseCase(reservaRepository, folioRepository, politicaRepository, retencion, notificaciones);
        noShow = new DeclararNoShowUseCase(reservaRepository, folioRepository, politicaRepository, retencion,
                parametros.horaLimiteNoShow());
        vencer = new VencerReservasPendientesUseCase(reservaRepository, folioRepository, notificaciones,
                parametros.plazoConfirmacion());
        llegada = new RegistrarLlegadaUseCase(reservaRepository, domoRepository, folioRepository, new EntregaDomoService());
        salida = new RegistrarSalidaUseCase(reservaRepository, domoRepository, folioRepository, new LiquidacionSalidaService());
        folio = new GestionarFolioUseCase(folioRepository, alojamiento);
        canalExterno = new CanalExternoUseCase(new ConciliacionCanalExternoService(reservaRepository, disponibilidad,
                cotizador, UMBRAL), reservaRepository, domoRepository, temporadaRepository, politicaRepository,
                folioRepository, novedadRepository, generador, notificaciones, cancelar);
    }

    private Reserva crearReserva() {
        return crear.ejecutar(DOM_03, ESTANCIA, new HoraEstimadaLlegada(LocalTime.of(18, 30)), TITULAR,
                List.of(ANA, LUIS, SOFIA_NINA), CanalOrigen.PORTAL, AHORA);
    }

    private Folio folioDe(Reserva reserva) {
        return folioRepository.obtenerPorReserva(reserva.getCodigo()).orElseThrow();
    }

    private void pagarAnticipoYConfirmar(Reserva reserva) {
        folio.registrarPago(reserva.getCodigo(), pesos(VALOR_ESPERADO / 2), MedioPago.TRANSFERENCIA, HOY);
        confirmar.ejecutar(reserva.getCodigo(), null);
    }

    @Test
    void crearReservaCongelaElValorAbreElFolioYNotifica() {
        Reserva reserva = crearReserva();
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
        assertEquals(pesos(VALOR_ESPERADO), reserva.getValorCongelado());
        assertEquals(VALOR_ESPERADO, folioDe(reserva).saldo().enPesos());
        assertEquals(reserva, consultar.ejecutar(reserva.getCodigo()));
        assertEquals(1, notificaciones.getEnviados().size());
    }

    @Test
    void crearReservaSolapadaEsRechazada() {                                               // RN-01
        crearReserva();
        assertThrows(ReglaDominioException.class, this::crearReserva);
    }

    @Test
    void elCanalExternoNoEntraPorLaCreacionNormal() {
        assertThrows(ReglaDominioException.class, () -> crear.ejecutar(DOM_03, ESTANCIA, null, TITULAR, List.of(ANA),
                CanalOrigen.EXTERNO, AHORA));
    }

    @Test
    void consultarReservaInexistenteLanzaNoEncontrada() {
        assertThrows(ReservaNoEncontradaException.class, () -> consultar.ejecutar(new CodigoReserva("RES-2026-99999")));
    }

    @Test
    void confirmarSinAnticipoEsRechazado() {                                               // L-11
        Reserva reserva = crearReserva();
        assertThrows(ReglaDominioException.class, () -> confirmar.ejecutar(reserva.getCodigo(), null));
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
    }

    @Test
    void cicloCompletoHastaLaSalida() {
        Reserva reserva = crearReserva();
        CodigoReserva codigo = reserva.getCodigo();
        pagarAnticipoYConfirmar(reserva);
        assertEquals(List.of(reserva), listar.ejecutar(new CriteriosBusquedaReserva(EstadoReserva.CONFIRMADA, null,
                null, null, null, null), 0).contenido());

        // L-22: sin depósito no hay check-in
        assertThrows(ReglaDominioException.class, () -> llegada.ejecutar(codigo, ESTANCIA.fechaEntrada()));
        folio.registrarDeposito(codigo, ESTANCIA.fechaEntrada());
        llegada.ejecutar(codigo, ESTANCIA.fechaEntrada());
        assertEquals(EstadoReserva.EN_CURSO, reserva.getEstado());
        assertEquals(EstadoOperativo.OCUPADO, familiar.getEstadoOperativo());

        // RN-17: con saldo pendiente y sin autorización no cambia nada
        assertThrows(ReglaDominioException.class, () -> salida.ejecutar(codigo, null));
        assertEquals(EstadoReserva.EN_CURSO, reserva.getEstado());

        // Se devuelve el depósito y se paga el resto: saldo cero, sale sin autorización
        folio.devolverDeposito(codigo, ESTANCIA.fechaSalida());
        folio.registrarPago(codigo, pesos(VALOR_ESPERADO / 2), MedioPago.EFECTIVO, ESTANCIA.fechaSalida());
        assertTrue(folioDe(reserva).saldo().esCero());
        salida.ejecutar(codigo, null);
        assertEquals(EstadoReserva.FINALIZADA, reserva.getEstado());
        assertEquals(EstadoOperativo.PENDIENTE_PREPARACION, familiar.getEstadoOperativo());
        assertTrue(folioDe(reserva).isCerrado());
    }

    @Test
    void salidaConSaldoPendienteRequiereAutorizacionRegistrada() {                         // RN-17
        Reserva reserva = crearReserva();
        pagarAnticipoYConfirmar(reserva);
        folio.registrarDeposito(reserva.getCodigo(), HOY);
        llegada.ejecutar(reserva.getCodigo(), ESTANCIA.fechaEntrada());
        salida.ejecutar(reserva.getCodigo(), new AutorizacionCierre("admin.jiyju", "Saldo se paga por transferencia"));
        assertEquals(EstadoReserva.FINALIZADA, reserva.getEstado());
        assertEquals("admin.jiyju", folioDe(reserva).getAutorizacionCierre().autor());
    }

    @Test
    void cancelarRetieneSegunLaAntelacionDeLaPoliticaCongelada() {                                   // RN-13
        Reserva reserva = crearReserva();
        folio.registrarPago(reserva.getCodigo(), pesos(VALOR_ESPERADO / 2), MedioPago.TRANSFERENCIA, HOY);
        // Se cancela el 1 de octubre para una estancia del 10: 9 días → tramo del 50 %
        cancelar.ejecutar(reserva.getCodigo(), "El huésped cambió de planes", AHORA);
        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
        // Retención 50 % = pago del anticipo 50 %: saldo cero
        assertTrue(folioDe(reserva).saldo().esCero());

        Reserva lejana = crear.ejecutar(DOM_01, estancia("2026-11-20", "2026-11-22"),
                new HoraEstimadaLlegada(LocalTime.of(18, 30)), TITULAR, List.of(ANA, LUIS), CanalOrigen.DIRECTO, AHORA);
        folio.registrarPago(lejana.getCodigo(), pesos(180_000), MedioPago.TRANSFERENCIA, HOY);
        cancelar.ejecutar(lejana.getCodigo(), "Viaje aplazado", AHORA);
        // 720.000 × 10 % = 72.000 de retención; pagó 180.000 → saldo a favor de 108.000
        assertEquals(-108_000, folioDe(lejana).saldo().enPesos());
    }

    @Test
    void laCancelacionLiberaLaDisponibilidadDeInmediato() {                               // RN-12
        Reserva reserva = crearReserva();
        cancelar.ejecutar(reserva.getCodigo(), "El huésped cambió de planes", AHORA);
        Reserva otra = crearReserva();
        assertEquals(EstadoReserva.PENDIENTE, otra.getEstado());
    }

    @Test
    void noShowSoloDespuesDeLaHoraLimiteYRetieneSegunLaPolitica() {
        Reserva reserva = crearReserva();
        pagarAnticipoYConfirmar(reserva);
        assertThrows(ReglaDominioException.class,
                () -> noShow.ejecutar(reserva.getCodigo(), ESTANCIA.fechaEntrada().atTime(18, 0)));
        noShow.ejecutar(reserva.getCodigo(), ESTANCIA.fechaEntrada().atTime(20, 30));
        assertEquals(EstadoReserva.NO_SHOW, reserva.getEstado());
        // 100 % retenido, la mitad pagada: el huésped debe la otra mitad
        assertEquals(VALOR_ESPERADO / 2, folioDe(reserva).saldo().enPesos());
    }

    @Test
    void lasPendientesSeVencenSinIntervencionManual() {                                   // RN-21
        Reserva reserva = crearReserva();
        assertEquals(0, vencer.ejecutar(AHORA.plusHours(3)).size());
        assertEquals(1, vencer.ejecutar(AHORA.plusHours(4)).size());
        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
        assertTrue(folioDe(reserva).saldo().esCero());
    }

    @Test
    void modificarRecalculaYRegistraElAjusteEnElFolio() {                                 // RN-14
        Reserva reserva = crearReserva();
        Estancia tresNoches = estancia("2026-10-10", "2026-10-13");
        modificar.ejecutar(reserva.getCodigo(), null, tresNoches, null, AHORA);
        long valorTresNoches = 3 * (2 * 250_000 + 62_500);
        assertEquals(pesos(valorTresNoches), reserva.getValorCongelado());
        assertEquals(valorTresNoches, folioDe(reserva).saldo().enPesos());

        // Pasar a DOM-01 (capacidad 2) con 3 ocupantes viola RN-02
        assertThrows(ReglaDominioException.class, () -> modificar.ejecutar(reserva.getCodigo(), DOM_01, null, null, AHORA));
        // Pasar a DOM-01 solo con los dos adultos sí se puede, y baja el valor
        modificar.ejecutar(reserva.getCodigo(), DOM_01, null, List.of(ANA, LUIS), AHORA);
        assertEquals(pesos(3 * 2 * 180_000), reserva.getValorCongelado());
        assertEquals(3 * 2 * 180_000, folioDe(reserva).saldo().enPesos());
    }

    @Test
    void canalExternoAceptadaRepetidaYEnConflicto() {                                      // RN-18, RN-19
        DatosReservaExterna datos = new DatosReservaExterna(new IdentificadorExterno("BOOKING_COM", "EXT-1001"),
                DOM_03, ESTANCIA, TITULAR, List.of(ANA, LUIS), new HoraEstimadaLlegada(LocalTime.of(19, 0)));

        ResultadoConciliacion aceptada = canalExterno.recibirReserva(datos, AHORA);
        assertEquals(ResultadoConciliacion.Tipo.CREADA, aceptada.tipo());
        assertEquals(CanalOrigen.EXTERNO, aceptada.reserva().getCanalOrigen());
        assertTrue(folioRepository.obtenerPorReserva(aceptada.reserva().getCodigo()).isPresent());

        assertEquals(ResultadoConciliacion.Tipo.DUPLICADA, canalExterno.recibirReserva(datos, AHORA).tipo());
        assertEquals(1, listar.ejecutar(null, 0).totalElementos());

        DatosReservaExterna otra = new DatosReservaExterna(new IdentificadorExterno("BOOKING_COM", "EXT-1002"),
                DOM_03, estancia("2026-10-11", "2026-10-13"), TITULAR, List.of(ANA), null);
        assertEquals(ResultadoConciliacion.Tipo.CONFLICTO, canalExterno.recibirReserva(otra, AHORA).tipo());
        assertEquals(1, novedadRepository.listarPorTipo(TipoNovedad.CONFLICTO_CANAL).size());

        Reserva cancelada = canalExterno.cancelarReserva(new IdentificadorExterno("BOOKING_COM", "EXT-1001"),
                "Cancelada desde la plataforma", AHORA);
        assertEquals(EstadoReserva.CANCELADA, cancelada.getEstado());
    }

    @Test
    void listadoPaginadoDeDiezMasRecientePrimero() {                                       // 7.5, 12.4
        for (int i = 0; i < 12; i++) {
            Estancia estancia = estancia("2026-11-01", "2026-11-02");
            Estancia propia = new Estancia(estancia.fechaEntrada().plusDays(i * 2L), estancia.fechaSalida().plusDays(i * 2L));
            crear.ejecutar(DOM_01, propia, null, TITULAR, List.of(ANA), CanalOrigen.PORTAL, AHORA.plusMinutes(i));
        }
        var primera = listar.ejecutar(null, 0);
        assertEquals(10, primera.contenido().size());
        assertEquals(12, primera.totalElementos());
        assertEquals(2, primera.totalPaginas());
        assertEquals(2, listar.ejecutar(null, 1).contenido().size());
        assertTrue(primera.contenido().get(0).getMomentoCreacion().isAfter(primera.contenido().get(9).getMomentoCreacion()));
    }
}
