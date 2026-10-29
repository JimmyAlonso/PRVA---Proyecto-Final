package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.entity.Domo;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.DomoRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.IdBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;
import co.edu.uniquindio.sga.domain.valueobject.MotivoBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;

import java.util.List;

/**
 * Reglas del inventario (7.3) que el agregado Domo no puede evaluar solo, porque necesitan ver
 * las reservas del domo, que son otro agregado:
 * <ul>
 *   <li>La identificación de cada domo es única (L-04).</li>
 *   <li>Un domo solo se retira de la venta si no tiene reservas activas ni futuras.</li>
 *   <li>No puede registrarse un bloqueo sobre noches que ya tengan reservas activas.</li>
 *   <li>Al reducir la capacidad, las reservas ya creadas no cambian, pero se advierten.</li>
 * </ul>
 */
public class InventarioDomoService {

    private final ReservaRepository reservaRepository;
    private final DomoRepository domoRepository;

    public InventarioDomoService(ReservaRepository reservaRepository, DomoRepository domoRepository) {
        if (reservaRepository == null || domoRepository == null) {
            throw new ReglaDominioException("Los repositorios de reservas y domos son obligatorios.");
        }
        this.reservaRepository = reservaRepository;
        this.domoRepository = domoRepository;
    }

    /** L-04: la identificación de cada domo es única y estable; tampoco se reutiliza la de un domo retirado. */
    public void verificarIdentificacionDisponible(IdentificacionDomo identificacion) {
        if (domoRepository.existe(identificacion)) {
            throw new ReglaDominioException("Ya existe un domo con la identificación " + identificacion.valor() + ".");
        }
    }

    /** 7.3: retiro con eliminación lógica, solo sin reservas activas (PENDIENTE, CONFIRMADA o EN_CURSO). */
    public void retirarDeVenta(Domo domo) {
        if (domo == null) {
            throw new ReglaDominioException("El domo es obligatorio.");
        }
        List<Reserva> activas = reservaRepository.buscarActivasPorDomo(domo.getIdentificacion()).stream()
                .filter(Reserva::estaActiva)
                .toList();
        if (!activas.isEmpty()) {
            throw new ReglaDominioException("No se puede retirar el domo " + domo.getIdentificacion().valor()
                    + ": tiene " + activas.size() + " reserva(s) activa(s) o futura(s).");
        }
        domo.retirarDeVenta();
    }

    /** 7.3 Bloqueos: no se bloquean noches que ya tengan reservas activas. */
    public Bloqueo registrarBloqueo(IdBloqueo id, Domo domo, Periodo periodo, MotivoBloqueo motivo, String observacion) {
        if (domo == null || periodo == null) {
            throw new ReglaDominioException("El domo y el periodo del bloqueo son obligatorios.");
        }
        if (domo.isEliminado()) {
            throw new ReglaDominioException("No se bloquea un domo retirado de la venta.");
        }
        boolean chocaConReserva = reservaRepository.buscarActivasPorDomo(domo.getIdentificacion(), periodo).stream()
                .filter(Reserva::estaActiva)
                .anyMatch(reserva -> reserva.getEstancia().nochesOcupadas().stream().anyMatch(periodo::contiene));
        if (chocaConReserva) {
            throw new ReglaDominioException("No se puede bloquear: el domo tiene reservas activas en esas noches.");
        }
        return new Bloqueo(id, domo.getIdentificacion(), periodo, motivo, observacion);
    }

    /** 7.3: reservas activas que quedan por encima de la capacidad actual del domo (se advierten, no se cambian). */
    public List<Reserva> reservasQueSuperanCapacidad(Domo domo) {
        return reservaRepository.buscarActivasPorDomo(domo.getIdentificacion()).stream()
                .filter(Reserva::estaActiva)
                .filter(reserva -> reserva.totalOcupantes() > domo.getCapacidad())
                .toList();
    }
}
