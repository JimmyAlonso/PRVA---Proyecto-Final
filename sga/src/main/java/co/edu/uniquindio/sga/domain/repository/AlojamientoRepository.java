package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.entity.Alojamiento;

/**
 * F-13: el sistema administra un único alojamiento, así que no hay búsqueda por identificador.
 * Sus parámetros de operación (sección 6) son configuración; lo que el administrador gestiona
 * (descripción, dirección, ubicación, normas y servicios) se guarda aquí.
 */
public interface AlojamientoRepository {

    Alojamiento obtener();

    void guardar(Alojamiento alojamiento);
}
