package co.edu.uniquindio.sga.infrastructure.rest;

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
import co.edu.uniquindio.sga.infrastructure.rest.mapper.ReservaRestMapper;
import co.edu.uniquindio.sga.infrastructure.security.AccesoReservas;
import co.edu.uniquindio.sga.infrastructure.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
 * Reservas: creación, consulta, modificación y ciclo de vida (7.5 y sección 8). Controller
 * delgado: valida la forma con @Valid, traduce con MapStruct y delega en el caso de uso. Las
 * operaciones de negocio no reciben el estado final: lo decide el agregado Reserva.
 *
 * <p>Seguridad: el rol se exige en SeguridadConfig; el huésped solo accede a sus propias
 * reservas, verificado por propietario con {@code @accesoReservas}.
 */
@RestController
@RequestMapping("/api/v1/reservas")
public class ReservaController {

    private static final String PROPIETARIO = "@accesoReservas.puedeAcceder(#codigo, authentication)";

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
    private final AccesoReservas accesoReservas;
    private final ReservaRestMapper mapper;
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
                             AccesoReservas accesoReservas, ReservaRestMapper mapper, Clock reloj) {
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
        this.accesoReservas = accesoReservas;
        this.mapper = mapper;
        this.reloj = reloj;
    }

    // ------------------------------------------------------------------ consultas

    /** 7.5 Listados (recepción): filtros por fechas, estado, domo, canal y titular; 10 por página. */
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

    /** 7.1 Huésped: su historial de reservas y el estado de cada una. */
    @GetMapping("/mias")
    public ResponseEntity<Pagina<ReservaResumenResponse>> misReservas(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                                                      @RequestParam(defaultValue = "0") int pagina) {
        return ResponseEntity.ok(consultarReservasUseCase
                .deTitular(new DocumentoIdentidad(usuario.documento()), pagina).mapear(mapper::aResumen));
    }

    /** 7.6: llegadas y salidas previstas para un día. */
    @GetMapping("/movimientos")
    public ResponseEntity<MovimientosDelDiaResponse> movimientos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        LocalDate dia = fecha != null ? fecha : LocalDate.now(reloj);
        var movimientos = movimientosDelDiaUseCase.ejecutar(dia);
        return ResponseEntity.ok(new MovimientosDelDiaResponse(dia, mapper.aResumenes(movimientos.llegadas()),
                mapper.aResumenes(movimientos.salidas())));
    }

    @GetMapping("/{codigo}")
    @PreAuthorize(PROPIETARIO)
    public ResponseEntity<ReservaDetalleResponse> obtener(@PathVariable String codigo) {
        return ResponseEntity.ok(mapper.aDetalle(consultarReservaUseCase.ejecutar(new CodigoReserva(codigo))));
    }

    // ------------------------------------------------------------------ creación y modificación

    /** 7.5: la reserva nace PENDIENTE, con su valor y política congelados y su folio abierto. 201 + Location. */
    @PostMapping
    public ResponseEntity<ReservaDetalleResponse> crear(@Valid @RequestBody CrearReservaRequest request,
                                                        @AuthenticationPrincipal UsuarioAutenticado usuario) {
        accesoReservas.verificarCreacion(request.canalOrigen(), request.titular().documento(), usuario);
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

    /** RN-14: cambia fechas, ocupantes o domo de una reserva no iniciada y registra el ajuste en el folio. */
    @PutMapping("/{codigo}")
    public ResponseEntity<ReservaDetalleResponse> modificar(@PathVariable String codigo,
                                                            @Valid @RequestBody ModificarReservaRequest request) {
        Reserva reserva = modificarReservaUseCase.ejecutar(new CodigoReserva(codigo),
                request.idDomo() == null ? null : new IdentificacionDomo(request.idDomo()),
                request.fechaEntrada(), request.fechaSalida(),
                request.ocupantes() == null ? null : request.ocupantes().stream().map(mapper::aOcupante).toList(),
                LocalDateTime.now(reloj));
        return ResponseEntity.ok(mapper.aDetalle(reserva));
    }

    // ------------------------------------------------------------------ ciclo de vida (sección 8)

    @PostMapping("/{codigo}/confirmar")
    public ResponseEntity<ReservaDetalleResponse> confirmar(@PathVariable String codigo,
                                                            @RequestBody(required = false) ConfirmarReservaRequest request) {
        Reserva reserva = confirmarReservaUseCase.ejecutar(new CodigoReserva(codigo),
                request == null ? null : mapper.aHoraEstimadaLlegada(request.horaEstimadaLlegada()));
        return ResponseEntity.ok(mapper.aDetalle(reserva));
    }

    /** 7.1: el huésped cancela su propia reserva; la recepción, cualquiera. Política congelada (RN-13). */
    @PostMapping("/{codigo}/cancelar")
    @PreAuthorize(PROPIETARIO)
    public ResponseEntity<ReservaDetalleResponse> cancelar(@PathVariable String codigo,
                                                           @Valid @RequestBody CancelarReservaRequest request) {
        Reserva reserva = cancelarReservaUseCase.ejecutar(new CodigoReserva(codigo), request.motivo(),
                LocalDateTime.now(reloj));
        return ResponseEntity.ok(mapper.aDetalle(reserva));
    }

    @PostMapping("/{codigo}/check-in")
    public ResponseEntity<ReservaDetalleResponse> registrarLlegada(@PathVariable String codigo) {
        Reserva reserva = registrarLlegadaUseCase.ejecutar(new CodigoReserva(codigo), LocalDate.now(reloj));
        return ResponseEntity.ok(mapper.aDetalle(reserva));
    }

    /**
     * RN-17: si el folio tiene saldo distinto de cero, la salida requiere la autorización de un
     * administrador; el autor registrado es el administrador autenticado.
     */
    @PostMapping("/{codigo}/check-out")
    public ResponseEntity<ReservaDetalleResponse> registrarSalida(@PathVariable String codigo,
                                                                  @Valid @RequestBody(required = false) RegistrarSalidaRequest request,
                                                                  @AuthenticationPrincipal UsuarioAutenticado usuario) {
        AutorizacionCierre autorizacion = null;
        if (request != null && request.motivoAutorizacion() != null && !request.motivoAutorizacion().isBlank()) {
            if (!usuario.esAdministrador()) {
                throw new AccessDeniedException("Solo un administrador autoriza el cierre del folio con saldo pendiente.");
            }
            autorizacion = new AutorizacionCierre(usuario.correo(), request.motivoAutorizacion());
        }
        Reserva reserva = registrarSalidaUseCase.ejecutar(new CodigoReserva(codigo), autorizacion);
        return ResponseEntity.ok(mapper.aDetalle(reserva));
    }

    @PostMapping("/{codigo}/no-show")
    public ResponseEntity<ReservaDetalleResponse> declararNoShow(@PathVariable String codigo) {
        Reserva reserva = declararNoShowUseCase.ejecutar(new CodigoReserva(codigo), LocalDateTime.now(reloj));
        return ResponseEntity.ok(mapper.aDetalle(reserva));
    }
}
