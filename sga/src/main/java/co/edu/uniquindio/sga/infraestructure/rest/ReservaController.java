package co.edu.uniquindio.sga.infraestructure.rest;

import co.edu.uniquindio.sga.application.dto.Pagina;
import co.edu.uniquindio.sga.application.dto.request.CancelarReservaRequest;
import co.edu.uniquindio.sga.application.dto.request.ConfirmarReservaRequest;
import co.edu.uniquindio.sga.application.dto.request.CrearReservaRequest;
import co.edu.uniquindio.sga.application.dto.request.ModificarReservaRequest;
import co.edu.uniquindio.sga.application.dto.request.RegistrarSalidaRequest;
import co.edu.uniquindio.sga.application.dto.response.OperacionResponses.MovimientosDelDiaResponse;
import co.edu.uniquindio.sga.application.dto.response.ReservaDetalleResponse;
import co.edu.uniquindio.sga.application.dto.response.ReservaResumenResponse;
import co.edu.uniquindio.sga.application.usecase.CancelarReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.ConfirmarReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarMovimientosDelDiaUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarReservasUseCase;
import co.edu.uniquindio.sga.application.usecase.CrearReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.DeclararNoShowUseCase;
import co.edu.uniquindio.sga.application.usecase.ModificarReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.RegistrarLlegadaUseCase;
import co.edu.uniquindio.sga.application.usecase.RegistrarSalidaUseCase;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.CriteriosBusquedaReserva;
import co.edu.uniquindio.sga.domain.valueobject.AutorizacionCierre;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;
import co.edu.uniquindio.sga.infraestructure.rest.mapper.ReservaMapper;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Reservas: creación, consulta, modificación y ciclo de vida (secciones 7.5, 7.6 y 8).
 * Las operaciones de negocio no reciben el estado final: lo decide el agregado Reserva.
 */
@RestController
@RequestMapping("/api/v1/reservas")
public class ReservaController {

    private final CrearReservaUseCase crearReservaUseCase;
    private final ConsultarReservaUseCase consultarReservaUseCase;
    private final ConsultarReservasUseCase consultarReservasUseCase;
    private final ModificarReservaUseCase modificarReservaUseCase;
    private final ConfirmarReservaUseCase confirmarReservaUseCase;
    private final CancelarReservaUseCase cancelarReservaUseCase;
    private final RegistrarLlegadaUseCase registrarLlegadaUseCase;
    private final RegistrarSalidaUseCase registrarSalidaUseCase;
    private final DeclararNoShowUseCase declararNoShowUseCase;
    private final ConsultarMovimientosDelDiaUseCase movimientosDelDiaUseCase;
    private final ReservaMapper mapper;
    private final Clock reloj;

    public ReservaController(CrearReservaUseCase crearReservaUseCase,
                             ConsultarReservaUseCase consultarReservaUseCase,
                             ConsultarReservasUseCase consultarReservasUseCase,
                             ModificarReservaUseCase modificarReservaUseCase,
                             ConfirmarReservaUseCase confirmarReservaUseCase,
                             CancelarReservaUseCase cancelarReservaUseCase,
                             RegistrarLlegadaUseCase registrarLlegadaUseCase,
                             RegistrarSalidaUseCase registrarSalidaUseCase,
                             DeclararNoShowUseCase declararNoShowUseCase,
                             ConsultarMovimientosDelDiaUseCase movimientosDelDiaUseCase,
                             ReservaMapper mapper, Clock reloj) {
        this.crearReservaUseCase = crearReservaUseCase;
        this.consultarReservaUseCase = consultarReservaUseCase;
        this.consultarReservasUseCase = consultarReservasUseCase;
        this.modificarReservaUseCase = modificarReservaUseCase;
        this.confirmarReservaUseCase = confirmarReservaUseCase;
        this.cancelarReservaUseCase = cancelarReservaUseCase;
        this.registrarLlegadaUseCase = registrarLlegadaUseCase;
        this.registrarSalidaUseCase = registrarSalidaUseCase;
        this.declararNoShowUseCase = declararNoShowUseCase;
        this.movimientosDelDiaUseCase = movimientosDelDiaUseCase;
        this.mapper = mapper;
        this.reloj = reloj;
    }

    // ------------------------------------------------------------------ consultas

    // GET /api/v1/reservas?estado=&idDomo=&canal=&documentoTitular=&desde=&hasta=&pagina=0
    @GetMapping
    public ResponseEntity<Pagina<ReservaResumenResponse>> listar(
            @RequestParam(required = false) EstadoReserva estado,
            @RequestParam(required = false) String idDomo,
            @RequestParam(required = false) CanalOrigen canal,
            @RequestParam(required = false) String documentoTitular,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") int pagina) {
        CriteriosBusquedaReserva criterios = new CriteriosBusquedaReserva(estado,
                idDomo == null ? null : new IdentificacionDomo(idDomo), canal,
                documentoTitular == null ? null : new DocumentoIdentidad(documentoTitular), desde, hasta);
        return ResponseEntity.ok(consultarReservasUseCase.ejecutar(criterios, pagina).mapear(mapper::aResumen));
    }

    // GET /api/v1/reservas/{codigo}
    @GetMapping("/{codigo}")
    public ResponseEntity<ReservaDetalleResponse> obtener(@PathVariable String codigo) {
        return ResponseEntity.ok(mapper.aDetalle(consultarReservaUseCase.ejecutar(new CodigoReserva(codigo))));
    }

