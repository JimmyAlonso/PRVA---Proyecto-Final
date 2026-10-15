package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.HoraEstimadaLlegada;
import co.edu.uniquindio.sga.domain.valueobject.HoraLimiteNoShow;
import co.edu.uniquindio.sga.domain.valueobject.IdentificadorExterno;
import co.edu.uniquindio.sga.domain.valueobject.PlazoConfirmacion;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static co.edu.uniquindio.sga.Fixtures.ANA;
import static co.edu.uniquindio.sga.Fixtures.DOM_01;
import static co.edu.uniquindio.sga.Fixtures.DOM_03;
import static co.edu.uniquindio.sga.Fixtures.HOY;
import static co.edu.uniquindio.sga.Fixtures.LUIS;
import static co.edu.uniquindio.sga.Fixtures.PEDRO;
import static co.edu.uniquindio.sga.Fixtures.POLITICA_V1;
import static co.edu.uniquindio.sga.Fixtures.SOFIA_NINA;
import static co.edu.uniquindio.sga.Fixtures.TITULAR;
import static co.edu.uniquindio.sga.Fixtures.UMBRAL;
import static co.edu.uniquindio.sga.Fixtures.cotizacionPlana;
import static co.edu.uniquindio.sga.Fixtures.estancia;
import static co.edu.uniquindio.sga.Fixtures.pesos;
import static co.edu.uniquindio.sga.Fixtures.reserva;
import static co.edu.uniquindio.sga.Fixtures.reservaBasica;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReservaTest {

    private static final Estancia ESTANCIA = estancia("2026-10-10", "2026-10-12");
    private static final HoraEstimadaLlegada LLEGADA = new HoraEstimadaLlegada(LocalTime.of(18, 30));

    @Test
    void nacePendienteConValorYPoliticaCongelados() {                                    // RN-22
        Reserva reserva = reservaBasica("RES-1", ESTANCIA);
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
        assertEquals(pesos(720_000), reserva.getValorCongelado());
        assertEquals(2, reserva.getDesgloseCongelado().size());
        assertEquals(POLITICA_V1, reserva.getPoliticaCongelada());
    }

    @Test
    void noCrearReservaPasado() {                                                         // RN-04
        assertThrows(ReglaDominioException.class, () -> reservaBasica("RES-1", estancia("2026-09-30", "2026-10-02")));
    }

    @Test
    void noAdmitirMasOcupantesQueLaCapacidad() {                                          // RN-02
        assertThrows(ReglaDominioException.class,
                () -> reserva("RES-1", DOM_01, 2, ESTANCIA, List.of(ANA, LUIS, SOFIA_NINA)));
    }

    @Test
    void losNoFacturablesTambienCuentanParaLaCapacidad() {                               // RN-06
        Reserva reserva = reserva("RES-1", DOM_03, 3, ESTANCIA, List.of(ANA, LUIS, SOFIA_NINA));
        assertEquals(3, reserva.totalOcupantes());
        assertEquals(2, reserva.ocupantesFacturables(UMBRAL));
    }

    @Test
    void elTitularDebeSerOcupanteYFacturable() {                                          // 3.2
        assertThrows(ReglaDominioException.class, () -> reserva("RES-1", DOM_01, 2, ESTANCIA, List.of(LUIS, PEDRO)));

        TitularDeLaReserva nino = new TitularDeLaReserva(SOFIA_NINA.getDocumento(), "Sofía", "s@correo.com", "300");
        assertThrows(ReglaDominioException.class, () -> Reserva.crear(new CodigoReserva("RES-2"), DOM_01, 2, ESTANCIA,
                nino, List.of(SOFIA_NINA, ANA), CanalOrigen.PORTAL, cotizacionPlana(ESTANCIA, 1), POLITICA_V1, UMBRAL,
                HOY.atStartOfDay()));
    }

    @Test
    void noAdmitirOcupantesRepetidos() {
        assertThrows(ReglaDominioException.class, () -> reserva("RES-1", DOM_03, 4, ESTANCIA, List.of(ANA, ANA)));
    }

    @Test
    void exigirHoraEstimadaDeLlegadaParaConfirmar() {                                    // RN-09
        Reserva reserva = reservaBasica("RES-1", ESTANCIA);
        assertThrows(ReglaDominioException.class, reserva::confirmar);
        reserva.registrarHoraEstimadaLlegada(LLEGADA);
        reserva.confirmar();
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
    }

    @Test
    void rechazarTransicionEstadoInvalida() {                                             // RN-08
        Reserva reserva = reservaBasica("RES-1", ESTANCIA);
        // PENDIENTE no puede registrar llegada, salida ni no-show
        assertThrows(ReglaDominioException.class, () -> reserva.registrarLlegada(ESTANCIA.fechaEntrada()));
        assertThrows(ReglaDominioException.class, reserva::registrarSalida);
        assertThrows(ReglaDominioException.class,
                () -> reserva.declararNoShow(ESTANCIA.fechaEntrada().atTime(23, 0), new HoraLimiteNoShow(LocalTime.of(20, 0))));

        reserva.cancelar("Cambio de planes");
        // CANCELADA es terminal
        assertThrows(ReglaDominioException.class, reserva::confirmar);
        assertThrows(ReglaDominioException.class, () -> reserva.cancelar("otra vez"));
    }

    @Test
    void noRegistrarLlegadaAntesDeLaFechaDeEntrada() {                                    // RN-10
        Reserva reserva = reservaBasica("RES-1", ESTANCIA);
        reserva.registrarHoraEstimadaLlegada(LLEGADA);
        reserva.confirmar();
        assertThrows(ReglaDominioException.class, () -> reserva.registrarLlegada(LocalDate.of(2026, 10, 9)));
        reserva.registrarLlegada(ESTANCIA.fechaEntrada());
        assertEquals(EstadoReserva.EN_CURSO, reserva.getEstado());
        // EN_CURSO ya no se puede cancelar: la salida anticipada es una salida
        assertThrows(ReglaDominioException.class, () -> reserva.cancelar("Se van antes"));
        reserva.registrarSalida();
        assertEquals(EstadoReserva.FINALIZADA, reserva.getEstado());
    }

    @Test
    void noShowSoloDesdeLaHoraLimite() {                                                   // L-15
        Reserva reserva = reservaBasica("RES-1", ESTANCIA);
        reserva.registrarHoraEstimadaLlegada(LLEGADA);
        reserva.confirmar();
        HoraLimiteNoShow limite = new HoraLimiteNoShow(LocalTime.of(20, 0));
        assertThrows(ReglaDominioException.class, () -> reserva.declararNoShow(ESTANCIA.fechaEntrada().atTime(19, 59), limite));
        reserva.declararNoShow(ESTANCIA.fechaEntrada().atTime(20, 0), limite);
        assertEquals(EstadoReserva.NO_SHOW, reserva.getEstado());
        assertFalse(reserva.estaActiva());                                                 // RN-12
    }

    @Test
    void pendienteSeVenceAlSuperarElPlazoDeConfirmacion() {                               // RN-21
        Reserva reserva = reservaBasica("RES-1", ESTANCIA);        // creada HOY a las 09:00
        PlazoConfirmacion cuatroHoras = new PlazoConfirmacion(4);
        assertFalse(reserva.venceElPlazo(cuatroHoras, HOY.atTime(12, 59)));
        assertThrows(ReglaDominioException.class, () -> reserva.vencerPorPlazo(cuatroHoras, HOY.atTime(12, 0)));
        assertTrue(reserva.venceElPlazo(cuatroHoras, HOY.atTime(13, 0)));
        reserva.vencerPorPlazo(cuatroHoras, HOY.atTime(13, 0));
        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
    }

    @Test
    void modificarRecalculaYDevuelveElValorAnterior() {                                   // RN-14, RN-22
        Reserva reserva = reservaBasica("RES-1", ESTANCIA);
        Estancia tresNoches = estancia("2026-10-10", "2026-10-13");
        Dinero anterior = reserva.modificar(DOM_01, 2, tresNoches, List.of(ANA, LUIS), cotizacionPlana(tresNoches, 360_000),
                UMBRAL, HOY);
        assertEquals(pesos(720_000), anterior);
        assertEquals(pesos(1_080_000), reserva.getValorCongelado());
        assertEquals(POLITICA_V1, reserva.getPoliticaCongelada());
        // La modificación repite las validaciones: capacidad
        assertThrows(ReglaDominioException.class, () -> reserva.modificar(DOM_01, 2, tresNoches,
                List.of(ANA, LUIS, PEDRO), cotizacionPlana(tresNoches, 1), UMBRAL, HOY));
    }

    @Test
    void soloLasReservasExternasLlevanIdentificadorExterno() {
        Reserva portal = reservaBasica("RES-1", ESTANCIA);
        assertThrows(ReglaDominioException.class,
                () -> portal.marcarComoExterna(new IdentificadorExterno("BOOKING_COM", "EXT-1")));
        assertEquals(TITULAR, portal.getTitular());
    }
}
