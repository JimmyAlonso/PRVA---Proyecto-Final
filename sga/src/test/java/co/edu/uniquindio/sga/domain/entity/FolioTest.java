package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.AutorizacionCierre;
import co.edu.uniquindio.sga.domain.valueobject.Cargo;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.ConceptoCargo;
import co.edu.uniquindio.sga.domain.valueobject.Deposito;
import co.edu.uniquindio.sga.domain.valueobject.IdPago;
import co.edu.uniquindio.sga.domain.valueobject.MedioPago;
import org.junit.jupiter.api.Test;

import static co.edu.uniquindio.sga.Fixtures.HOY;
import static co.edu.uniquindio.sga.Fixtures.pesos;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FolioTest {

    private final CodigoReserva reserva = new CodigoReserva("RES-2026-00001");

    private Folio folioDe(long alojamiento) {
        return Folio.abrir(reserva, pesos(alojamiento), HOY);
    }

    private Pago pago(String id, long monto, MedioPago medio) {
        return new Pago(new IdPago(id), pesos(monto), medio, HOY);
    }

    @Test
    void seAbreConElCargoDeAlojamientoYAdmitePagosParcialesConMediosDistintos() {        // 7.7, RN-15
        Folio folio = folioDe(720_000);
        folio.registrarPago(pago("P1", 360_000, MedioPago.TRANSFERENCIA));
        folio.registrarPago(pago("P2", 100_000, MedioPago.TARJETA));
        assertEquals(720_000, folio.totalCargos().enPesos());
        assertEquals(260_000, folio.saldo().enPesos());
    }

    @Test
    void elSaldoPuedeQuedarAFavorDelHuesped() {                                           // 7.7
        Folio folio = folioDe(100_000);
        folio.registrarPago(pago("P1", 150_000, MedioPago.EFECTIVO));
        assertTrue(folio.saldo().esSaldoAFavor());
        assertEquals(-50_000, folio.saldo().enPesos());
    }

    @Test
    void noPermitirEdicionDirectaDeCargos() {                                             // RN-16
        Folio folio = folioDe(720_000);
        assertThrows(UnsupportedOperationException.class, () -> folio.getCargos().clear());
        assertThrows(UnsupportedOperationException.class, () -> folio.getPagos().clear());
        // La corrección es un movimiento inverso, que además no puede superar lo cargado
        assertThrows(ReglaDominioException.class, () -> folio.registrarMovimientoInverso(
                new Cargo(ConceptoCargo.SERVICIO, pesos(10_000), HOY, "no existe", true)));
        folio.registrarMovimientoInverso(new Cargo(ConceptoCargo.ALOJAMIENTO, pesos(20_000), HOY, "corrección", true));
        assertEquals(700_000, folio.totalCargos().enPesos());
        assertEquals(2, folio.getCargos().size());
    }

    @Test
    void noCerrarConSaldoSinAutorizacion() {                                              // RN-17
        Folio folio = folioDe(720_000);
        assertFalse(folio.puedeCerrarseSinAutorizacion());
        assertThrows(ReglaDominioException.class, () -> folio.cerrar(null));
        folio.cerrar(new AutorizacionCierre("admin", "Pago pendiente acordado"));
        assertTrue(folio.isCerrado());
        assertEquals("admin", folio.getAutorizacionCierre().autor());
        assertThrows(ReglaDominioException.class, () -> folio.registrarPago(pago("P9", 1, MedioPago.EFECTIVO)));
    }

    @Test
    void conSaldoCeroCierraSinAutorizacion() {
        Folio folio = folioDe(200_000);
        folio.registrarPago(pago("P1", 200_000, MedioPago.EFECTIVO));
        folio.cerrar(null);
        assertTrue(folio.isCerrado());
    }

    @Test
    void depositoRegistradoYDevuelto() {                                                   // L-22
        Folio folio = folioDe(500_000);
        assertFalse(folio.tieneDepositoRegistrado());
        folio.registrarDeposito(new Deposito(pesos(100_000)), HOY);
        assertTrue(folio.tieneDepositoRegistrado());
        assertThrows(ReglaDominioException.class, () -> folio.registrarDeposito(new Deposito(pesos(100_000)), HOY));
        folio.devolverDeposito(HOY);
        assertFalse(folio.tieneDepositoRegistrado());
        assertEquals(500_000, folio.totalCargos().enPesos());
    }

    @Test
    void ajustePositivoYNegativoPorModificacion() {                                        // RN-14
        Folio folio = folioDe(720_000);
        folio.registrarAjuste(pesos(720_000), pesos(1_080_000), HOY, "una noche más");
        assertEquals(1_080_000, folio.totalCargos().enPesos());
        folio.registrarAjuste(pesos(1_080_000), pesos(360_000), HOY, "dos noches menos");
        assertEquals(360_000, folio.totalCargos().enPesos());
    }

    @Test
    void liquidarTerminacionDejaSoloLaRetencionYElSaldoAFavor() {                          // RN-13, 7.5
        Folio folio = folioDe(1_000_000);
        folio.registrarPago(pago("P1", 500_000, MedioPago.TRANSFERENCIA));
        folio.liquidarTerminacion(pesos(1_000_000), pesos(100_000), HOY, "cancelación");
        assertEquals(100_000, folio.totalCargos().enPesos());
        assertEquals(-400_000, folio.saldo().enPesos());     // devolución registrada como saldo a favor
    }
}