    // GET /api/v1/reservas/movimientos?fecha=2026-10-20  (llegadas y salidas previstas, 7.6)
    @GetMapping("/movimientos")
    public ResponseEntity<MovimientosDelDiaResponse> movimientos(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        LocalDate dia = fecha != null ? fecha : LocalDate.now(reloj);
        var movimientos = movimientosDelDiaUseCase.ejecutar(dia);
        return ResponseEntity.ok(new MovimientosDelDiaResponse(dia, mapper.aResumenes(movimientos.llegadas()),
                mapper.aResumenes(movimientos.salidas())));
    }

    // ------------------------------------------------------------------ creación y modificación

    // POST /api/v1/reservas
    @PostMapping
    public ResponseEntity<ReservaDetalleResponse> crear(@Valid @RequestBody CrearReservaRequest request) {
        // 1. DTO → dominio   2. caso de uso   3. dominio → DTO
        Reserva reserva = crearReservaUseCase.ejecutar(
                new IdentificacionDomo(request.idDomo()),
                new Estancia(request.fechaEntrada(), request.fechaSalida()),
                mapper.aHoraEstimadaLlegada(request.horaEstimadaLlegada()),
                mapper.aTitular(request.titular()),
                mapper.aOcupantes(request.titular(), request.ocupantes()),
                request.canalOrigen(),
                LocalDateTime.now(reloj));
        URI location = URI.create("/api/v1/reservas/" + reserva.getCodigo().valor());
        return ResponseEntity.created(location).body(mapper.aDetalle(reserva));
    }

    // PUT /api/v1/reservas/{codigo}  (RN-14)
    @PutMapping("/{codigo}")
    public ResponseEntity<ReservaDetalleResponse> modificar(@PathVariable String codigo,
                                                            @Valid @RequestBody ModificarReservaRequest request) {
        CodigoReserva cod = new CodigoReserva(codigo);
        Reserva actual = consultarReservaUseCase.ejecutar(cod);
        Estancia nuevaEstancia = null;
        if (request.fechaEntrada() != null || request.fechaSalida() != null) {
            nuevaEstancia = new Estancia(
                    request.fechaEntrada() != null ? request.fechaEntrada() : actual.getEstancia().fechaEntrada(),
                    request.fechaSalida() != null ? request.fechaSalida() : actual.getEstancia().fechaSalida());
        }
        Reserva reserva = modificarReservaUseCase.ejecutar(cod,
                request.idDomo() == null ? null : new IdentificacionDomo(request.idDomo()),
                nuevaEstancia,
                mapper.aOcupantesModificados(actual, request.ocupantes()),
                LocalDateTime.now(reloj));
        return ResponseEntity.ok(mapper.aDetalle(reserva));
    }

    // ------------------------------------------------------------------ ciclo de vida (sección 8)

    // POST /api/v1/reservas/{codigo}/confirmar
    @PostMapping("/{codigo}/confirmar")
    public ResponseEntity<ReservaDetalleResponse> confirmar(@PathVariable String codigo,
                                                            @RequestBody(required = false) ConfirmarReservaRequest request) {
        Reserva reserva = confirmarReservaUseCase.ejecutar(new CodigoReserva(codigo),
                request == null ? null : mapper.aHoraEstimadaLlegada(request.horaEstimadaLlegada()));
        return ResponseEntity.ok(mapper.aDetalle(reserva));
    }

    // POST /api/v1/reservas/{codigo}/cancelar
    @PostMapping("/{codigo}/cancelar")
    public ResponseEntity<ReservaDetalleResponse> cancelar(@PathVariable String codigo,
                                                           @Valid @RequestBody CancelarReservaRequest request) {
        Reserva reserva = cancelarReservaUseCase.ejecutar(new CodigoReserva(codigo), request.motivo(),
                LocalDateTime.now(reloj));
        return ResponseEntity.ok(mapper.aDetalle(reserva));
    }

    // POST /api/v1/reservas/{codigo}/check-in
    @PostMapping("/{codigo}/check-in")
    public ResponseEntity<ReservaDetalleResponse> registrarLlegada(@PathVariable String codigo) {
        Reserva reserva = registrarLlegadaUseCase.ejecutar(new CodigoReserva(codigo), LocalDate.now(reloj));
        return ResponseEntity.ok(mapper.aDetalle(reserva));
    }

    // POST /api/v1/reservas/{codigo}/check-out
    @PostMapping("/{codigo}/check-out")
    public ResponseEntity<ReservaDetalleResponse> registrarSalida(@PathVariable String codigo,
                                                                  @RequestBody(required = false) RegistrarSalidaRequest request) {
        AutorizacionCierre autorizacion = null;
        if (request != null && request.autorizadoPor() != null && !request.autorizadoPor().isBlank()) {
            autorizacion = new AutorizacionCierre(request.autorizadoPor(), request.motivoAutorizacion());
        }
        Reserva reserva = registrarSalidaUseCase.ejecutar(new CodigoReserva(codigo), autorizacion);
        return ResponseEntity.ok(mapper.aDetalle(reserva));
    }

    // POST /api/v1/reservas/{codigo}/no-show
    @PostMapping("/{codigo}/no-show")
    public ResponseEntity<ReservaDetalleResponse> declararNoShow(@PathVariable String codigo) {
        Reserva reserva = declararNoShowUseCase.ejecutar(new CodigoReserva(codigo), LocalDateTime.now(reloj));
        return ResponseEntity.ok(mapper.aDetalle(reserva));
    }
}
