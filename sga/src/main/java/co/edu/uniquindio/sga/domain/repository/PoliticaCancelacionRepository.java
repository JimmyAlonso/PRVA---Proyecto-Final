package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.entity.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PoliticaCancelacionRepository {

    /** RN-22: la versión que rige en la fecha indicada es la que se congela al crear la reserva. */
    Optional<PoliticaCancelacion> obtenerVigenteEn(LocalDate fecha);

    /** RN-13: la retención se calcula con la versión congelada en la reserva, no con la vigente hoy. */
    Optional<PoliticaCancelacion> obtenerPorVersion(VersionPolitica version);

    /** F-07: la última versión publicada, de la que se deriva la siguiente. */
    Optional<PoliticaCancelacion> obtenerUltima();

    /** 7.4: historial completo de versiones, de la más reciente a la más antigua. */
    List<PoliticaCancelacion> listarVersiones();

    /** 7.4: al actualizar la política se conserva la versión anterior. */
    void guardar(PoliticaCancelacion politica);
}
