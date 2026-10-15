package co.edu.uniquindio.sga.infraestructure.persistence.inmemory;

import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

// 7.7: un folio pertenece a una sola reserva, por eso la llave es el código de la reserva
public class FolioRepositoryInMemory implements FolioRepository {

    private final Map<CodigoReserva, Folio> folios = new ConcurrentHashMap<>();

    @Override
    public Optional<Folio> obtenerPorReserva(CodigoReserva reserva) {
        return Optional.ofNullable(folios.get(reserva));
    }

    @Override
    public void guardar(Folio folio) {
        folios.put(folio.getReserva(), folio);
    }
}
