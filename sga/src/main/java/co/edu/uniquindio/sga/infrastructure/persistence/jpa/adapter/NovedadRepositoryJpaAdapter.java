package co.edu.uniquindio.sga.infrastructure.persistence.jpa.adapter;

import co.edu.uniquindio.sga.domain.entity.Novedad;
import co.edu.uniquindio.sga.domain.repository.NovedadRepository;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;
import co.edu.uniquindio.sga.domain.valueobject.TipoNovedad;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.CatalogoPersistenceMapper;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.NovedadSpringDataRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class NovedadRepositoryJpaAdapter implements NovedadRepository {

    private final NovedadSpringDataRepository repositorio;
    private final CatalogoPersistenceMapper mapper;

    public NovedadRepositoryJpaAdapter(NovedadSpringDataRepository repositorio, CatalogoPersistenceMapper mapper) {
        this.repositorio = repositorio;
        this.mapper = mapper;
    }

    @Override
    public void guardar(Novedad novedad) {
        repositorio.save(mapper.aEntidad(novedad));
    }

    @Override
    public List<Novedad> listarPorDomo(IdentificacionDomo domo) {
        return repositorio.findByDomoOrderByMomentoReporteDesc(domo.valor()).stream().map(mapper::aDominio).toList();
    }

    @Override
    public List<Novedad> listarPorTipo(TipoNovedad tipo) {
        return repositorio.findByTipoOrderByMomentoReporteDesc(tipo).stream().map(mapper::aDominio).toList();
    }
}
