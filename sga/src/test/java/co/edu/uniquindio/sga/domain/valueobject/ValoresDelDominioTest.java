package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static co.edu.uniquindio.sga.Fixtures.HORARIO;
import static co.edu.uniquindio.sga.Fixtures.estancia;
import static co.edu.uniquindio.sga.Fixtures.pesos;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Objetos de valor: Estancia (RN-03, 3.1), Saldo (RN-15), TiempoPreparacion (RN-20), Dinero (3.4), L-20, L-21. */
class ValoresDelDominioTest {

    // ---------------------------------------------------------------- RN-03 y 3.1

    @Test
    void verificarFechaEstancia() {
        assertThrows(ReglaDominioException.class, () -> estancia("2026-10-12", "2026-10-12"));
        assertThrows(ReglaDominioException.class, () -> estancia("2026-10-12", "2026-10-10"));
    }

    @Test
    void del10Al12SonDosNochesYLaDeSalidaNoSeOcupa() {
        Estancia estancia = estancia("2026-10-10", "2026-10-12");
        assertEquals(2, estancia.noches());
        assertTrue(estancia.incluye(LocalDate.of(2026, 10, 11)));
        assertFalse(estancia.incluye(LocalDate.of(2026, 10, 12)));
    }

    @Test
    void solapanSoloSiCompartenUnaNoche() {
        Estancia a = estancia("2026-10-10", "2026-10-12");
        assertTrue(a.seSolapaCon(estancia("2026-10-11", "2026-10-13")));
        assertFalse(a.seSolapaCon(estancia("2026-10-12", "2026-10-14")));   // salida = entrada: no solapan
    }

    @Test
    void rechazarEntradaAnteriorAHoy() {                                        // RN-04
        assertThrows(ReglaDominioException.class,
                () -> estancia("2026-09-30", "2026-10-02").verificarQueNoEmpiezaAntesDe(LocalDate.of(2026, 10, 1)));
    }

    // ---------------------------------------------------------------- RN-15

    @Test
    void calcularSaldoDiferenciaCargosPagos() {
        assertEquals(new BigDecimal("300000"), Saldo.de(pesos(500_000), pesos(200_000)).valor());
        Saldo aFavor = Saldo.de(pesos(100_000), pesos(250_000));
        assertTrue(aFavor.esSaldoAFavor());
        assertEquals(-150_000, aFavor.enPesos());
    }

    // ---------------------------------------------------------------- 3.4

    @Test
    void dineroNoAdmiteNegativosYRedondeaAlPesoSoloCuandoSePide() {
        assertThrows(ReglaDominioException.class, () -> new Dinero(new BigDecimal("-1")));
        Dinero conDecimales = new Dinero(new BigDecimal("1000.50"));
        assertEquals(new BigDecimal("1000.50"), conDecimales.valor());
        assertEquals(1001, conDecimales.redondearAlPeso().enPesos());
    }

    // ---------------------------------------------------------------- RN-20 y 3.3

    @Test
    void cuatroHorasCabenEntreLaSalidaDeLas11YLaEntradaDeLas15() {
        LocalDate dia = LocalDate.of(2026, 10, 12);
        assertTrue(new TiempoPreparacion(4).seRespetaEntre(dia, dia, HORARIO));
    }

    @Test
    void siLaPreparacionExcedeLaVentanaNoSeRecibeElMismoDia() {
        LocalDate dia = LocalDate.of(2026, 10, 12);
        TiempoPreparacion cincoHoras = new TiempoPreparacion(5);
        assertFalse(cincoHoras.seRespetaEntre(dia, dia, HORARIO));
        assertTrue(cincoHoras.seRespetaEntre(dia, dia.plusDays(1), HORARIO));
    }

    // ---------------------------------------------------------------- L-20

    @Test
    void recargoDel25PorCientoPorCadaOcupanteSobreLaBaseDeDos() {
        RecargoPersonaAdicional recargo = new RecargoPersonaAdicional(2, Porcentaje.de(25));
        assertEquals(Dinero.CERO, recargo.porNoche(pesos(250_000), 2));
        assertEquals(pesos(62_500), recargo.porNoche(pesos(250_000), 3));
        assertEquals(pesos(125_000), recargo.porNoche(pesos(250_000), 4));
    }

    // ---------------------------------------------------------------- L-21

    @Test
    void jacuzziExigeLlegadaConTresHorasSobreElCheckIn() {
        Jacuzzi jacuzzi = new Jacuzzi("Jacuzzi a leña", 3);
        assertEquals(LocalTime.of(18, 0), jacuzzi.llegadaMasTempranaAdmitida(HORARIO));
        assertFalse(jacuzzi.alcanzaAlistamiento(new HoraEstimadaLlegada(LocalTime.of(16, 30)), HORARIO));
        assertTrue(jacuzzi.alcanzaAlistamiento(new HoraEstimadaLlegada(LocalTime.of(18, 0)), HORARIO));
    }

    // ---------------------------------------------------------------- RN-19

    @Test
    void canalMasIdentificadorExternoDefinenLaIdentidad() {
        assertEquals(new IdentificadorExterno("booking_com", "EXT-1"), new IdentificadorExterno("BOOKING_COM", "EXT-1"));
        assertFalse(new IdentificadorExterno("BOOKING_COM", "EXT-1").equals(new IdentificadorExterno("AIRBNB", "EXT-1")));
    }
}
