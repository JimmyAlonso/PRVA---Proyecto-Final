package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.dto.Pagina;
import co.edu.uniquindio.sga.domain.entity.Domo;
import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.repository.DomoRepository;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;
import co.edu.uniquindio.sga.domain.service.CotizadorEstanciaService;
import co.edu.uniquindio.sga.domain.service.DisponibilidadDomoService;
import co.edu.uniquindio.sga.domain.valueobject.Cotizacion;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 7.5 Búsqueda: domos disponibles para un rango de fechas y una composición de grupo, con
 * filtros de capacidad, precio y características. Cada resultado trae su valor estimado.
 * También responde la consulta de disponibilidad del canal externo (10.3.1, operación 1).
 */
@Service
@Transactional(readOnly = true)
public class BuscarDomosDisponiblesUseCase {

    public record Filtros(Integer capacidadMinima, Dinero precioMaximo, String caracteristica, Boolean conJacuzzi) {
        public static Filtros ninguno() {
            return new Filtros(null, null, null, null);
        }
    }

    public record DomoDisponible(Domo domo, Cotizacion cotizacion) {
    }

    private final DomoRepository domoRepository;
    private final TemporadaRepository temporadaRepository;
    private final DisponibilidadDomoService disponibilidadService;
    private final CotizadorEstanciaService cotizador;

    public BuscarDomosDisponiblesUseCase(DomoRepository domoRepository, TemporadaRepository temporadaRepository,
                                         DisponibilidadDomoService disponibilidadService,
                                         CotizadorEstanciaService cotizador) {
        this.domoRepository = domoRepository;
        this.temporadaRepository = temporadaRepository;
        this.disponibilidadService = disponibilidadService;
        this.cotizador = cotizador;
    }

    public Pagina<DomoDisponible> ejecutar(Estancia estancia, List<Ocupante> grupo, Filtros filtros,
                                           LocalDate fechaActual, int pagina) {
        estancia.verificarQueNoEmpiezaAntesDe(fechaActual);              // RN-04
        Filtros f = filtros == null ? Filtros.ninguno() : filtros;
        List<Temporada> temporadas = temporadaRepository.buscarTodas();

        List<DomoDisponible> resultado = new ArrayList<>();
        for (Domo domo : domoRepository.listarActivos()) {               // 7.3: ignora eliminados
            if (!domo.tieneTarifasCompletas(temporadas)) continue;
            if (f.capacidadMinima() != null && domo.getCapacidad() < f.capacidadMinima()) continue;
            if (f.conJacuzzi() != null && domo.tieneJacuzzi() != f.conJacuzzi()) continue;
            if (f.caracteristica() != null && !f.caracteristica().isBlank() && domo.getCaracteristicas().stream()
                    .noneMatch(c -> c.toLowerCase().contains(f.caracteristica().toLowerCase()))) continue;
            if (!disponibilidadService.estaDisponible(domo, estancia, grupo.size())) continue;

            Cotizacion cotizacion = cotizador.cotizar(domo, estancia, grupo, temporadas);
            if (f.precioMaximo() != null && cotizacion.total().esMayorQue(f.precioMaximo())) continue;
            resultado.add(new DomoDisponible(domo, cotizacion));
        }
        resultado.sort(Comparator.comparing(d -> d.cotizacion().total().valor()));
        return Pagina.de(resultado, pagina);
    }
}
