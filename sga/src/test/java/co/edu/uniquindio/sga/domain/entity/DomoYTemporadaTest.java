package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.service.CalendarioTemporadasService;
import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import co.edu.uniquindio.sga.domain.valueobject.HoraEstimadaLlegada;
import co.edu.uniquindio.sga.domain.valueobject.IdTemporada;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static co.edu.uniquindio.sga.Fixtures.HORARIO;
import static co.edu.uniquindio.sga.Fixtures.domoBasico;
import static co.edu.uniquindio.sga.Fixtures.domoExclusivo;
import static co.edu.uniquindio.sga.Fixtures.temporadas;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DomoYTemporadaTest {

    @Test
    void noPermitirEntregaSiNoEstaPreparado() {                                           // RN-11
        Domo domo = domoBasico();
        assertTrue(domo.puedeRecibirGrupo());
        domo.declararFueraDeServicio();
        assertFalse(domo.puedeRecibirGrupo());
        assertThrows(ReglaDominioException.class, domo::marcarOcupado);
    }

    @Test
    void transicionesDelEstadoOperativo() {                                               // 7.6
        Domo domo = domoBasico();
        domo.marcarOcupado();
        assertThrows(ReglaDominioException.class, domo::declararFueraDeServicio);         // no desde OCUPADO
        assertThrows(ReglaDominioException.class, domo::iniciarPreparacion);
        domo.marcarPendientePreparacion();
        domo.iniciarPreparacion();
        domo.marcarPreparado();
        assertEquals(EstadoOperativo.PREPARADO, domo.getEstadoOperativo());
    }

    @Test
    void alistamientoDelJacuzziAlConfirmar() {                                            // L-21
        Domo conJacuzzi = domoBasico();
        assertThrows(ReglaDominioException.class,
                () -> conJacuzzi.verificarAlistamientoJacuzzi(new HoraEstimadaLlegada(LocalTime.of(16, 0)), HORARIO));
        assertDoesNotThrow(() -> conJacuzzi.verificarAlistamientoJacuzzi(new HoraEstimadaLlegada(LocalTime.of(18, 0)), HORARIO));
        // Un domo sin jacuzzi (DOM-04) no tiene esa restricción
        assertDoesNotThrow(() -> domoExclusivo().verificarAlistamientoJacuzzi(new HoraEstimadaLlegada(LocalTime.of(15, 0)), HORARIO));
    }

    @Test
    void noAceptaReservasSinTarifasCompletasOInactivo() {                                 // 7.4, 7.5 condición 3
        Domo sinTarifas = new Domo(new IdentificacionDomo("DOM-09"), "Nuevo", 1, 2);
        assertThrows(ReglaDominioException.class, () -> sinTarifas.verificarQueAceptaReservas(temporadas()));
        assertThrows(ReglaDominioException.class, () -> sinTarifas.activar(temporadas()));

        Domo domo = domoBasico();
        domo.desactivar();
        assertThrows(ReglaDominioException.class, () -> domo.verificarQueAceptaReservas(temporadas()));
    }

    @Test
    void laTemporadaAltaPuedeTenerVariosRangos() {                                        // L-08
        Temporada alta = new Temporada(new IdTemporada("ALTA"), "Alta", List.of(
                new Periodo(LocalDate.of(2026, 12, 15), LocalDate.of(2027, 1, 15)),
                new Periodo(LocalDate.of(2027, 3, 22), LocalDate.of(2027, 3, 28))), false);
        assertTrue(alta.cubre(LocalDate.of(2027, 1, 1)));
        assertTrue(alta.cubre(LocalDate.of(2027, 3, 25)));
        assertFalse(alta.cubre(LocalDate.of(2027, 2, 10)));
    }

    @Test
    void lasTemporadasNoSePuedenSolaparYLaBaseEsObligatoria() {                           // 7.4, F-05
        CalendarioTemporadasService calendario = new CalendarioTemporadasService();
        assertDoesNotThrow(() -> calendario.verificarCalendario(temporadas()));

        List<Temporada> conSolape = new ArrayList<>(temporadas());
        conSolape.add(new Temporada(new IdTemporada("PUENTES"), "Puentes",
                List.of(new Periodo(LocalDate.of(2027, 1, 8), LocalDate.of(2027, 1, 10))), false));
        assertThrows(ReglaDominioException.class, () -> calendario.verificarCalendario(conSolape));

        List<Temporada> sinBase = temporadas().stream().filter(t -> !t.isEsBase()).toList();
        assertThrows(ReglaDominioException.class, () -> calendario.verificarCalendario(sinBase));
    }
}
