package co.edu.uniquindio.sga.domain.valueobject;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

//Test para un objeto de valor
class IdentificacionDomoTest {

    @Test
    void verificarDosIdentificadoresConElMismoValorSonIguales(){
        IdentificacionDomo idUno = new IdentificacionDomo("apto:01");
        IdentificacionDomo idDos = new IdentificacionDomo("APTO:01");

        assertEquals(idUno,idDos);
        assertEquals(idUno.hashCode(),idDos.hashCode());
    }

    @Test
    void normalizarAMayusculas(){
        IdentificacionDomo idUno = new IdentificacionDomo("apto:01");

        assertEquals("APTO:01",idUno.valor());
    }

}