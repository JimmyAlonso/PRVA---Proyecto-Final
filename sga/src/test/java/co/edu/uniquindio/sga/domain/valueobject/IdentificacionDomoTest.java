package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// L-04: la identificación de cada domo es única y estable
class IdentificacionDomoTest {

    @Test
    void verificarDosIdentificadoresConElMismoValorSonIguales() {
        IdentificacionDomo idUno = new IdentificacionDomo("dom-01");
        IdentificacionDomo idDos = new IdentificacionDomo(" DOM-01 ");

        assertEquals(idUno, idDos);
        assertEquals(idUno.hashCode(), idDos.hashCode());
    }

    @Test
    void normalizarAMayusculas() {
        assertEquals("DOM-01", new IdentificacionDomo("dom-01").valor());
    }

    @Test
    void rechazarIdentificacionVacia() {
        assertThrows(ReglaDominioException.class, () -> new IdentificacionDomo("  "));
    }
}
