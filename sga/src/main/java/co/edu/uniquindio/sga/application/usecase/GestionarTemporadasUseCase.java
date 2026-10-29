package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.entity.Domo;
import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.repository.DomoRepository;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;
import co.edu.uniquindio.sga.domain.service.CalendarioTemporadasService;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.IdTemporada;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;
import co.edu.uniquindio.sga.domain.valueobject.Tarifa;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 7.4 Temporadas: no pueden solaparse, existe una temporada base obligatoria y todo domo activo
 * debe tener tarifa en todas las temporadas. Por eso una temporada nueva se registra junto con
 * la tarifa de cada domo activo; si falta alguna, el dominio la rechaza (409).
 */
@Service
@Transactional
public class GestionarTemporadasUseCase {

    private final TemporadaRepository temporadaRepository;
    private final DomoRepository domoRepository;
    private final CalendarioTemporadasService calendarioService;

    public GestionarTemporadasUseCase(TemporadaRepository temporadaRepository, DomoRepository domoRepository,
                                      CalendarioTemporadasService calendarioService) {
        this.temporadaRepository = temporadaRepository;
        this.domoRepository = domoRepository;
        this.calendarioService = calendarioService;
    }

    @Transactional(readOnly = true)
    public List<Temporada> listar() {
        return temporadaRepository.buscarTodas();
    }

    @Transactional(readOnly = true)
    public Temporada consultar(IdTemporada id) {
        return Buscar.temporada(temporadaRepository, id);
    }

    public Temporada registrar(Temporada nueva, Map<IdentificacionDomo, Dinero> tarifasPorDomo, LocalDate fecha) {
        List<Temporada> existentes = temporadaRepository.buscarTodas();
        calendarioService.verificarRegistro(existentes, nueva);                       // 7.4: sin solapamientos

        List<Domo> domos = domoRepository.listarNoEliminados();
        Map<IdentificacionDomo, Dinero> tarifas = tarifasPorDomo == null ? Map.of() : tarifasPorDomo;
        for (IdentificacionDomo idDomo : tarifas.keySet()) {
            Buscar.domo(domoRepository, idDomo);                                        // 404 si el domo no existe
        }
        for (Domo domo : domos) {
            Dinero valor = tarifas.get(domo.getIdentificacion());
            if (valor != null) {
                domo.establecerTarifa(new Tarifa(nueva.getId(), valor), fecha);
            }
        }
        List<Temporada> calendario = new ArrayList<>(existentes);
        calendario.add(nueva);
        calendarioService.verificarTarifasCompletas(domos.stream().filter(Domo::estaActivo).toList(), calendario);

        temporadaRepository.guardar(nueva);
        domos.forEach(domoRepository::guardar);
        return nueva;
    }

    public Temporada redefinir(IdTemporada id, String nombre, List<Periodo> periodos) {
        Temporada temporada = Buscar.temporada(temporadaRepository, id);
        temporada.redefinir(nombre, periodos);
        calendarioService.verificarNuevaTemporada(temporadaRepository.buscarTodas(), temporada);
        temporadaRepository.guardar(temporada);
        return temporada;
    }
}
