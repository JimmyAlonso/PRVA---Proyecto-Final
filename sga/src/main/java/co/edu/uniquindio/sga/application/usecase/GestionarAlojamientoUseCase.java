package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;
import co.edu.uniquindio.sga.domain.valueobject.ServicioAdicional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 7.3 Datos del alojamiento: descripción, dirección, ubicación, normas y servicios adicionales.
 * Los parámetros de la sección 6 (umbral de edad, plazos, horarios) son configuración en
 * application.properties: se cambian sin recompilar y no se editan por la API.
 */
@Service
@Transactional
public class GestionarAlojamientoUseCase {

    private final AlojamientoRepository alojamientoRepository;

    public GestionarAlojamientoUseCase(AlojamientoRepository alojamientoRepository) {
        this.alojamientoRepository = alojamientoRepository;
    }

    public Alojamiento consultar() {
        return alojamientoRepository.obtener();
    }

    public Alojamiento actualizarDatos(String descripcion, String direccion, BigDecimal latitud, BigDecimal longitud,
                                       String normasConvivencia) {
        Alojamiento alojamiento = alojamientoRepository.obtener();
        alojamiento.actualizarDatos(descripcion, direccion, latitud, longitud, normasConvivencia);
        alojamientoRepository.guardar(alojamiento);
        return alojamiento;
    }

    public Alojamiento definirServicio(ServicioAdicional servicio) {
        Alojamiento alojamiento = alojamientoRepository.obtener();
        alojamiento.definirServicio(servicio);
        alojamientoRepository.guardar(alojamiento);
        return alojamiento;
    }
}
