package co.edu.uniquindio.sga.domain.port;

import co.edu.uniquindio.sga.domain.entity.Reserva;

/**
 * Puerto de salida de notificaciones (10.3.2 opción A, I-01): el dominio no conoce al
 * proveedor de correo. Ficha A.6: EmailNotificationAdapter detrás de este puerto.
 *
 * <p>I-05: el fallo de la notificación nunca impide reservar, cobrar ni registrar. Por eso
 * las implementaciones no lanzan excepciones hacia el caso de uso.
 */
public interface NotificacionService {

    void reservaCreada(Reserva reserva);

    void reservaConfirmada(Reserva reserva);

    void reservaCancelada(Reserva reserva);
}
