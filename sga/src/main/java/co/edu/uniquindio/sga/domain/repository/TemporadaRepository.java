package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.valueobject.IdTemporada;

import java.util.List;
import java.util.Optional;

public interface TemporadaRepository {

    List<Temporada> buscarTodas();

    Optional<Temporada> obtenerPorId(IdTemporada id);

    void guardar(Temporada temporada);
}
