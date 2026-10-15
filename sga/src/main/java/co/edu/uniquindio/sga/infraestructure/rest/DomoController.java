package co.edu.uniquindio.sga.infraestructure.rest;

import co.edu.uniquindio.sga.application.dto.Pagina;
import co.edu.uniquindio.sga.application.dto.request.CambiarEstadoOperativoRequest;
import co.edu.uniquindio.sga.application.dto.request.CotizarEstanciaRequest;
import co.edu.uniquindio.sga.application.dto.request.RegistrarBloqueoRequest;
import co.edu.uniquindio.sga.application.dto.request.RegistrarNovedadRequest;
import co.edu.uniquindio.sga.application.dto.response.CotizacionDomoResponse;
import co.edu.uniquindio.sga.application.dto.response.DomoResponse;
import co.edu.uniquindio.sga.application.dto.response.OperacionResponses.BloqueoResponse;
import co.edu.uniquindio.sga.application.dto.response.OperacionResponses.NovedadResponse;
import co.edu.uniquindio.sga.application.usecase.BuscarDomosDisponiblesUseCase;
import co.edu.uniquindio.sga.application.usecase.BuscarDomosDisponiblesUseCase.Filtros;
import co.edu.uniquindio.sga.application.usecase.CotizarEstanciaUseCase;
import co.edu.uniquindio.sga.application.usecase.OperarDomoUseCase;
import co.edu.uniquindio.sga.domain.entity.Domo;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Cotizacion;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;
import co.edu.uniquindio.sga.infraestructure.rest.mapper.OperacionMapper;
import co.edu.uniquindio.sga.infraestructure.rest.mapper.ReservaMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Domos: listado, búsqueda con cotización (7.5), cotización (7.4) y operación diaria (7.6). */
@RestController
@RequestMapping("/api/v1/domos")
public class DomoController {

    private final OperarDomoUseCase operarDomoUseCase;
    private final BuscarDomosDisponiblesUseCase buscarDisponiblesUseCase;
    private final CotizarEstanciaUseCase cotizarEstanciaUseCase;
    private final OperacionMapper operacionMapper;
    private final ReservaMapper reservaMapper;
    private final Clock reloj;

    public DomoController(OperarDomoUseCase operarDomoUseCase, BuscarDomosDisponiblesUseCase buscarDisponiblesUseCase,
                          CotizarEstanciaUseCase cotizarEstanciaUseCase, OperacionMapper operacionMapper,
                          ReservaMapper reservaMapper, Clock reloj) {
        this.operarDomoUseCase = operarDomoUseCase;
        this.buscarDisponiblesUseCase = buscarDisponiblesUseCase;
        this.cotizarEstanciaUseCase = cotizarEstanciaUseCase;
        this.operacionMapper = operacionMapper;
        this.reservaMapper = reservaMapper;
        this.reloj = reloj;
    }

