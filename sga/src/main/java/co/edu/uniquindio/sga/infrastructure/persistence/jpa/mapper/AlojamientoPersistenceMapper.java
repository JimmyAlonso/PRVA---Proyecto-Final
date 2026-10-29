package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.HorarioAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.ParametrosOperacion;
import co.edu.uniquindio.sga.domain.valueobject.ServicioAdicional;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.ServicioAdicionalEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.AlojamientoEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * El alojamiento se reconstruye con los datos guardados más el horario y los parámetros de
 * operación, que son configuración (sección 6) y llegan desde application.properties.
 */
@Component
public class AlojamientoPersistenceMapper {

    public static final String ID_ALOJAMIENTO = "ALOJAMIENTO";

    public AlojamientoEntity aEntidad(Alojamiento alojamiento) {
        AlojamientoEntity entidad = new AlojamientoEntity();
        entidad.setId(ID_ALOJAMIENTO);
        entidad.setNombre(alojamiento.getNombre());
        entidad.setDescripcion(alojamiento.getDescripcion());
        entidad.setCiudad(alojamiento.getCiudad());
        entidad.setDireccion(alojamiento.getDireccion());
        entidad.setLatitud(alojamiento.getLatitud());
        entidad.setLongitud(alojamiento.getLongitud());
        entidad.setNormasConvivencia(alojamiento.getNormasConvivencia());
        entidad.setAceptaMascotas(alojamiento.isAceptaMascotas());
        entidad.setServicios(new ArrayList<>(alojamiento.getServicios().stream()
                .map(s -> new ServicioAdicionalEmbeddable(s.codigo(), s.nombre(), s.valor().valor(), s.porPersona()))
                .toList()));
        return entidad;
    }

    public Alojamiento aDominio(AlojamientoEntity entidad, HorarioAlojamiento horario, ParametrosOperacion parametros) {
        Alojamiento alojamiento = new Alojamiento(entidad.getNombre(), entidad.getDescripcion(), entidad.getCiudad(),
                entidad.getDireccion(), entidad.getLatitud(), entidad.getLongitud(), horario,
                entidad.getNormasConvivencia(), parametros, entidad.isAceptaMascotas());
        entidad.getServicios().forEach(s -> alojamiento.agregarServicio(new ServicioAdicional(s.getCodigo(), s.getNombre(),
                new Dinero(s.getValor()), Boolean.TRUE.equals(s.getPorPersona()))));
        return alojamiento;
    }
}
