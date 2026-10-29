package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.HoraEstimadaLlegada;
import co.edu.uniquindio.sga.domain.valueobject.IdTemporada;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;
import co.edu.uniquindio.sga.domain.valueobject.Porcentaje;
import co.edu.uniquindio.sga.domain.valueobject.Tarifa;
import co.edu.uniquindio.sga.domain.valueobject.TramoRetencion;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static co.edu.uniquindio.sga.Fixtures.ALTA;
import static co.edu.uniquindio.sga.Fixtures.ANA;
import static co.edu.uniquindio.sga.Fixtures.BASE;
import static co.edu.uniquindio.sga.Fixtures.HOY;
import static co.edu.uniquindio.sga.Fixtures.LUIS;
import static co.edu.uniquindio.sga.Fixtures.PEDRO;
import static co.edu.uniquindio.sga.Fixtures.domoBasico;
import static co.edu.uniquindio.sga.Fixtures.estancia;
import static co.edu.uniquindio.sga.Fixtures.pesos;
import static co.edu.uniquindio.sga.Fixtures.politicaFicha;
import static co.edu.uniquindio.sga.Fixtures.reservaBasica;
import static co.edu.uniquindio.sga.Fixtures.temporadas;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Corte 2: gestión del inventario (7.3), tarifas y temporadas (7.4) y política versionada (F-07). */
class InventarioYCatalogoTest {

    @Test
    void unDomoNuevoNaceInactivoYSoloSeActivaConTarifasCompletas() {                      // 7.4
        Domo domo = Domo.registrar(new IdentificacionDomo("DOM-06"), "Domo Mirador", 1, 2);
        assertFalse(domo.estaActivo());
        domo.establecerTarifa(new Tarifa(BASE, pesos(190_000)), HOY);
        assertThrows(ReglaDominioException.class, () -> domo.activar(temporadas()));
        domo.establecerTarifa(new Tarifa(ALTA, pesos(290_000)), HOY);
        domo.activar(temporadas());
        assertTrue(domo.estaActivo());
    }

    @Test
    void cambiarUnaTarifaGuardaElValorAnteriorEnElHistorico() {                           // 7.4
        Domo domo = domoBasico();
        domo.establecerTarifa(new Tarifa(BASE, pesos(200_000)), HOY);
        assertEquals(pesos(200_000), domo.tarifaEn(BASE).orElseThrow().valorPorOcupanteNoche());
        assertEquals(1, domo.getHistorialTarifas().size());
        assertEquals(pesos(180_000), domo.getHistorialTarifas().get(0).valorPorOcupanteNoche());
        assertEquals(HOY, domo.getHistorialTarifas().get(0).reemplazadaEl());
    }

    @Test
    void retirarDeLaVentaEsEliminacionLogicaSinVueltaAtras() {                            // 7.3 y 12.4
        Domo domo = domoBasico();
        domo.retirarDeVenta();
        assertTrue(domo.isEliminado());
        assertFalse(domo.estaActivo());
        assertThrows(ReglaDominioException.class, () -> domo.activar(temporadas()));
        assertThrows(ReglaDominioException.class, () -> domo.actualizarDatos("Otro", null, 1, 2, List.of()));
    }

    @Test
    void elPersonalNoSacaUnDomoDeFueraDeServicio() {                                       // 7.1 y 7.6
        Domo domo = domoBasico();
        domo.declararFueraDeServicio();
        assertThrows(ReglaDominioException.class, domo::marcarPreparado);
        domo.restablecerServicio();
        assertEquals(EstadoOperativo.PREPARADO, domo.getEstadoOperativo());
        assertThrows(ReglaDominioException.class, domo::restablecerServicio);
    }

    @Test
    void laTemporadaBaseNoLlevaRangosYLasDemasSiLosNecesitan() {                           // 7.4
        assertThrows(ReglaDominioException.class, () -> new Temporada(BASE, "Base",
                List.of(new Periodo(HOY, HOY.plusDays(3))), true));
        Temporada alta = temporadas().get(1);
        assertThrows(ReglaDominioException.class, () -> alta.redefinir("Alta", List.of()));
        alta.redefinir("Alta diciembre", List.of(new Periodo(LocalDate.of(2026, 12, 20), LocalDate.of(2027, 1, 10))));
        assertEquals("Alta diciembre", alta.getNombre());
    }

    @Test
    void unaNuevaVersionDeLaPoliticaConservaLaAnterior() {                                 // F-07, RN-13
        PoliticaCancelacion v1 = politicaFicha();
        PoliticaCancelacion v2 = v1.nuevaVersion(List.of(new TramoRetencion(240, Porcentaje.de(20)),
                new TramoRetencion(0, Porcentaje.de(80))), Porcentaje.de(100), LocalDate.of(2026, 11, 1));
        assertEquals(2, v2.getVersion().numero());
        assertEquals(1, v1.getVersion().numero());
        assertThrows(ReglaDominioException.class, () -> v1.nuevaVersion(v1.getTramos(), Porcentaje.de(100),
                LocalDate.of(2025, 12, 31)));
    }

    @Test
    void reconstruirConservaElEstadoYElValorCongelado() {                                  // RN-22
        Reserva original = reservaBasica("RES-2026-00001", estancia("2026-10-10", "2026-10-12"));
        original.registrarHoraEstimadaLlegada(new HoraEstimadaLlegada(LocalTime.of(18, 30)));
        original.confirmar();
        Reserva copia = Reserva.reconstruir(original.getCodigo(), original.getDomo(), original.getEstancia(),
                original.getEstado(), original.getCanalOrigen(), null, original.getTitular(), original.getOcupantes(),
                original.getHoraEstimadaLlegada(), original.getValorCongelado(), original.getDesgloseCongelado(),
                original.getPoliticaCongelada(), original.getMomentoCreacion(), null);
        assertEquals(EstadoReserva.CONFIRMADA, copia.getEstado());
        assertEquals(original.getValorCongelado(), copia.getValorCongelado());
        assertEquals(original, copia);
    }

    @Test
    void unaModificacionConservaAlTitularComoOcupante() {                                  // 3.2 y RN-14
        Reserva reserva = reservaBasica("RES-2026-00001", estancia("2026-10-10", "2026-10-12"));
        List<Ocupante> grupo = reserva.conAcompanantes(List.of(PEDRO));
        assertEquals(List.of(ANA, PEDRO), grupo);
        assertEquals(estancia("2026-10-10", "2026-10-14"), reserva.estanciaCon(null, LocalDate.of(2026, 10, 14)));
        assertEquals(List.of(ANA, LUIS), reserva.getOcupantes());
    }

    @Test
    void elIdentificadorDeTemporadaEsObligatorio() {
        assertThrows(ReglaDominioException.class, () -> new IdTemporada(" "));
    }
}
