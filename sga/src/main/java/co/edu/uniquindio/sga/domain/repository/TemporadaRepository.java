package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.entity.Temporada;

import java.util.List;

public interface TemporadaRepository {
    List<Temporada> listarTodas();
    void guardar(Temporada temporada);
}
