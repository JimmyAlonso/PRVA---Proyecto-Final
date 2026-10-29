package co.edu.uniquindio.sga.infrastructure.persistence.inmemory;

import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;
import co.edu.uniquindio.sga.domain.valueobject.IdTemporada;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class TemporadaRepositoryInMemory implements TemporadaRepository {

    private final Map<IdTemporada, Temporada> temporadas = new ConcurrentHashMap<>();

    @Override
    public List<Temporada> buscarTodas() {
        return List.copyOf(temporadas.values());
    }

    @Override
    public Optional<Temporada> obtenerPorId(IdTemporada id) {
        return Optional.ofNullable(temporadas.get(id));
    }

    @Override
    public void guardar(Temporada temporada) {
        temporadas.put(temporada.getId(), temporada);
    }
}
