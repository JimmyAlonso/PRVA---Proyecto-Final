package co.edu.uniquindio.sga.infrastructure.persistence.jpa.adapter;

import co.edu.uniquindio.sga.domain.entity.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.repository.PoliticaCancelacionRepository;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.CatalogoPersistenceMapper;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.PoliticaCancelacionSpringDataRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class PoliticaCancelacionRepositoryJpaAdapter implements PoliticaCancelacionRepository {

    private final PoliticaCancelacionSpringDataRepository repositorio;
    private final CatalogoPersistenceMapper mapper;

    public PoliticaCancelacionRepositoryJpaAdapter(PoliticaCancelacionSpringDataRepository repositorio,
                                                   CatalogoPersistenceMapper mapper) {
        this.repositorio = repositorio;
        this.mapper = mapper;
    }

    @Override
    public Optional<PoliticaCancelacion> obtenerVigenteEn(LocalDate fecha) {
        return repositorio.findFirstByVigenteDesdeLessThanEqualOrderByVigenteDesdeDescNumeroVersionDesc(fecha)
                .map(mapper::aDominio);
    }

    @Override
    public Optional<PoliticaCancelacion> obtenerPorVersion(VersionPolitica version) {
        return repositorio.findById(version.numero())
                .filter(entidad -> entidad.getVigenteDesde().equals(version.vigenteDesde()))
                .map(mapper::aDominio);
    }

    @Override
    public Optional<PoliticaCancelacion> obtenerUltima() {
        return repositorio.findFirstByOrderByNumeroVersionDesc().map(mapper::aDominio);
    }

    @Override
    public List<PoliticaCancelacion> listarVersiones() {
        return repositorio.findAllByOrderByNumeroVersionDesc().stream().map(mapper::aDominio).toList();
    }

    @Override
    public void guardar(PoliticaCancelacion politica) {
        repositorio.save(mapper.aEntidad(politica));
    }
}
