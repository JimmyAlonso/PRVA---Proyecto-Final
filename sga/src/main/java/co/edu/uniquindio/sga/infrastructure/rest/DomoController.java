package co.edu.uniquindio.sga.infrastructure.rest;

import co.edu.uniquindio.sga.application.dto.Pagina;
import co.edu.uniquindio.sga.application.dto.request.ActualizarDomoRequest;
import co.edu.uniquindio.sga.application.dto.request.CotizarEstanciaRequest;
import co.edu.uniquindio.sga.application.dto.request.EstablecerTarifasRequest;
import co.edu.uniquindio.sga.application.dto.request.RegistrarBloqueoRequest;
import co.edu.uniquindio.sga.application.dto.request.RegistrarDomoRequest;
import co.edu.uniquindio.sga.application.dto.request.RegistrarNovedadRequest;
import co.edu.uniquindio.sga.application.dto.response.CotizacionDomoResponse;
import co.edu.uniquindio.sga.application.dto.response.DomoActualizadoResponse;
import co.edu.uniquindio.sga.application.dto.response.DomoResponse;
import co.edu.uniquindio.sga.application.dto.response.NocheCalendarioResponse;
import co.edu.uniquindio.sga.application.dto.response.OperacionResponses.BloqueoResponse;
import co.edu.uniquindio.sga.application.dto.response.OperacionResponses.NovedadResponse;
import co.edu.uniquindio.sga.application.dto.response.RegistroTarifaResponse;
import co.edu.uniquindio.sga.application.usecase.BuscarDomosDisponiblesUseCase.Filtros;
import co.edu.uniquindio.sga.application.usecase.BuscarDomosDisponiblesUseCase;
import co.edu.uniquindio.sga.application.usecase.CotizarEstanciaUseCase;
import co.edu.uniquindio.sga.application.usecase.GestionarDomosUseCase;
import co.edu.uniquindio.sga.application.usecase.OperarDomoUseCase;
import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.entity.Domo;
import co.edu.uniquindio.sga.domain.entity.Novedad;
import co.edu.uniquindio.sga.domain.valueobject.Cotizacion;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.IdBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.DomoRestMapper;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.ReservaRestMapper;
import co.edu.uniquindio.sga.infrastructure.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
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
import java.util.List;

/**
 * Domos: catálogo y búsqueda (7.5), cotización (7.4), operación diaria (7.6) y gestión del
 * inventario, tarifas y bloqueos (7.3). Cada transición del estado operativo tiene su propio
 * endpoint de negocio: no existe un endpoint genérico de cambio de estado.
 */
@RestController
@RequestMapping("/api/v1/domos")
public class DomoController {

    private final OperarDomoUseCase operarDomoUseCase;
    private final GestionarDomosUseCase gestionarDomosUseCase;
    private final BuscarDomosDisponiblesUseCase buscarDisponiblesUseCase;
    private final CotizarEstanciaUseCase cotizarEstanciaUseCase;
    private final DomoRestMapper domoMapper;
    private final ReservaRestMapper reservaMapper;
    private final Clock reloj;

    public DomoController(OperarDomoUseCase operarDomoUseCase, GestionarDomosUseCase gestionarDomosUseCase,
                          BuscarDomosDisponiblesUseCase buscarDisponiblesUseCase,
                          CotizarEstanciaUseCase cotizarEstanciaUseCase, DomoRestMapper domoMapper,
                          ReservaRestMapper reservaMapper, Clock reloj) {
        this.operarDomoUseCase = operarDomoUseCase;
        this.gestionarDomosUseCase = gestionarDomosUseCase;
        this.buscarDisponiblesUseCase = buscarDisponiblesUseCase;
        this.cotizarEstanciaUseCase = cotizarEstanciaUseCase;
        this.domoMapper = domoMapper;
        this.reservaMapper = reservaMapper;
        this.reloj = reloj;
    }

    // ------------------------------------------------------------------ catálogo público (7.5)

