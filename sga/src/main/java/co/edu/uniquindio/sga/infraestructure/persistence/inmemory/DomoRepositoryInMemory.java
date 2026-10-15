package co.edu.uniquindio.sga.infraestructure.persistence.inmemory;

import co.edu.uniquindio.sga.domain.entity.Domo;
import co.edu.uniquindio.sga.domain.repository.DomoRepository;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DomoRepositoryInMemory implements DomoRepository {

    private final Map<IdentificacionDomo, Domo> domos = new ConcurrentHashMap<>();

    @Override
    public Optional<Domo> obtenerPorIdentificacion(IdentificacionDomo identificacion) {
        return Optional.ofNullable(domos.get(identificacion));
    }

    @Override
    public List<Domo> listarActivos() {
        return listarTodos().stream().filter(Domo::estaActivo).toList();
    }

    @Override
    public List<Domo> listarTodos() {
        return domos.values().stream()
                .sorted(Comparator.comparing(domo -> domo.getIdentificacion().valor()))
                .toList();
    }

    @Override
    public void guardar(Domo domo) {
        domos.put(domo.getIdentificacion(), domo);
    }
}
