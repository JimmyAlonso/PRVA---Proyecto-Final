package co.edu.uniquindio.sga.infraestructure.persistence.inmemory;

import co.edu.uniquindio.sga.domain.repository.GeneradorCodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;

import java.time.Clock;
import java.time.Year;
import java.util.concurrent.atomic.AtomicInteger;

// Formato del negocio: RES-AAAA-NNNNN (ejemplo: RES-2026-00042)
public class GeneradorCodigoReservaInMemory implements GeneradorCodigoReserva {

    private final AtomicInteger consecutivo = new AtomicInteger();
    private final Clock reloj;

    public GeneradorCodigoReservaInMemory(Clock reloj) {
        this.reloj = reloj;
    }

    public GeneradorCodigoReservaInMemory() {
        this(Clock.systemDefaultZone());
    }

    @Override
    public CodigoReserva generar() {
        return new CodigoReserva("RES-%d-%05d".formatted(Year.now(reloj).getValue(), consecutivo.incrementAndGet()));
    }
}
