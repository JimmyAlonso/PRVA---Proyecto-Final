package co.edu.uniquindio.sga.infrastructure.persistence.jpa.adapter;

import co.edu.uniquindio.sga.domain.entity.Domo;
import co.edu.uniquindio.sga.domain.repository.DomoRepository;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.DomoPersistenceMapper;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.DomoSpringDataRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DomoRepositoryJpaAdapter implements DomoRepository {

    private final DomoSpringDataRepository repositorio;
    private final DomoPersistenceMapper mapper;

    public DomoRepositoryJpaAdapter(DomoSpringDataRepository repositorio, DomoPersistenceMapper mapper) {
        this.repositorio = repositorio;
        this.mapper = mapper;
    }

    @Override
    public Optional<Domo> obtenerPorIdentificacion(IdentificacionDomo identificacion) {
        return repositorio.findById(identificacion.valor()).map(mapper::aDominio);
    }

    @Override
    public List<Domo> listarActivos() {
        return repositorio.findByEliminadoFalseAndActivoTrueOrderByIdentificacionAsc().stream().map(mapper::aDominio).toList();
    }

    @Override
    public List<Domo> listarNoEliminados() {
        return repositorio.findByEliminadoFalseOrderByIdentificacionAsc().stream().map(mapper::aDominio).toList();
    }

    @Override
    public List<Domo> listarTodos() {
        return repositorio.findAllByOrderByIdentificacionAsc().stream().map(mapper::aDominio).toList();
    }

    @Override
    public boolean existe(IdentificacionDomo identificacion) {
        return repositorio.existsById(identificacion.valor());
    }

    @Override
    public void guardar(Domo domo) {
        repositorio.save(mapper.aEntidad(domo));
    }
}
