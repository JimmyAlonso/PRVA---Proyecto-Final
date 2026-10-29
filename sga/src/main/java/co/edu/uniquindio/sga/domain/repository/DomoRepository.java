package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.entity.Domo;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;

import java.util.List;
import java.util.Optional;

public interface DomoRepository {

    Optional<Domo> obtenerPorIdentificacion(IdentificacionDomo identificacion);

    /** 7.3: los listados y búsquedas ignoran los domos retirados de la venta y los inactivos. */
    List<Domo> listarActivos();

    /** Todos los domos no eliminados (activos e inactivos), para la administración del inventario. */
    List<Domo> listarNoEliminados();

    List<Domo> listarTodos();

    boolean existe(IdentificacionDomo identificacion);

    void guardar(Domo domo);
}
