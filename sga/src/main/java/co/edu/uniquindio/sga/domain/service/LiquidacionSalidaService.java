package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Domo;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.AutorizacionCierre;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;

/**
 * Registro de salida (check-out), 7.6: la reserva pasa a FINALIZADA, el domo a
 * PENDIENTE_PREPARACION y el folio se cierra. RN-17: si el saldo no es cero, se exige la
 * autorización del administrador (autor y motivo). Quién puede darla lo decide la seguridad,
 * no este servicio.
 *
 * <p>Corrección: antes este servicio guardaba en los repositorios. En arquitectura hexagonal
 * el dominio solo cambia los agregados; el caso de uso es quien los persiste.
 */
public class LiquidacionSalidaService {

    public void registrarSalida(Reserva reserva, Domo domo, Folio folio, AutorizacionCierre autorizacion) {
        if (reserva == null || domo == null || folio == null) {
            throw new ReglaDominioException("Reserva, domo y folio son obligatorios.");
        }
        if (!reserva.getDomo().equals(domo.getIdentificacion())) {
            throw new ReglaDominioException("El domo no corresponde al de la reserva.");
        }
        if (!folio.getReserva().equals(reserva.getCodigo())) {
            throw new ReglaDominioException("El folio no pertenece a la reserva.");
        }
        // Verificar todo antes de mutar nada
        if (!reserva.getEstado().puedeTransitarA(EstadoReserva.FINALIZADA)) {
            throw new ReglaDominioException("No es posible registrar la salida de una reserva " + reserva.getEstado() + ".");
        }
        if (!folio.puedeCerrarseSinAutorizacion() && autorizacion == null) {               // RN-17
            throw new ReglaDominioException("El folio tiene saldo de " + folio.saldo().enPesos()
                    + " COP: se requiere autorización del administrador para registrar la salida.");
        }
        folio.cerrar(autorizacion);
        reserva.registrarSalida();
        domo.marcarPendientePreparacion();
    }
}
