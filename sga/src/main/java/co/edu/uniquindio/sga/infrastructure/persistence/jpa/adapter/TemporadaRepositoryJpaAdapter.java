package co.edu.uniquindio.sga.infrastructure.persistence.jpa.adapter;

import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;
import co.edu.uniquindio.sga.domain.valueobject.IdTemporada;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.CatalogoPersistenceMapper;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.TemporadaSpringDataRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TemporadaRepositoryJpaAdapter implements TemporadaRepository {

    private final TemporadaSpringDataRepository repositorio;
    private final CatalogoPersistenceMapper mapper;

    public TemporadaRepositoryJpaAdapter(TemporadaSpringDataRepository repositorio, CatalogoPersistenceMapper mapper) {
        this.repositorio = repositorio;
        this.mapper = mapper;
    }

    @Override
    public List<Temporada> buscarTodas() {
        return repositorio.findAllByOrderByIdAsc().stream().map(mapper::aDominio).toList();
    }

    @Override
    public Optional<Temporada> obtenerPorId(IdTemporada id) {
        return repositorio.findById(id.valor()).map(mapper::aDominio);
    }

    @Override
    public void guardar(Temporada temporada) {
        repositorio.save(mapper.aEntidad(temporada));
    }
}
