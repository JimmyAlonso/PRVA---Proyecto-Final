package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;

import java.util.List;

public interface BloqueoRepository {

    List<Bloqueo> buscarVigentesPorApartamento(IdentificacionDomo apartamento, Periodo periodo);
}
