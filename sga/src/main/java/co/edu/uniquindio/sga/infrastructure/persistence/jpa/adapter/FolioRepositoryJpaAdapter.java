package co.edu.uniquindio.sga.infrastructure.persistence.jpa.adapter;

import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.FolioPersistenceMapper;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.FolioSpringDataRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class FolioRepositoryJpaAdapter implements FolioRepository {

    private final FolioSpringDataRepository repositorio;
    private final FolioPersistenceMapper mapper;

    public FolioRepositoryJpaAdapter(FolioSpringDataRepository repositorio, FolioPersistenceMapper mapper) {
        this.repositorio = repositorio;
        this.mapper = mapper;
    }

    @Override
    public Optional<Folio> obtenerPorReserva(CodigoReserva reserva) {
        return repositorio.findByCodigoReserva(reserva.valor()).map(mapper::aDominio);
    }

    @Override
    public void guardar(Folio folio) {
        repositorio.save(mapper.aEntidad(folio));
    }
}