    @GetMapping
    public ResponseEntity<Pagina<DomoResponse>> listar(@RequestParam(defaultValue = "0") int pagina) {
        return ResponseEntity.ok(operarDomoUseCase.listar(pagina).mapear(domoMapper::aDomo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DomoResponse> obtener(@PathVariable String id) {
        return ResponseEntity.ok(domoMapper.aDomo(operarDomoUseCase.consultar(new IdentificacionDomo(id))));
    }

    /**
     * 7.5 Búsqueda por fechas y composición del grupo (fechas de nacimiento o número de
     * ocupantes), con filtros de capacidad, precio, características y servicios. 10 por página.
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
        LocalDate hoy = LocalDate.now(reloj);
        Filtros filtros = new Filtros(capacidadMinima, precioMaximo == null ? null : Dinero.de(precioMaximo),
                caracteristica, conJacuzzi);
        var resultado = buscarDisponiblesUseCase.ejecutar(new Estancia(fechaEntrada, fechaSalida),
                reservaMapper.aGrupo(fechasNacimiento, numeroOcupantes, hoy), filtros, hoy, pagina);
        return ResponseEntity.ok(resultado.mapear(d -> reservaMapper.aCotizacion(d.domo(), d.cotizacion())));
    }

    /** 7.4 Cotización con el desglose noche por noche. */
    @PostMapping("/{id}/cotizacion")
    public ResponseEntity<CotizacionDomoResponse> cotizar(@PathVariable String id,
                                                          @Valid @RequestBody CotizarEstanciaRequest request) {
        IdentificacionDomo idDomo = new IdentificacionDomo(id);
        Cotizacion cotizacion = cotizarEstanciaUseCase.ejecutar(idDomo,
                new Estancia(request.fechaEntrada(), request.fechaSalida()),
                reservaMapper.aGrupo(request.fechasNacimiento()), LocalDate.now(reloj));
        return ResponseEntity.ok(reservaMapper.aCotizacion(operarDomoUseCase.consultar(idDomo), cotizacion));
    }

    /** 7.5 Calendario de ocupación del domo por rango de fechas (noches LIBRE, RESERVADA o BLOQUEADA). */
    @GetMapping("/{id}/calendario")
    public ResponseEntity<List<NocheCalendarioResponse>> calendario(
            @PathVariable String id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(domoMapper.aCalendario(
                operarDomoUseCase.calendario(new IdentificacionDomo(id), new Periodo(desde, hasta))));
    }

    // ------------------------------------------------------------------ operación diaria (7.6)

    @GetMapping("/pendientes-preparacion")
    public ResponseEntity<Pagina<DomoResponse>> pendientesDePreparacion(@RequestParam(defaultValue = "0") int pagina) {
        return ResponseEntity.ok(operarDomoUseCase.pendientesDePreparacion(pagina).mapear(domoMapper::aDomo));
    }

    /** PENDIENTE_PREPARACION → EN_PREPARACION (personal de servicio). */
    @PostMapping("/{id}/iniciar-preparacion")
    public ResponseEntity<DomoResponse> iniciarPreparacion(@PathVariable String id) {
        return ResponseEntity.ok(domoMapper.aDomo(operarDomoUseCase.iniciarPreparacion(new IdentificacionDomo(id))));
    }

    /** EN_PREPARACION → PREPARADO (personal de servicio). */
    @PostMapping("/{id}/marcar-preparado")
    public ResponseEntity<DomoResponse> marcarPreparado(@PathVariable String id) {
        return ResponseEntity.ok(domoMapper.aDomo(operarDomoUseCase.marcarPreparado(new IdentificacionDomo(id))));
    }

    /** 7.6: novedad con fecha y autor (el usuario autenticado), descripción y gravedad. */
    @PostMapping("/{id}/novedades")
    public ResponseEntity<NovedadResponse> registrarNovedad(@PathVariable String id,
                                                            @Valid @RequestBody RegistrarNovedadRequest request,
                                                            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        Novedad novedad = operarDomoUseCase.registrarNovedad(new IdentificacionDomo(id), request.tipo(),
                request.descripcion(), request.gravedad(), usuario.correo(), LocalDateTime.now(reloj));
        return ResponseEntity.created(URI.create("/api/v1/domos/" + id + "/novedades"))
                .body(domoMapper.aNovedad(novedad));
    }

    @GetMapping("/{id}/novedades")
    public ResponseEntity<Pagina<NovedadResponse>> historialNovedades(@PathVariable String id,
                                                                      @RequestParam(defaultValue = "0") int pagina) {
        return ResponseEntity.ok(operarDomoUseCase.historialNovedades(new IdentificacionDomo(id), pagina)
                .mapear(domoMapper::aNovedad));
    }

    // ------------------------------------------------------------------ administración del inventario (7.3, 7.4)

    @GetMapping("/inventario")
    public ResponseEntity<Pagina<DomoResponse>> inventario(@RequestParam(defaultValue = "0") int pagina) {
        return ResponseEntity.ok(gestionarDomosUseCase.inventario(pagina).mapear(domoMapper::aDomo));
    }

    @PostMapping
    public ResponseEntity<DomoResponse> registrar(@Valid @RequestBody RegistrarDomoRequest request) {
        Domo domo = gestionarDomosUseCase.registrar(domoMapper.aDomoNuevo(request), domoMapper.aTarifas(request.tarifas()),
                LocalDate.now(reloj));
        return ResponseEntity.created(URI.create("/api/v1/domos/" + domo.getIdentificacion().valor()))
                .body(domoMapper.aDomo(domo));
    }

    /** 7.3: actualiza datos y dotación; advierte las reservas que quedan por encima de la nueva capacidad. */
    @PutMapping("/{id}")
    public ResponseEntity<DomoActualizadoResponse> actualizar(@PathVariable String id,
                                                              @Valid @RequestBody ActualizarDomoRequest request) {
        var resultado = gestionarDomosUseCase.actualizar(new IdentificacionDomo(id), request.nombre(),
                request.descripcion(), request.dormitorios(), request.capacidad(), request.caracteristicas(),
                domoMapper.aJacuzzi(request.jacuzzi()), domoMapper.aMalla(request.mallaPesoMaximoKg()),
                domoMapper.aFogata(request.fogataKitsLena()));
        var excedidas = reservaMapper.aResumenes(resultado.reservasPorEncimaDeLaCapacidad());
        String advertencia = excedidas.isEmpty() ? null
                : excedidas.size() + " reserva(s) activa(s) superan la nueva capacidad; no se modificaron.";
        return ResponseEntity.ok(new DomoActualizadoResponse(domoMapper.aDomo(resultado.domo()), excedidas, advertencia));
    }

    /** 7.4: define o cambia tarifas; el valor anterior pasa al histórico. */
    @PutMapping("/{id}/tarifas")
    public ResponseEntity<DomoResponse> establecerTarifas(@PathVariable String id,
                                                          @Valid @RequestBody EstablecerTarifasRequest request) {
        return ResponseEntity.ok(domoMapper.aDomo(gestionarDomosUseCase.establecerTarifas(new IdentificacionDomo(id),
                domoMapper.aTarifas(request.tarifas()), LocalDate.now(reloj))));
    }

    @GetMapping("/{id}/tarifas/historial")
    public ResponseEntity<List<RegistroTarifaResponse>> historialTarifas(@PathVariable String id) {
        return ResponseEntity.ok(domoMapper.aRegistrosTarifa(
                gestionarDomosUseCase.historialTarifas(new IdentificacionDomo(id))));
    }

    @PostMapping("/{id}/activar")
    public ResponseEntity<DomoResponse> activar(@PathVariable String id) {
        return ResponseEntity.ok(domoMapper.aDomo(gestionarDomosUseCase.activar(new IdentificacionDomo(id))));
    }

    /** 7.3: retiro de la venta con eliminación lógica (no hay DELETE físico). */
    @PostMapping("/{id}/retirar")
    public ResponseEntity<DomoResponse> retirarDeVenta(@PathVariable String id) {
        return ResponseEntity.ok(domoMapper.aDomo(gestionarDomosUseCase.retirarDeVenta(new IdentificacionDomo(id))));
    }

    @PostMapping("/{id}/fuera-de-servicio")
    public ResponseEntity<DomoResponse> declararFueraDeServicio(@PathVariable String id) {
        return ResponseEntity.ok(domoMapper.aDomo(gestionarDomosUseCase.declararFueraDeServicio(new IdentificacionDomo(id))));
    }

    @PostMapping("/{id}/restablecer-servicio")
    public ResponseEntity<DomoResponse> restablecerServicio(@PathVariable String id) {
        return ResponseEntity.ok(domoMapper.aDomo(gestionarDomosUseCase.restablecerServicio(new IdentificacionDomo(id))));
    }

    // ------------------------------------------------------------------ bloqueos (7.3)

    @PostMapping("/{id}/bloqueos")
    public ResponseEntity<BloqueoResponse> registrarBloqueo(@PathVariable String id,
                                                            @Valid @RequestBody RegistrarBloqueoRequest request) {
        Bloqueo bloqueo = gestionarDomosUseCase.registrarBloqueo(new IdentificacionDomo(id),
                new Periodo(request.desde(), request.hasta()), request.motivo(), request.observacion());
        return ResponseEntity.created(URI.create("/api/v1/domos/" + id + "/bloqueos"))
                .body(domoMapper.aBloqueo(bloqueo));
    }

    @GetMapping("/{id}/bloqueos")
    public ResponseEntity<Pagina<BloqueoResponse>> bloqueos(@PathVariable String id,
                                                            @RequestParam(defaultValue = "0") int pagina) {
        return ResponseEntity.ok(operarDomoUseCase.bloqueos(new IdentificacionDomo(id), pagina)
                .mapear(domoMapper::aBloqueo));
    }

    /** Levantar un bloqueo es su eliminación lógica: queda como no vigente. */
    @PostMapping("/{id}/bloqueos/{idBloqueo}/levantar")
    public ResponseEntity<BloqueoResponse> levantarBloqueo(@PathVariable String id, @PathVariable String idBloqueo) {
        return ResponseEntity.ok(domoMapper.aBloqueo(gestionarDomosUseCase.levantarBloqueo(new IdentificacionDomo(id),
                new IdBloqueo(idBloqueo))));
    }
}
