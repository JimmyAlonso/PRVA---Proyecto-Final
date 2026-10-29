package co.edu.uniquindio.sga.infrastructure.security;

import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * 7.2: "un huésped nunca puede acceder a reservas, folios ni datos de otros huéspedes. Esta
 * restricción se verifica sobre el propietario del recurso, no solo sobre el rol". El
 * propietario de una reserva es el usuario cuyo documento coincide con el del titular.
 *
 * <p>Se usa desde @PreAuthorize en los controllers: {@code @accesoReservas.puedeAcceder(#codigo, authentication)}.
 */
@Component("accesoReservas")
public class AccesoReservas {

    private final ReservaRepository reservaRepository;

    public AccesoReservas(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    /** Recepción y administración ven cualquier reserva; el huésped solo las suyas. */
    public boolean puedeAcceder(String codigo, Authentication autenticacion) {
        UsuarioAutenticado usuario = usuario(autenticacion);
        if (usuario == null) {
            return false;
        }
        if (usuario.esPersonalDeRecepcion()) {
            return true;
        }
        if (!usuario.esHuesped() || codigo == null || codigo.isBlank()) {
            return false;
        }
        return reservaRepository.obtenerPorCodigo(new CodigoReserva(codigo))
                .map(reserva -> reserva.getTitular().getDocumento().valor().equals(usuario.documento()))
                // Si la reserva no existe se deja pasar para que el caso de uso responda 404
                .orElse(true);
    }

    /**
     * 7.1: el huésped reserva por el canal PORTAL y a su propio nombre; la recepción registra por
     * el canal DIRECTO a nombre de un titular que puede no tener cuenta.
     */
    public void verificarCreacion(CanalOrigen canal, String documentoTitular, UsuarioAutenticado usuario) {
        if (usuario.esHuesped()) {
            if (canal != CanalOrigen.PORTAL) {
                throw new AccessDeniedException("El huésped solo reserva por el canal PORTAL.");
            }
            if (!usuario.documento().equals(documentoTitular == null ? null : documentoTitular.trim())) {
                throw new AccessDeniedException("El huésped solo puede crear reservas a su propio nombre.");
            }
            return;
        }
        if (usuario.esPersonalDeRecepcion() && canal != CanalOrigen.DIRECTO) {
            throw new AccessDeniedException("La recepción registra reservas por el canal DIRECTO.");
        }
    }

    public static UsuarioAutenticado usuario(Authentication autenticacion) {
        if (autenticacion != null && autenticacion.getPrincipal() instanceof UsuarioAutenticado usuario) {
            return usuario;
        }
        return null;
    }
}
