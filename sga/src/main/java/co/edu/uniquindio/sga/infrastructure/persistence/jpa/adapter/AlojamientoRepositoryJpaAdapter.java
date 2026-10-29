package co.edu.uniquindio.sga.infrastructure.persistence.jpa.adapter;

import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.HorarioAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.ParametrosOperacion;
import co.edu.uniquindio.sga.domain.valueobject.ServicioAdicional;
import co.edu.uniquindio.sga.infrastructure.config.SgaProperties;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.AlojamientoPersistenceMapper;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.AlojamientoSpringDataRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * F-13: un único alojamiento. La primera vez que se consulta se crea con los datos de la Ficha
 * (application.properties); después, lo que cambie el administrador queda en la base.
 */
@Repository
public class AlojamientoRepositoryJpaAdapter implements AlojamientoRepository {

    private final AlojamientoSpringDataRepository repositorio;
    private final AlojamientoPersistenceMapper mapper;
    private final SgaProperties propiedades;
    private final HorarioAlojamiento horario;
    private final ParametrosOperacion parametros;

    public AlojamientoRepositoryJpaAdapter(AlojamientoSpringDataRepository repositorio,
                                           AlojamientoPersistenceMapper mapper, SgaProperties propiedades,
                                           HorarioAlojamiento horario, ParametrosOperacion parametros) {
        this.repositorio = repositorio;
        this.mapper = mapper;
        this.propiedades = propiedades;
        this.horario = horario;
        this.parametros = parametros;
    }

    @Override
    @Transactional
    public Alojamiento obtener() {
        return repositorio.findById(AlojamientoPersistenceMapper.ID_ALOJAMIENTO)
                .map(entidad -> mapper.aDominio(entidad, horario, parametros))
                .orElseGet(this::crearDesdeLaFicha);
    }

    @Override
    @Transactional
    public void guardar(Alojamiento alojamiento) {
        repositorio.save(mapper.aEntidad(alojamiento));
    }

    private Alojamiento crearDesdeLaFicha() {
        SgaProperties.DatosAlojamiento p = propiedades.alojamiento();
        Alojamiento alojamiento = new Alojamiento(p.nombre(), p.descripcion(), p.ciudad(), p.direccion(),
                p.latitud(), p.longitud(), horario, p.normasConvivencia(), parametros, p.aceptaMascotas());
        if (p.servicios() != null) {
            p.servicios().forEach(s -> alojamiento.agregarServicio(
                    new ServicioAdicional(s.codigo(), s.nombre(), Dinero.de(s.valor()), s.porPersona())));
        }
        guardar(alojamiento);
        return alojamiento;
    }
}
