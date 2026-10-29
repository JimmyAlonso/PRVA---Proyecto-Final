package co.edu.uniquindio.sga.infrastructure.persistence.jpa.adapter;

import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.repository.BloqueoRepository;
import co.edu.uniquindio.sga.domain.valueobject.IdBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.CatalogoPersistenceMapper;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.BloqueoSpringDataRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class BloqueoRepositoryJpaAdapter implements BloqueoRepository {

    private final BloqueoSpringDataRepository repositorio;
    private final CatalogoPersistenceMapper mapper;

    public BloqueoRepositoryJpaAdapter(BloqueoSpringDataRepository repositorio, CatalogoPersistenceMapper mapper) {
        this.repositorio = repositorio;
        this.mapper = mapper;
    }

    @Override
    public List<Bloqueo> buscarVigentesPorDomo(IdentificacionDomo domo, Periodo periodo) {
        return repositorio.buscarVigentesQueSeSolapan(domo.valor(), periodo.desde(), periodo.hasta()).stream()
                .map(mapper::aDominio).toList();
    }

    @Override
    public List<Bloqueo> listarPorDomo(IdentificacionDomo domo) {
        return repositorio.listarPorDomo(domo.valor()).stream().map(mapper::aDominio).toList();
    }

    @Override
    public Optional<Bloqueo> obtenerPorId(IdBloqueo id) {
        return repositorio.findById(id.valor()).map(mapper::aDominio);
    }

    @Override
    public void guardar(Bloqueo bloqueo) {
        repositorio.save(mapper.aEntidad(bloqueo));
    }
}
