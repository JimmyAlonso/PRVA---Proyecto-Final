package co.edu.uniquindio.sga.infrastructure.persistence.jpa.adapter;

import co.edu.uniquindio.sga.domain.repository.GeneradorCodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.ReservaSpringDataRepository;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Year;

/**
 * Formato del negocio: RES-AAAA-NNNNN. El consecutivo continúa desde el último código guardado
 * en la base, así no se repite después de reiniciar la aplicación con H2 en modo archivo.
 */
@Component
public class GeneradorCodigoReservaJpaAdapter implements GeneradorCodigoReserva {

    private final ReservaSpringDataRepository repositorio;
    private final Clock reloj;

    public GeneradorCodigoReservaJpaAdapter(ReservaSpringDataRepository repositorio, Clock reloj) {
        this.repositorio = repositorio;
        this.reloj = reloj;
    }

    @Override
    public synchronized CodigoReserva generar() {
        String prefijo = "RES-%d-".formatted(Year.now(reloj).getValue());
        int ultimo = repositorio.ultimoCodigoConPrefijo(prefijo)
                .map(codigo -> Integer.parseInt(codigo.substring(prefijo.length())))
                .orElse(0);
        return new CodigoReserva(prefijo + "%05d".formatted(ultimo + 1));
    }
}
