package co.edu.uniquindio.sga.infrastructure.persistence.inmemory;

import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.repository.BloqueoRepository;
import co.edu.uniquindio.sga.domain.valueobject.IdBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class BloqueoRepositoryInMemory implements BloqueoRepository {

    private final Map<IdBloqueo, Bloqueo> bloqueos = new ConcurrentHashMap<>();

    @Override
    public List<Bloqueo> buscarVigentesPorDomo(IdentificacionDomo domo, Periodo periodo) {
        return bloqueos.values().stream()
                .filter(bloqueo -> bloqueo.getDomo().equals(domo))
                .filter(Bloqueo::isVigente)
                .filter(bloqueo -> bloqueo.getPeriodo().seSolapaCon(periodo))
                .toList();
    }

    @Override
    public List<Bloqueo> listarPorDomo(IdentificacionDomo domo) {
        return bloqueos.values().stream()
                .filter(bloqueo -> bloqueo.getDomo().equals(domo))
                .sorted(Comparator.comparing(bloqueo -> bloqueo.getPeriodo().desde()))
                .toList();
    }

    @Override
    public Optional<Bloqueo> obtenerPorId(IdBloqueo id) {
        return Optional.ofNullable(bloqueos.get(id));
    }

    @Override
    public void guardar(Bloqueo bloqueo) {
        bloqueos.put(bloqueo.getId(), bloqueo);
    }
}
