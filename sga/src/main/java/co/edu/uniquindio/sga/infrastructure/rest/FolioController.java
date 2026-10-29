package co.edu.uniquindio.sga.infrastructure.rest;

import co.edu.uniquindio.sga.application.dto.request.RegistrarPagoRequest;
import co.edu.uniquindio.sga.application.dto.request.RegistrarServicioRequest;
import co.edu.uniquindio.sga.application.dto.response.FolioResponse;
import co.edu.uniquindio.sga.application.usecase.GestionarFolioUseCase;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.FolioRestMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.Clock;
import java.time.LocalDate;

/**
 * 7.7 Folio, cargos y pagos. Los movimientos no se editan ni se borran (RN-16): no hay PUT ni
 * DELETE. El huésped consulta el folio de sus propias reservas; la recepción registra movimientos.
 */
@RestController
@RequestMapping("/api/v1/reservas/{codigo}/folio")
public class FolioController {

    private final GestionarFolioUseCase folioUseCase;
    private final FolioRestMapper mapper;
    private final Clock reloj;

    public FolioController(GestionarFolioUseCase folioUseCase, FolioRestMapper mapper, Clock reloj) {
        this.folioUseCase = folioUseCase;
        this.mapper = mapper;
        this.reloj = reloj;
    }

    @GetMapping
    @PreAuthorize("@accesoReservas.puedeAcceder(#codigo, authentication)")
    public ResponseEntity<FolioResponse> consultar(@PathVariable String codigo) {
        return ResponseEntity.ok(mapper.aFolio(folioUseCase.consultar(new CodigoReserva(codigo))));
    }

    /** RN-15: pago parcial con medio y fecha (anticipo, saldo, etc.). */
    @PostMapping("/pagos")
    public ResponseEntity<FolioResponse> registrarPago(@PathVariable String codigo,
                                                       @Valid @RequestBody RegistrarPagoRequest request) {
        LocalDate fecha = request.fecha() != null ? request.fecha() : LocalDate.now(reloj);
        Folio folio = folioUseCase.registrarPago(new CodigoReserva(codigo), Dinero.de(request.monto()),
                request.medio(), fecha);
        return creado(codigo, folio);
    }

    /** L-17: servicio adicional del catálogo del alojamiento. */
    @PostMapping("/servicios")
    public ResponseEntity<FolioResponse> registrarServicio(@PathVariable String codigo,
                                                           @Valid @RequestBody RegistrarServicioRequest request) {
        Folio folio = folioUseCase.registrarServicio(new CodigoReserva(codigo), request.codigoServicio(),
                request.cantidad(), LocalDate.now(reloj));
        return creado(codigo, folio);
    }

    /** L-22: depósito requerido para el check-in. */
    @PostMapping("/deposito")
    public ResponseEntity<FolioResponse> registrarDeposito(@PathVariable String codigo) {
        return creado(codigo, folioUseCase.registrarDeposito(new CodigoReserva(codigo), LocalDate.now(reloj)));
    }

    /** L-22: devolución del depósito como movimiento inverso (RN-16). */
    @PostMapping("/deposito/devolucion")
    public ResponseEntity<FolioResponse> devolverDeposito(@PathVariable String codigo) {
        return ResponseEntity.ok(mapper.aFolio(folioUseCase.devolverDeposito(new CodigoReserva(codigo),
                LocalDate.now(reloj))));
    }

    private ResponseEntity<FolioResponse> creado(String codigo, Folio folio) {
        return ResponseEntity.created(URI.create("/api/v1/reservas/" + codigo + "/folio")).body(mapper.aFolio(folio));
    }
}
