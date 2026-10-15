package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.entity.Domo;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Pago;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Cotizacion;
import co.edu.uniquindio.sga.domain.valueobject.DatosReservaExterna;
import co.edu.uniquindio.sga.domain.valueobject.Deposito;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.HoraEstimadaLlegada;
import co.edu.uniquindio.sga.domain.valueobject.IdBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.IdPago;
import co.edu.uniquindio.sga.domain.valueobject.IdentificadorExterno;
import co.edu.uniquindio.sga.domain.valueobject.MedioPago;
import co.edu.uniquindio.sga.domain.valueobject.MotivoBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;
import co.edu.uniquindio.sga.domain.valueobject.Porcentaje;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.TiempoPreparacion;
import co.edu.uniquindio.sga.infraestructure.persistence.inmemory.BloqueoRepositoryInMemory;
import co.edu.uniquindio.sga.infraestructure.persistence.inmemory.ReservaRepositoryInMemory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static co.edu.uniquindio.sga.Fixtures.ANA;
import static co.edu.uniquindio.sga.Fixtures.DOM_01;
import static co.edu.uniquindio.sga.Fixtures.HORARIO;
import static co.edu.uniquindio.sga.Fixtures.HOY;
import static co.edu.uniquindio.sga.Fixtures.LUIS;
import static co.edu.uniquindio.sga.Fixtures.PEDRO;
import static co.edu.uniquindio.sga.Fixtures.RECARGO;
import static co.edu.uniquindio.sga.Fixtures.SOFIA_NINA;
import static co.edu.uniquindio.sga.Fixtures.TITULAR;
import static co.edu.uniquindio.sga.Fixtures.UMBRAL;
import static co.edu.uniquindio.sga.Fixtures.domoBasico;
import static co.edu.uniquindio.sga.Fixtures.domoFamiliar;
import static co.edu.uniquindio.sga.Fixtures.estancia;
import static co.edu.uniquindio.sga.Fixtures.pesos;
import static co.edu.uniquindio.sga.Fixtures.politicaFicha;
import static co.edu.uniquindio.sga.Fixtures.reservaBasica;
import static co.edu.uniquindio.sga.Fixtures.temporadas;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiciosDeDominioTest {

    private ReservaRepositoryInMemory reservas;
    private BloqueoRepositoryInMemory bloqueos;
    private DisponibilidadDomoService disponibilidad;
    private CotizadorEstanciaService cotizador;
    private Domo dom01;

    @BeforeEach
    void configurar() {
        reservas = new ReservaRepositoryInMemory();
        bloqueos = new BloqueoRepositoryInMemory();
        disponibilidad = new DisponibilidadDomoService(reservas, bloqueos, new TiempoPreparacion(4), HORARIO);
        cotizador = new CotizadorEstanciaService(UMBRAL, RECARGO);
        dom01 = domoBasico();
    }

    // ================================================================ DisponibilidadDomoService

    @Test
    void noPermitirSolapamientoReservaActiva() {                                           // RN-01
        reservas.guardar(reservaBasica("RES-1", estancia("2026-10-10", "2026-10-12")));
        assertFalse(disponibilidad.estaDisponible(dom01, estancia("2026-10-11", "2026-10-13"), 2));
        assertThrows(ReglaDominioException.class,
                () -> disponibilidad.verificarDisponibilidad(dom01, estancia("2026-10-09", "2026-10-11"), 2, null));
    }

    @Test
    void unaReservaQueDejoDeEstarActivaLiberaSusNoches() {                                 // RN-12
        Reserva reserva = reservaBasica("RES-1", estancia("2026-10-10", "2026-10-12"));
        reserva.cancelar("Cambio de planes");
        reservas.guardar(reserva);
        assertTrue(disponibilidad.estaDisponible(dom01, estancia("2026-10-10", "2026-10-12"), 2));
    }

    @Test
    void noAdmitirOcupantesCapacidad() {                                                   // RN-02
        assertFalse(disponibilidad.estaDisponible(dom01, estancia("2026-10-10", "2026-10-12"), 3));
    }

    @Test
    void unBloqueoVigenteImpideVender() {                                                  // RN-07
        Bloqueo bloqueo = new Bloqueo(new IdBloqueo("B1"), DOM_01,
                new Periodo(LocalDate.of(2026, 10, 11), LocalDate.of(2026, 10, 11)), MotivoBloqueo.MANTENIMIENTO, null);
        bloqueos.guardar(bloqueo);
        assertFalse(disponibilidad.estaDisponible(dom01, estancia("2026-10-10", "2026-10-12"), 2));
        assertTrue(disponibilidad.estaDisponible(dom01, estancia("2026-10-12", "2026-10-14"), 2));
        bloqueo.levantar();
        assertTrue(disponibilidad.estaDisponible(dom01, estancia("2026-10-10", "2026-10-12"), 2));
    }

    @Test
    void tiempoDePreparacionEntreEstancias() {                                             // RN-20
        reservas.guardar(reservaBasica("RES-1", estancia("2026-10-10", "2026-10-12")));
        // Con 4 horas y la ventana 11:00-15:00, se puede entrar el mismo día de la salida
        assertTrue(disponibilidad.estaDisponible(dom01, estancia("2026-10-12", "2026-10-14"), 2));
        // Con 6 horas ya no cabe: la entrada del mismo día queda fuera
        DisponibilidadDomoService seisHoras = new DisponibilidadDomoService(reservas, bloqueos, new TiempoPreparacion(6), HORARIO);
        assertFalse(seisHoras.estaDisponible(dom01, estancia("2026-10-12", "2026-10-14"), 2));
        assertTrue(seisHoras.estaDisponible(dom01, estancia("2026-10-13", "2026-10-15"), 2));
    }

    @Test
    void unaReservaNoCompiteConsigoMismaAlModificarse() {                                 // RN-14
        Reserva reserva = reservaBasica("RES-1", estancia("2026-10-10", "2026-10-12"));
        reservas.guardar(reserva);
        assertTrue(disponibilidad.evaluar(dom01, estancia("2026-10-10", "2026-10-13"), 2, reserva.getCodigo()).disponible());
    }

    // ================================================================ CotizadorEstanciaService

    @Test
    void calcularValorEstanciaNochePorNoche() {                                           // RN-05, RN-06
        // 14 y 15 de diciembre: una noche Base (180.000) y una Alta (280.000); la niña no paga
        Domo familiar = domoFamiliar();
        Cotizacion cotizacion = cotizador.cotizar(familiar, estancia("2026-12-14", "2026-12-16"),
                List.of(ANA, SOFIA_NINA), temporadas());
        assertEquals(2, cotizacion.noches().size());
        assertEquals("Base", cotizacion.noches().get(0).nombreTemporada());
        assertEquals("Alta", cotizacion.noches().get(1).nombreTemporada());
        assertEquals(1, cotizacion.noches().get(0).ocupantesFacturables());
        assertEquals(pesos(250_000 + 350_000), cotizacion.total());
    }

    @Test
    void recargoPorPersonaAdicionalL20() {                                                 // L-20
        // Familiar, base 250.000, 4 adultos: 4 × 250.000 + 2 × 25 % × 250.000 = 1.125.000 por noche
        Cotizacion cotizacion = cotizador.cotizar(domoFamiliar(), estancia("2026-10-10", "2026-10-11"),
                List.of(ANA, LUIS, PEDRO, SOFIA_NINA), temporadas());
        assertEquals(pesos(125_000), cotizacion.totalRecargos());
        assertEquals(pesos(3 * 250_000 + 125_000), cotizacion.total());   // la niña cuenta para el recargo, no para la tarifa
    }

    @Test
    void cotizarExigeUnFacturableYRespetaLaCapacidad() {
        assertThrows(ReglaDominioException.class, () -> cotizador.cotizar(dom01, estancia("2026-10-10", "2026-10-11"),
                List.of(SOFIA_NINA), temporadas()));
        assertThrows(ReglaDominioException.class, () -> cotizador.cotizar(dom01, estancia("2026-10-10", "2026-10-11"),
                List.of(ANA, LUIS, PEDRO), temporadas()));
    }

    // ================================================================ RetencionService (RN-13)

    @Test
    void retencionSegunLosTramosDeLaFicha() {
        RetencionService retencion = new RetencionService(HORARIO);
        Reserva reserva = reservaBasica("RES-1", estancia("2026-11-01", "2026-11-03"));   // valor 720.000
        // 30 días antes: 10 %
        assertEquals(pesos(72_000), retencion.porCancelacion(reserva, politicaFicha(), LocalDateTime.of(2026, 10, 2, 9, 0)));
        // 7 días antes: 50 %
        assertEquals(pesos(360_000), retencion.porCancelacion(reserva, politicaFicha(), LocalDateTime.of(2026, 10, 25, 9, 0)));
        // 2 días antes: 100 %
        assertEquals(pesos(720_000), retencion.porCancelacion(reserva, politicaFicha(), LocalDateTime.of(2026, 10, 30, 9, 0)));
        assertEquals(pesos(720_000), retencion.porNoShow(reserva, politicaFicha()));
    }

    @Test
    void laRetencionSeCalculaConLaPoliticaCongelada() {
        RetencionService retencion = new RetencionService(HORARIO);
        Reserva reserva = reservaBasica("RES-1", estancia("2026-11-01", "2026-11-03"));
        var otraVersion = new co.edu.uniquindio.sga.domain.entity.PoliticaCancelacion(
                new co.edu.uniquindio.sga.domain.valueobject.VersionPolitica(2, LocalDate.of(2026, 9, 1)),
                politicaFicha().getTramos(), Porcentaje.de(100));
        assertThrows(ReglaDominioException.class, () -> retencion.porNoShow(reserva, otraVersion));
    }

    // ================================================================ Confirmación (anticipo y L-21)

    @Test
    void confirmarExigeAnticipoYAlistamientoDelJacuzzi() {
        ConfirmacionReservaService confirmacion = new ConfirmacionReservaService(Porcentaje.de(50), HORARIO);
        Reserva reserva = reservaBasica("RES-1", estancia("2026-10-10", "2026-10-12"));    // 720.000
        Folio folio = Folio.abrir(reserva.getCodigo(), reserva.getValorCongelado(), HOY);

        reserva.registrarHoraEstimadaLlegada(new HoraEstimadaLlegada(LocalTime.of(16, 0)));
        folio.registrarPago(new Pago(new IdPago("P1"), pesos(360_000), MedioPago.TRANSFERENCIA, HOY));
        assertThrows(ReglaDominioException.class, () -> confirmacion.confirmar(reserva, dom01, folio));   // L-21

        reserva.registrarHoraEstimadaLlegada(new HoraEstimadaLlegada(LocalTime.of(18, 30)));
        Reserva sinAnticipo = reservaBasica("RES-2", estancia("2026-10-20", "2026-10-22"));
        sinAnticipo.registrarHoraEstimadaLlegada(new HoraEstimadaLlegada(LocalTime.of(18, 30)));
        Folio folioSinPagos = Folio.abrir(sinAnticipo.getCodigo(), sinAnticipo.getValorCongelado(), HOY);
        assertThrows(ReglaDominioException.class, () -> confirmacion.confirmar(sinAnticipo, dom01, folioSinPagos)); // anticipo

        confirmacion.confirmar(reserva, dom01, folio);
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
    }

    // ================================================================ Entrega (RN-10, RN-11, L-22)

    @Test
    void entregaExigeDomoPreparadoYDepositoEnElFolio() {
        EntregaDomoService entrega = new EntregaDomoService();
        Reserva reserva = reservaBasica("RES-1", estancia("2026-10-10", "2026-10-12"));
        reserva.registrarHoraEstimadaLlegada(new HoraEstimadaLlegada(LocalTime.of(18, 30)));
        reserva.confirmar();
        Folio folio = Folio.abrir(reserva.getCodigo(), reserva.getValorCongelado(), HOY);
        LocalDate entrada = reserva.getEstancia().fechaEntrada();

        assertThrows(ReglaDominioException.class, () -> entrega.registrarLlegada(reserva, dom01, folio, entrada)); // L-22
        folio.registrarDeposito(new Deposito(pesos(100_000)), HOY);

        dom01.declararFueraDeServicio();
        assertThrows(ReglaDominioException.class, () -> entrega.registrarLlegada(reserva, dom01, folio, entrada)); // RN-11
        dom01.marcarPreparado();

        assertThrows(ReglaDominioException.class,
                () -> entrega.registrarLlegada(reserva, dom01, folio, entrada.minusDays(1)));                     // RN-10
        entrega.registrarLlegada(reserva, dom01, folio, entrada);
        assertEquals(EstadoReserva.EN_CURSO, reserva.getEstado());
    }

    // ================================================================ Conciliación (RN-18, RN-19)

    @Test
    void rechazarYRegistrarConflictoReservaExterna() {
        ConciliacionCanalExternoService conciliacion = new ConciliacionCanalExternoService(reservas, disponibilidad,
                cotizador, UMBRAL);
        Estancia estancia = estancia("2026-10-10", "2026-10-12");
        DatosReservaExterna datos = new DatosReservaExterna(new IdentificadorExterno("BOOKING_COM", "EXT-1"), DOM_01,
                estancia, TITULAR, List.of(ANA, LUIS), null);

        ResultadoConciliacion primera = conciliacion.conciliar(datos, dom01, temporadas(), politicaFicha(),
                new CodigoReserva("RES-1"), HOY.atTime(9, 0));
        assertEquals(ResultadoConciliacion.Tipo.CREADA, primera.tipo());
        reservas.guardar(primera.reserva());

        // RN-19: el mismo mensaje no crea otra reserva
        ResultadoConciliacion repetida = conciliacion.conciliar(datos, dom01, temporadas(), politicaFicha(),
                new CodigoReserva("RES-2"), HOY.atTime(9, 5));
        assertEquals(ResultadoConciliacion.Tipo.DUPLICADA, repetida.tipo());
        assertEquals(primera.reserva().getCodigo(), repetida.reserva().getCodigo());

        // RN-18: otro identificador sobre las mismas noches se rechaza sin tocar la vigente
        DatosReservaExterna enConflicto = new DatosReservaExterna(new IdentificadorExterno("BOOKING_COM", "EXT-2"),
                DOM_01, estancia, TITULAR, List.of(ANA), null);
        ResultadoConciliacion conflicto = conciliacion.conciliar(enConflicto, dom01, temporadas(), politicaFicha(),
                new CodigoReserva("RES-3"), HOY.atTime(9, 10));
        assertEquals(ResultadoConciliacion.Tipo.CONFLICTO, conflicto.tipo());
        assertEquals(EstadoReserva.PENDIENTE, primera.reserva().getEstado());
    }
}
