package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.entity.Domo;
import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.repository.DomoRepository;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;
import co.edu.uniquindio.sga.domain.service.CotizadorEstanciaService;
import co.edu.uniquindio.sga.domain.valueobject.Cotizacion;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;

import java.time.LocalDate;
import java.util.List;

/** 7.1 Huésped: cotizar una estancia antes de reservar, con el desglose noche por noche (7.4). */
public class CotizarEstanciaUseCase {

    private final DomoRepository domoRepository;
    private final TemporadaRepository temporadaRepository;
    private final CotizadorEstanciaService cotizador;

    public CotizarEstanciaUseCase(DomoRepository domoRepository, TemporadaRepository temporadaRepository,
                                  CotizadorEstanciaService cotizador) {
        this.domoRepository = domoRepository;
        this.temporadaRepository = temporadaRepository;
        this.cotizador = cotizador;
    }

    public Cotizacion ejecutar(IdentificacionDomo idDomo, Estancia estancia, List<Ocupante> ocupantes, LocalDate fechaActual) {
        estancia.verificarQueNoEmpiezaAntesDe(fechaActual);              // RN-04
        Domo domo = Buscar.domo(domoRepository, idDomo);
        List<Temporada> temporadas = temporadaRepository.buscarTodas();
        domo.verificarQueAceptaReservas(temporadas);
        return cotizador.cotizar(domo, estancia, ocupantes, temporadas);
    }
}
