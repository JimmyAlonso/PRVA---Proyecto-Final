package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Domo;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.HorarioAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.Porcentaje;

/**
 * Sección 8: PENDIENTE → CONFIRMADA, "con anticipo si el equipo lo exige". JiyJu Glamping
 * exige 50 % (Ficha A.4, L-11). Además aplica la regla propia L-21 (alistamiento del Jacuzzi).
 *
 * <p>Ninguna de las dos condiciones la puede evaluar la Reserva sola: el anticipo está en el
 * Folio y el Jacuzzi en el Domo. Este servicio decide SI se confirma; la Reserva decide CÓMO
 * cambia de estado (RN-08, RN-09).
 */
public class ConfirmacionReservaService {

    private final Porcentaje anticipo;
    private final HorarioAlojamiento horario;

    public ConfirmacionReservaService(Porcentaje anticipo, HorarioAlojamiento horario) {
        if (anticipo == null || horario == null) {
            throw new ReglaDominioException("El anticipo y el horario son obligatorios.");
        }
        this.anticipo = anticipo;
        this.horario = horario;
    }

    public Dinero anticipoExigido(Reserva reserva) {
        return anticipo.aplicarA(reserva.getValorCongelado()).redondearAlPeso();
    }

    public void confirmar(Reserva reserva, Domo domo, Folio folio) {
        if (reserva == null || domo == null || folio == null) {
            throw new ReglaDominioException("Reserva, domo y folio son obligatorios para confirmar.");
        }
        if (!reserva.getDomo().equals(domo.getIdentificacion())) {
            throw new ReglaDominioException("El domo no corresponde al de la reserva.");
        }
        if (!folio.getReserva().equals(reserva.getCodigo())) {
            throw new ReglaDominioException("El folio no corresponde a la reserva.");
        }
        if (reserva.getHoraEstimadaLlegada() == null) {
            throw new ReglaDominioException("La reserva debe registrar hora estimada de llegada para confirmarse (RN-09).");
        }
        // L-21
        domo.verificarAlistamientoJacuzzi(reserva.getHoraEstimadaLlegada(), horario);
        // L-11: anticipo
        Dinero exigido = anticipoExigido(reserva);
        if (!folio.pagosCubren(exigido)) {
            throw new ReglaDominioException("Para confirmar se exige un anticipo de " + exigido.enPesos()
                    + " COP; el folio registra pagos por " + folio.totalPagos().enPesos() + " COP.");
        }
        reserva.confirmar();
    }
}
