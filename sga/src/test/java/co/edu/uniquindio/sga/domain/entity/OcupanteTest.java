package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.UmbralEdadFacturable;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static co.edu.uniquindio.sga.Fixtures.SOFIA_NINA;
import static co.edu.uniquindio.sga.Fixtures.UMBRAL;
import static co.edu.uniquindio.sga.Fixtures.estancia;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// RN-06: un ocupante es facturable si a la fecha de entrada alcanza el umbral (12 años en JiyJu)
class OcupanteTest {

    @Test
    void evaluarUmbralEdadFacturable() {
        Ocupante adulto = new Ocupante(new DocumentoIdentidad("CC10123567"), "Jhan Carlos Martinez Ceballos",
                LocalDate.of(2001, 1, 13));
        Estancia estancia = estancia("2026-09-21", "2026-09-23");

        assertTrue(adulto.esFacturableEn(estancia, new UmbralEdadFacturable(18)));
        assertFalse(SOFIA_NINA.esFacturableEn(estancia, UMBRAL));
    }

    @Test
    void quienCumpleElUmbralDuranteLaEstanciaNoCambiaDeCondicion() {
        // Cumple 12 años el 11 de octubre, en mitad de la estancia: sigue sin ser facturable
        Ocupante cumpleanero = new Ocupante(new DocumentoIdentidad("TI1111111111"), "Niño", LocalDate.of(2014, 10, 11));
        assertFalse(cumpleanero.esFacturableEn(estancia("2026-10-10", "2026-10-13"), UMBRAL));
        // Si entra el mismo día que cumple 12, sí es facturable
        assertTrue(cumpleanero.esFacturableEn(estancia("2026-10-11", "2026-10-13"), UMBRAL));
    }
}
