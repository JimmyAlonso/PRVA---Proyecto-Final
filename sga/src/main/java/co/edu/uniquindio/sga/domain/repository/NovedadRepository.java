package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.entity.Novedad;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;
import co.edu.uniquindio.sga.domain.valueobject.TipoNovedad;

import java.util.List;

// Corrección: antes era una interfaz vacía y el servicio de conciliación llamaba guardar(...)
public interface NovedadRepository {

    void guardar(Novedad novedad);

    /** 7.6: historial de novedades de un domo, más reciente primero. */
    List<Novedad> listarPorDomo(IdentificacionDomo domo);

    /** 7.8: conflictos de canal pendientes de revisión del administrador. */
    List<Novedad> listarPorTipo(TipoNovedad tipo);
}