    @GetMapping
    public ResponseEntity<Pagina<DomoResponse>> listar(@RequestParam(defaultValue = "0") int pagina) {
        return ResponseEntity.ok(operarDomoUseCase.listar(pagina).mapear(operacionMapper::aDomo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DomoResponse> obtener(@PathVariable String id) {
        return ResponseEntity.ok(operacionMapper.aDomo(operarDomoUseCase.consultar(new IdentificacionDomo(id))));
    }

    /**
     * GET /api/v1/domos/disponibles?fechaEntrada=2026-11-20&fechaSalida=2026-11-22
     * &fechasNacimiento=1995-05-15,2016-03-02  (o &numeroOcupantes=2, todos adultos)
     * &capacidadMinima=&precioMaximo=&caracteristica=&conJacuzzi=&pagina=0
     */
    @GetMapping("/disponibles")
    public ResponseEntity<Pagina<CotizacionDomoResponse>> buscarDisponibles(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaEntrada,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaSalida,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) List<LocalDate> fechasNacimiento,
            @RequestParam(required = false) Integer numeroOcupantes,
            @RequestParam(required = false) Integer capacidadMinima,
            @RequestParam(required = false) Long precioMaximo,
            @RequestParam(required = false) String caracteristica,
            @RequestParam(required = false) Boolean conJacuzzi,
            @RequestParam(defaultValue = "0") int pagina) {
        List<LocalDate> grupo = componerGrupo(fechasNacimiento, numeroOcupantes);
        Filtros filtros = new Filtros(capacidadMinima, precioMaximo == null ? null : Dinero.de(precioMaximo),
                caracteristica, conJacuzzi);
        var resultado = buscarDisponiblesUseCase.ejecutar(new Estancia(fechaEntrada, fechaSalida),
                reservaMapper.aGrupo(grupo), filtros, LocalDate.now(reloj), pagina);
        return ResponseEntity.ok(resultado.mapear(d -> reservaMapper.aCotizacion(d.domo(), d.cotizacion())));
    }

    // POST /api/v1/domos/{id}/cotizacion  (7.4: desglose noche por noche)
    @PostMapping("/{id}/cotizacion")
    public ResponseEntity<CotizacionDomoResponse> cotizar(@PathVariable String id,
                                                          @Valid @RequestBody CotizarEstanciaRequest request) {
        IdentificacionDomo idDomo = new IdentificacionDomo(id);
        Cotizacion cotizacion = cotizarEstanciaUseCase.ejecutar(idDomo,
                new Estancia(request.fechaEntrada(), request.fechaSalida()),
                reservaMapper.aGrupo(request.fechasNacimiento()), LocalDate.now(reloj));
        Domo domo = operarDomoUseCase.consultar(idDomo);
        return ResponseEntity.ok(reservaMapper.aCotizacion(domo, cotizacion));
    }

    // ------------------------------------------------------------------ operación diaria (7.6)

    @GetMapping("/pendientes-preparacion")
    public ResponseEntity<List<DomoResponse>> pendientesDePreparacion() {
        return ResponseEntity.ok(operarDomoUseCase.pendientesDePreparacion().stream().map(operacionMapper::aDomo).toList());
    }

    @PostMapping("/{id}/estado-operativo")
    public ResponseEntity<DomoResponse> cambiarEstado(@PathVariable String id,
                                                      @Valid @RequestBody CambiarEstadoOperativoRequest request) {
        return ResponseEntity.ok(operacionMapper.aDomo(
                operarDomoUseCase.cambiarEstado(new IdentificacionDomo(id), request.accion())));
    }

    @PostMapping("/{id}/novedades")
    public ResponseEntity<NovedadResponse> registrarNovedad(@PathVariable String id,
                                                            @Valid @RequestBody RegistrarNovedadRequest request) {
        var novedad = operarDomoUseCase.registrarNovedad(new IdentificacionDomo(id), request.tipo(),
                request.descripcion(), request.gravedad(), request.autor(), LocalDateTime.now(reloj));
        return ResponseEntity.status(HttpStatus.CREATED).body(operacionMapper.aNovedad(novedad));
    }

    @GetMapping("/{id}/novedades")
    public ResponseEntity<Pagina<NovedadResponse>> historialNovedades(@PathVariable String id,
                                                                      @RequestParam(defaultValue = "0") int pagina) {
        return ResponseEntity.ok(operarDomoUseCase.historialNovedades(new IdentificacionDomo(id), pagina)
                .mapear(operacionMapper::aNovedad));
    }

    @PostMapping("/{id}/bloqueos")
    public ResponseEntity<BloqueoResponse> registrarBloqueo(@PathVariable String id,
                                                            @Valid @RequestBody RegistrarBloqueoRequest request) {
        var bloqueo = operarDomoUseCase.registrarBloqueo(new IdentificacionDomo(id), request.desde(), request.hasta(),
                request.motivo(), request.observacion());
        return ResponseEntity.status(HttpStatus.CREATED).body(operacionMapper.aBloqueo(bloqueo));
    }

    @GetMapping("/{id}/bloqueos")
    public ResponseEntity<List<BloqueoResponse>> bloqueos(@PathVariable String id) {
        return ResponseEntity.ok(operarDomoUseCase.bloqueos(new IdentificacionDomo(id)).stream()
                .map(operacionMapper::aBloqueo).toList());
    }

    private List<LocalDate> componerGrupo(List<LocalDate> fechasNacimiento, Integer numeroOcupantes) {
        if (fechasNacimiento != null && !fechasNacimiento.isEmpty()) {
            return fechasNacimiento;
        }
        if (numeroOcupantes == null || numeroOcupantes < 1) {
            throw new ReglaDominioException("Indique las fechas de nacimiento del grupo o el número de ocupantes.");
        }
        // Sin fechas de nacimiento se asume que todos son adultos (facturables)
        return new ArrayList<>(Collections.nCopies(numeroOcupantes, LocalDate.now(reloj).minusYears(30)));
    }
}
