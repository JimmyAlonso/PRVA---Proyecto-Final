package co.edu.uniquindio.sga.domain.port;

import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.valueobject.Correo;

/**
 * Puerto de salida de notificaciones (10.3.2 opción A). I-05: el fallo de una notificación
 * nunca detiene la operación de negocio; el adaptador captura y registra el error.
 */
public interface NotificacionService {

    void reservaCreada(Reserva reserva);

    void reservaConfirmada(Reserva reserva);

    void reservaCancelada(Reserva reserva);

    /** 7.2: envío del código de recuperación de contraseña al correo registrado. */
    void codigoRecuperacion(Correo destinatario, String codigo, int minutosVigencia);
}
