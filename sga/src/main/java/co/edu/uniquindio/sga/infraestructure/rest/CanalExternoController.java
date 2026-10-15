package co.edu.uniquindio.sga.infraestructure.rest;

import co.edu.uniquindio.sga.application.dto.Pagina;
import co.edu.uniquindio.sga.application.dto.request.CancelarReservaRequest;
import co.edu.uniquindio.sga.application.dto.request.ReservaExternaRequest;
import co.edu.uniquindio.sga.application.dto.response.CotizacionDomoResponse;
import co.edu.uniquindio.sga.application.dto.response.OperacionResponses.NovedadResponse;
import co.edu.uniquindio.sga.application.dto.response.OperacionResponses.ResultadoReservaExternaResponse;
import co.edu.uniquindio.sga.application.dto.response.ReservaDetalleResponse;
import co.edu.uniquindio.sga.application.usecase.BuscarDomosDisponiblesUseCase;
import co.edu.uniquindio.sga.application.usecase.CanalExternoUseCase;
import co.edu.uniquindio.sga.application.usecase.OperarDomoUseCase;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.service.ResultadoConciliacion;
import co.edu.uniquindio.sga.domain.valueobject.DatosReservaExterna;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;
import co.edu.uniquindio.sga.domain.valueobject.IdentificadorExterno;
import co.edu.uniquindio.sga.infraestructure.rest.mapper.OperacionMapper;
import co.edu.uniquindio.sga.infraestructure.rest.mapper.ReservaMapper;
import co.edu.uniquindio.sga.infraestructure.security.AutenticadorCanales;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 10.3.1 Canal externo, lado servidor: tres operaciones autenticadas con una credencial por
 * canal (cabeceras X-Canal-Id y X-Canal-Credencial).
 */
@RestController
@RequestMapping("/api/v1/canales")
public class CanalExternoController {

    private final CanalExternoUseCase canalExternoUseCase;
    private final BuscarDomosDisponiblesUseCase buscarDisponiblesUseCase;
    private final OperarDomoUseCase operarDomoUseCase;
    private final AutenticadorCanales autenticador;
    private final ReservaMapper reservaMapper;
    private final OperacionMapper operacionMapper;
    private final Clock reloj;

    public CanalExternoController(CanalExternoUseCase canalExternoUseCase,
                                  BuscarDomosDisponiblesUseCase buscarDisponiblesUseCase,
                                  OperarDomoUseCase operarDomoUseCase, AutenticadorCanales autenticador,
                                  ReservaMapper reservaMapper, OperacionMapper operacionMapper, Clock reloj) {
        this.canalExternoUseCase = canalExternoUseCase;
        this.buscarDisponiblesUseCase = buscarDisponiblesUseCase;
        this.operarDomoUseCase = operarDomoUseCase;
        this.autenticador = autenticador;
        this.reservaMapper = reservaMapper;
        this.operacionMapper = operacionMapper;
        this.reloj = reloj;
    }

    // 1. Consultar disponibilidad para un rango de fechas y una composición de grupo
    @GetMapping("/externo/disponibilidad")
    public ResponseEntity<Pagina<CotizacionDomoResponse>> disponibilidad(
            @RequestHeader("X-Canal-Id") String canal,
            @RequestHeader("X-Canal-Credencial") String credencial,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaEntrada,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaSalida,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) List<LocalDate> fechasNacimiento,
            @RequestParam(defaultValue = "0") int pagina) {
        autenticador.verificar(canal, credencial);
        var resultado = buscarDisponiblesUseCase.ejecutar(new Estancia(fechaEntrada, fechaSalida),
                reservaMapper.aGrupo(fechasNacimiento), BuscarDomosDisponiblesUseCase.Filtros.ninguno(),
                LocalDate.now(reloj), pagina);
        return ResponseEntity.ok(resultado.mapear(d -> reservaMapper.aCotizacion(d.domo(), d.cotizacion())));
    }

    // 2. Crear reserva externa: 201 creada, 200 mensaje repetido (RN-19), 409 conflicto (RN-18)
    @PostMapping("/externo/reservas")
    public ResponseEntity<ResultadoReservaExternaResponse> crearReserva(
            @RequestHeader("X-Canal-Id") String canal,
            @RequestHeader("X-Canal-Credencial") String credencial,
            @Valid @RequestBody ReservaExternaRequest request) {
        autenticador.verificar(canal, credencial);
        DatosReservaExterna datos = new DatosReservaExterna(
                new IdentificadorExterno(canal, request.idTransaccionExterna()),
                new IdentificacionDomo(request.idDomo()),
                new Estancia(request.fechaEntrada(), request.fechaSalida()),
                reservaMapper.aTitular(request.titular()),
                reservaMapper.aOcupantes(request.titular(), request.ocupantes()),
                reservaMapper.aHoraEstimadaLlegada(request.horaEstimadaLlegada()));

        ResultadoConciliacion resultado = canalExternoUseCase.recibirReserva(datos, LocalDateTime.now(reloj));
        return switch (resultado.tipo()) {
            case CREADA -> ResponseEntity.status(HttpStatus.CREATED).body(respuesta(resultado, "Reserva externa creada."));
            case DUPLICADA -> ResponseEntity.ok(respuesta(resultado, resultado.motivo()));
            case CONFLICTO -> ResponseEntity.status(HttpStatus.CONFLICT).body(new ResultadoReservaExternaResponse(
                    "CONFLICTO", null, null, 0, "Conflicto de canal registrado: " + resultado.motivo()));
        };
    }

    // 3. Cancelar una reserva externa previamente creada
    @PostMapping("/externo/reservas/{idTransaccionExterna}/cancelar")
    public ResponseEntity<ReservaDetalleResponse> cancelarReserva(
            @RequestHeader("X-Canal-Id") String canal,
            @RequestHeader("X-Canal-Credencial") String credencial,
            @PathVariable String idTransaccionExterna,
            @Valid @RequestBody CancelarReservaRequest request) {
        autenticador.verificar(canal, credencial);
        Reserva reserva = canalExternoUseCase.cancelarReserva(new IdentificadorExterno(canal, idTransaccionExterna),
                request.motivo(), LocalDateTime.now(reloj));
        return ResponseEntity.ok(reservaMapper.aDetalle(reserva));
    }

    // 7.1 Administrador: revisar los conflictos de canal registrados
    @GetMapping("/conflictos")
    public ResponseEntity<List<NovedadResponse>> conflictos() {
        return ResponseEntity.ok(operarDomoUseCase.conflictosDeCanal().stream().map(operacionMapper::aNovedad).toList());
    }

    private ResultadoReservaExternaResponse respuesta(ResultadoConciliacion resultado, String mensaje) {
        Reserva reserva = resultado.reserva();
        return new ResultadoReservaExternaResponse(resultado.tipo().name(), reserva.getCodigo().valor(),
                reserva.getEstado().name(), reserva.getValorCongelado().enPesos(), mensaje);
    }
}
