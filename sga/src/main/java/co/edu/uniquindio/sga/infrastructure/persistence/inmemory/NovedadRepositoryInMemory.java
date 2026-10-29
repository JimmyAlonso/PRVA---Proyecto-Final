package co.edu.uniquindio.sga.infrastructure.persistence.inmemory;

import co.edu.uniquindio.sga.domain.entity.Novedad;
import co.edu.uniquindio.sga.domain.repository.NovedadRepository;
import co.edu.uniquindio.sga.domain.valueobject.IdNovedad;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;
import co.edu.uniquindio.sga.domain.valueobject.TipoNovedad;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NovedadRepositoryInMemory implements NovedadRepository {

    private final Map<IdNovedad, Novedad> novedades = new ConcurrentHashMap<>();

    @Override
    public void guardar(Novedad novedad) {
        novedades.put(novedad.getId(), novedad);
    }

    @Override
    public List<Novedad> listarPorDomo(IdentificacionDomo domo) {
        return novedades.values().stream()
                .filter(novedad -> novedad.getDomo().equals(domo))
                .sorted(Comparator.comparing(Novedad::getMomentoReporte).reversed())
                .toList();
    }

    @Override
    public List<Novedad> listarPorTipo(TipoNovedad tipo) {
        return novedades.values().stream()
                .filter(novedad -> novedad.getTipo() == tipo)
                .sorted(Comparator.comparing(Novedad::getMomentoReporte).reversed())
                .toList();
    }
}
