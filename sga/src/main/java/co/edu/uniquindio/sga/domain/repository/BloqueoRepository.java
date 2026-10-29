package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.valueobject.IdBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;

import java.util.List;
import java.util.Optional;

public interface BloqueoRepository {

    /** RN-07: bloqueos vigentes del domo que se solapan con el periodo. */
    List<Bloqueo> buscarVigentesPorDomo(IdentificacionDomo domo, Periodo periodo);

    List<Bloqueo> listarPorDomo(IdentificacionDomo domo);

    Optional<Bloqueo> obtenerPorId(IdBloqueo id);

    void guardar(Bloqueo bloqueo);
}
