package co.edu.uniquindio.sga.infraestructure.persistence.inmemory;

import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.CriteriosBusquedaReserva;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;
import co.edu.uniquindio.sga.domain.valueobject.IdentificadorExterno;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adaptador en memoria (I-02). Se reemplaza por JPA sin tocar el dominio (12.2).
 */
public class ReservaRepositoryInMemory implements ReservaRepository {

    private final Map<CodigoReserva, Reserva> reservas = new ConcurrentHashMap<>();

    @Override
    public List<Reserva> buscarActivasPorDomo(IdentificacionDomo domo, Periodo periodo) {
        return reservas.values().stream()
                .filter(reserva -> reserva.getDomo().equals(domo))
                .filter(Reserva::estaActiva)
                .filter(reserva -> !reserva.getEstancia().fechaSalida().isBefore(periodo.desde())
                        && !reserva.getEstancia().fechaEntrada().isAfter(periodo.hasta()))
                .toList();
    }

    @Override
    public Optional<Reserva> obtenerPorCodigo(CodigoReserva codigo) {
        return Optional.ofNullable(reservas.get(codigo));
    }

    @Override
    public List<Reserva> buscarPorEstado(EstadoReserva estado) {
        return reservas.values().stream()
                .filter(reserva -> reserva.getEstado() == estado)
                .sorted(masRecientePrimero())
                .toList();
    }

    @Override
    public List<Reserva> buscar(CriteriosBusquedaReserva criterios) {
        return reservas.values().stream()
                .filter(criterios::cumple)
                .sorted(masRecientePrimero())
                .toList();
    }

    /** Corrección RN-19: antes devolvía siempre Optional.empty() y la idempotencia nunca funcionaba. */
    @Override
    public Optional<Reserva> buscarPorIdentificadorExterno(IdentificadorExterno identificador) {
        return reservas.values().stream()
                .filter(reserva -> reserva.getIdentificadorExterno().map(identificador::equals).orElse(false))
                .findFirst();
    }

    @Override
    public void guardar(Reserva reserva) {
        reservas.put(reserva.getCodigo(), reserva);
    }

    // 7.5: de la más reciente a la más antigua
    private static Comparator<Reserva> masRecientePrimero() {
        return Comparator.comparing(Reserva::getMomentoCreacion)
                .thenComparing(reserva -> reserva.getCodigo().valor())
                .reversed();
    }
}
