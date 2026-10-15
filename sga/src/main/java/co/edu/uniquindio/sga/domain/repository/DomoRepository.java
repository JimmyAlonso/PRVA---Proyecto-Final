package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.entity.Domo;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Novedad;
import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;
import org.hibernate.internal.util.Optional;

import java.util.List;

  public interface DomoRepository {
    Optional<Domo> obtenerPorIdentificacion(IdentificacionDomo identificacion);
    List<Domo> listarActivos();
    void guardar(Domo domo);
}