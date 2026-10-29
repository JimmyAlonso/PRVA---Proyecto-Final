package co.edu.uniquindio.sga.infrastructure.persistence.inmemory;

import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;

/** Adaptador en memoria (I-02): guarda el único alojamiento (F-13). */
public class AlojamientoRepositoryInMemory implements AlojamientoRepository {

    private Alojamiento alojamiento;

    public AlojamientoRepositoryInMemory(Alojamiento inicial) {
        this.alojamiento = inicial;
    }

    @Override
    public Alojamiento obtener() {
        return alojamiento;
    }

    @Override
    public void guardar(Alojamiento alojamiento) {
        this.alojamiento = alojamiento;
    }
}
