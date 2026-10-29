package co.edu.uniquindio.sga.infrastructure.scheduling;

import co.edu.uniquindio.sga.application.usecase.VencerReservasPendientesUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.logging.Logger;

/**
 * RN-21 y 7.5: el vencimiento de reservas pendientes ocurre sin intervención manual.
 * La frecuencia se configura con sga.vencimiento.intervalo-ms.
 */
@Component
public class VencimientoReservasTarea {

    private static final Logger LOG = Logger.getLogger(VencimientoReservasTarea.class.getName());

    private final VencerReservasPendientesUseCase vencerReservasPendientes;
    private final Clock reloj;

    public VencimientoReservasTarea(VencerReservasPendientesUseCase vencerReservasPendientes, Clock reloj) {
        this.vencerReservasPendientes = vencerReservasPendientes;
        this.reloj = reloj;
    }

    @Scheduled(fixedDelayString = "${sga.vencimiento.intervalo-ms:60000}",
            initialDelayString = "${sga.vencimiento.intervalo-ms:60000}")
    public void vencerPendientes() {
        int vencidas = vencerReservasPendientes.ejecutar(LocalDateTime.now(reloj)).size();
        if (vencidas > 0) {
            LOG.info("RN-21: " + vencidas + " reserva(s) pendiente(s) cancelada(s) por vencimiento del plazo.");
        }
    }
}
