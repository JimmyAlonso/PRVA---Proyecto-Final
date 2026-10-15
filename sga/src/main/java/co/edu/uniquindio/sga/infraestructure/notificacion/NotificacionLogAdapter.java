package co.edu.uniquindio.sga.infraestructure.notificacion;

import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.port.NotificacionService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementación alternativa local del puerto de notificaciones (I-02, I-08): no envía nada,
 * deja el mensaje en el log y en una bandeja en memoria. Permite operar y probar sin internet
 * ni credenciales. El adaptador SMTP de la ficha (EmailNotificationAdapter, timeout 3 s) se
 * conecta detrás del mismo puerto cuando se agregue spring-boot-starter-mail.
 *
 * <p>I-04 / I-05: cualquier fallo se captura y se registra; nunca se propaga al caso de uso.
 */
public class NotificacionLogAdapter implements NotificacionService {

    private static final Logger LOG = Logger.getLogger(NotificacionLogAdapter.class.getName());

    private final List<String> enviados = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void reservaCreada(Reserva reserva) {
        enviar(reserva, "Recibimos su reserva " + reserva.getCodigo().valor() + " en estado PENDIENTE por "
                + reserva.getValorCongelado().enPesos() + " COP.");
    }

    @Override
    public void reservaConfirmada(Reserva reserva) {
        enviar(reserva, "Su reserva " + reserva.getCodigo().valor() + " quedó CONFIRMADA. Llegada estimada: "
                + reserva.getHoraEstimadaLlegada().hora() + ".");
    }

    @Override
    public void reservaCancelada(Reserva reserva) {
        enviar(reserva, "Su reserva " + reserva.getCodigo().valor() + " fue CANCELADA. Motivo: "
                + reserva.getMotivoCancelacion());
    }

    private void enviar(Reserva reserva, String mensaje) {
        try {
            String linea = "Para " + reserva.getTitular().getCorreo() + ": " + mensaje;
            enviados.add(linea);
            LOG.info("[Notificación simulada] " + linea);
        } catch (RuntimeException fallo) {
            LOG.log(Level.WARNING, "No se pudo registrar la notificación; la operación continúa.", fallo);
        }
    }

    public List<String> getEnviados() {
        return List.copyOf(enviados);
    }
}
