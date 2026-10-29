package co.edu.uniquindio.sga.infrastructure.persistence.inmemory;

import co.edu.uniquindio.sga.domain.entity.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.repository.PoliticaCancelacionRepository;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class PoliticaCancelacionRepositoryInMemory implements PoliticaCancelacionRepository {

    private final Map<VersionPolitica, PoliticaCancelacion> politicas = new ConcurrentHashMap<>();

    @Override
    public Optional<PoliticaCancelacion> obtenerVigenteEn(LocalDate fecha) {
        // La vigente es la versión más reciente que ya empezó a regir en esa fecha
        return politicas.values().stream()
                .filter(politica -> !politica.getVersion().vigenteDesde().isAfter(fecha))
                .max(Comparator.comparing((PoliticaCancelacion politica) -> politica.getVersion().vigenteDesde())
                        .thenComparing(politica -> politica.getVersion().numero()));
    }

    @Override
    public Optional<PoliticaCancelacion> obtenerPorVersion(VersionPolitica version) {
        return Optional.ofNullable(politicas.get(version));
    }

    @Override
    public Optional<PoliticaCancelacion> obtenerUltima() {
        return politicas.values().stream().max(Comparator.comparing(politica -> politica.getVersion().numero()));
    }

    @Override
    public List<PoliticaCancelacion> listarVersiones() {
        return politicas.values().stream()
                .sorted(Comparator.comparing((PoliticaCancelacion politica) -> politica.getVersion().numero()).reversed())
                .toList();
    }

    @Override
    public void guardar(PoliticaCancelacion politica) {
        politicas.put(politica.getVersion(), politica);
    }
}
