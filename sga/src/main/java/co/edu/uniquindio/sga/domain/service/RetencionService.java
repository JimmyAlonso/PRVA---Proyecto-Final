package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.HorarioAlojamiento;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * RN-13: la retención por cancelación y por no-show se determina con la versión de política
 * congelada en la reserva (RN-22), no con la vigente al momento del hecho.
 *
 * <p>Política JiyJu (Ficha A.4): ≥ 15 días → retiene 10 %; entre 14 y 5 días → 50 %;
 * menos de 5 días o no-show → 100 %. La antelación se mide contra la hora de entrada (15:00).
 */
public class RetencionService {

    private final HorarioAlojamiento horario;

    public RetencionService(HorarioAlojamiento horario) {
        if (horario == null) {
            throw new ReglaDominioException("El horario del alojamiento es obligatorio.");
        }
        this.horario = horario;
    }

    public long horasDeAntelacion(Reserva reserva, LocalDateTime momento) {
        LocalDateTime inicioEstancia = reserva.getEstancia().fechaEntrada().atTime(horario.horaEntrada());
        return Math.max(0, ChronoUnit.HOURS.between(momento, inicioEstancia));
    }

    public Dinero porCancelacion(Reserva reserva, PoliticaCancelacion politica, LocalDateTime momento) {
        verificarPoliticaCongelada(reserva, politica);
        return politica.retencionPara(horasDeAntelacion(reserva, momento))
                .aplicarA(reserva.getValorCongelado())
                .redondearAlPeso();
    }

    public Dinero porNoShow(Reserva reserva, PoliticaCancelacion politica) {
        verificarPoliticaCongelada(reserva, politica);
        return politica.retencionPorNoShow().aplicarA(reserva.getValorCongelado()).redondearAlPeso();
    }

    private void verificarPoliticaCongelada(Reserva reserva, PoliticaCancelacion politica) {
        if (politica == null || !politica.getVersion().equals(reserva.getPoliticaCongelada())) {
            throw new ReglaDominioException(
                    "La retención debe calcularse con la versión de política congelada en la reserva (RN-13).");
        }
    }
}
