package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.entity.Domo;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;

import java.util.List;
import java.util.Optional;

// Corrección: antes importaba org.hibernate.internal.util.Optional (no compila y además
// metía el framework en el dominio) y clases que no usaba.
public interface DomoRepository {

    Optional<Domo> obtenerPorIdentificacion(IdentificacionDomo identificacion);

    /** 7.3: los listados y búsquedas ignoran los domos eliminados (inactivos). */
    List<Domo> listarActivos();

    List<Domo> listarTodos();

    void guardar(Domo domo);
}
