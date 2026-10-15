package co.edu.uniquindio.sga.infraestructure.config;

import co.edu.uniquindio.sga.application.usecase.CanalExternoUseCase;
import co.edu.uniquindio.sga.application.usecase.CancelarReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.ConfirmarReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.CrearReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.DeclararNoShowUseCase;
import co.edu.uniquindio.sga.application.usecase.GestionarFolioUseCase;
import co.edu.uniquindio.sga.application.usecase.OperarDomoUseCase;
import co.edu.uniquindio.sga.application.usecase.RegistrarLlegadaUseCase;
import co.edu.uniquindio.sga.application.usecase.RegistrarSalidaUseCase;
import co.edu.uniquindio.sga.domain.entity.Domo;
import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.entity.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.entity.TitularDeLaReserva;
import co.edu.uniquindio.sga.domain.repository.DomoRepository;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.PoliticaCancelacionRepository;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;
import co.edu.uniquindio.sga.domain.service.CalendarioTemporadasService;
import co.edu.uniquindio.sga.domain.service.ConfirmacionReservaService;
import co.edu.uniquindio.sga.domain.valueobject.AutorizacionCierre;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.DatosReservaExterna;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.Fogata;
import co.edu.uniquindio.sga.domain.valueobject.GravedadNovedad;
import co.edu.uniquindio.sga.domain.valueobject.HoraEstimadaLlegada;
import co.edu.uniquindio.sga.domain.valueobject.IdTemporada;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;
import co.edu.uniquindio.sga.domain.valueobject.IdentificadorExterno;
import co.edu.uniquindio.sga.domain.valueobject.Jacuzzi;
import co.edu.uniquindio.sga.domain.valueobject.Malla;
import co.edu.uniquindio.sga.domain.valueobject.MedioPago;
import co.edu.uniquindio.sga.domain.valueobject.MotivoBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;
import co.edu.uniquindio.sga.domain.valueobject.Porcentaje;
import co.edu.uniquindio.sga.domain.valueobject.Tarifa;
import co.edu.uniquindio.sga.domain.valueobject.TipoNovedad;
import co.edu.uniquindio.sga.domain.valueobject.TramoRetencion;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.logging.Logger;

/**
 * 12.3 Datos de prueba mínimos de JiyJu Glamping (Ficha A): los 5 domos con tarifas en todas
 * las temporadas, la política de cancelación, un bloqueo vigente, un domo fuera de servicio,
 * reservas en todos los estados y al menos una por cada canal. Las reservas se crean a través
 * de los casos de uso para que pasen por las mismas reglas que la API.
 *
 * <p>Se desactiva con sga.datos-prueba.habilitados=false (por ejemplo, en pruebas).
 * Las fechas de festivos son datos (calendario colombiano 2026-2027), no reglas.
 */
@Component
public class DatosDePrueba implements CommandLineRunner {

    private static final Logger LOG = Logger.getLogger(DatosDePrueba.class.getName());

    private static final IdTemporada BASE = new IdTemporada("BASE");
    private static final IdTemporada ALTA = new IdTemporada("ALTA");
    private static final IdTemporada PUENTES = new IdTemporada("PUENTES");

    private final SgaProperties propiedades;
    private final TemporadaRepository temporadas;
    private final DomoRepository domos;
    private final PoliticaCancelacionRepository politicas;
    private final FolioRepository folios;
    private final CalendarioTemporadasService calendario;
    private final ConfirmacionReservaService confirmacion;
    private final CrearReservaUseCase crear;
    private final ConfirmarReservaUseCase confirmar;
    private final CancelarReservaUseCase cancelar;
    private final DeclararNoShowUseCase noShow;
    private final RegistrarLlegadaUseCase llegada;
    private final RegistrarSalidaUseCase salida;
    private final GestionarFolioUseCase folio;
    private final OperarDomoUseCase operacion;
    private final CanalExternoUseCase canalExterno;
    private final Clock reloj;

    public DatosDePrueba(SgaProperties propiedades, TemporadaRepository temporadas, DomoRepository domos,
                         PoliticaCancelacionRepository politicas, FolioRepository folios,
                         CalendarioTemporadasService calendario, ConfirmacionReservaService confirmacion,
                         CrearReservaUseCase crear, ConfirmarReservaUseCase confirmar, CancelarReservaUseCase cancelar,
                         DeclararNoShowUseCase noShow, RegistrarLlegadaUseCase llegada, RegistrarSalidaUseCase salida,
                         GestionarFolioUseCase folio, OperarDomoUseCase operacion, CanalExternoUseCase canalExterno,
                         Clock reloj) {
        this.propiedades = propiedades;
        this.temporadas = temporadas;
        this.domos = domos;
        this.politicas = politicas;
        this.folios = folios;
        this.calendario = calendario;
        this.confirmacion = confirmacion;
        this.crear = crear;
        this.confirmar = confirmar;
        this.cancelar = cancelar;
        this.noShow = noShow;
        this.llegada = llegada;
        this.salida = salida;
        this.folio = folio;
        this.operacion = operacion;
        this.canalExterno = canalExterno;
        this.reloj = reloj;
    }

    @Override
    public void run(String... args) {
        if (propiedades.datosPrueba() == null || !propiedades.datosPrueba().habilitados()) {
            return;
        }
        cargarTemporadas();
        cargarDomos();
        cargarPolitica();
        cargarReservas();
        cargarEstadosOperativos();
        LOG.info("Datos de prueba de JiyJu Glamping cargados.");
    }

    // ------------------------------------------------------------------ Ficha A.3

    private void cargarTemporadas() {
        temporadas.guardar(Temporada.base(BASE, "Base"));
        temporadas.guardar(new Temporada(ALTA, "Alta", List.of(
                rango("2026-12-15", "2027-01-15"),
                rango("2027-03-22", "2027-03-28"),     // Semana Santa 2027
                rango("2027-12-15", "2028-01-15")), false));
        // Fines de semana con lunes festivo: noches de viernes, sábado y domingo
        temporadas.guardar(new Temporada(PUENTES, "Puentes y Festivos", List.of(
                rango("2026-10-30", "2026-11-01"), rango("2026-11-13", "2026-11-15"),
                rango("2027-03-19", "2027-03-21"), rango("2027-05-07", "2027-05-09"),
                rango("2027-05-28", "2027-05-30"), rango("2027-06-04", "2027-06-06"),
                rango("2027-07-02", "2027-07-04"), rango("2027-08-13", "2027-08-15"),
                rango("2027-10-15", "2027-10-17"), rango("2027-10-29", "2027-10-31"),
                rango("2027-11-12", "2027-11-14")), false));
        calendario.verificarCalendario(temporadas.buscarTodas());
    }

    // ------------------------------------------------------------------ Ficha A.2 y A.3

    private void cargarDomos() {
        int horasJacuzzi = propiedades.alojamiento().jacuzziHorasPrecalentamiento();
        guardarDomo("DOM-01", "Domo Básico", 1, 2, 180_000, 280_000, 230_000,
                List.of("Vista panorámica", "Jacuzzi exterior a leña", "Malla catamarán privada"),
                new Jacuzzi("Jacuzzi exterior a leña", horasJacuzzi), new Malla(250), null);
        guardarDomo("DOM-02", "Domo Básico", 1, 2, 180_000, 280_000, 230_000,
                List.of("Vista al valle", "Jacuzzi exterior a leña", "Malla catamarán privada"),
                new Jacuzzi("Jacuzzi exterior a leña", horasJacuzzi), new Malla(250), null);
        guardarDomo("DOM-03", "Domo Familiar", 2, 4, 250_000, 350_000, 330_000,
                List.of("Doble espacio", "Zona de fogata privada amplia", "Jacuzzi"),
                new Jacuzzi("Jacuzzi", horasJacuzzi), null, new Fogata(2));
        guardarDomo("DOM-04", "Domo Exclusivo", 1, 2, 220_000, 320_000, 260_000,
                List.of("Malla catamarán", "Terraza privada", "Proyector de cine"),
                null, new Malla(250), null);
        guardarDomo("DOM-05", "Domo Familiar", 2, 4, 250_000, 350_000, 330_000,
                List.of("Doble espacio", "Acceso directo a senderos", "Zona de fogata", "Jacuzzi ecológico"),
                new Jacuzzi("Jacuzzi ecológico", horasJacuzzi), null, new Fogata(1));
    }

    private void guardarDomo(String id, String nombre, int dormitorios, int capacidad, long base, long alta,
                             long puentes, List<String> caracteristicas, Jacuzzi jacuzzi, Malla malla, Fogata fogata) {
        Domo domo = new Domo(new IdentificacionDomo(id), nombre, dormitorios, capacidad);
        domo.describir(nombre + " de JiyJu Glamping, Salento.", caracteristicas);
        if (jacuzzi != null) domo.equiparJacuzzi(jacuzzi);
        if (malla != null) domo.equiparMalla(malla);
        if (fogata != null) domo.equiparFogata(fogata);
        domo.definirTarifa(new Tarifa(BASE, Dinero.de(base)));
        domo.definirTarifa(new Tarifa(ALTA, Dinero.de(alta)));
        domo.definirTarifa(new Tarifa(PUENTES, Dinero.de(puentes)));
        domos.guardar(domo);
    }

    // ------------------------------------------------------------------ Ficha A.4 (política)

    private void cargarPolitica() {
        // ≥ 15 días: retiene 10 %; entre 14 y 5 días: 50 %; menos de 5 días: 100 %. No-show: 100 %.
        politicas.guardar(new PoliticaCancelacion(new VersionPolitica(1, LocalDate.of(2026, 1, 1)), List.of(
                new TramoRetencion(15 * 24, Porcentaje.de(10)),
                new TramoRetencion(5 * 24, Porcentaje.de(50)),
                new TramoRetencion(0, Porcentaje.de(100))), Porcentaje.de(100)));
    }

    // ------------------------------------------------------------------ 12.3: reservas en todos los estados

    private void cargarReservas() {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        LocalDate hoy = ahora.toLocalDate();
        HoraEstimadaLlegada llegada18 = new HoraEstimadaLlegada(LocalTime.of(18, 30));   // cumple L-21

        // PENDIENTE (canal PORTAL)
        crearReserva("DOM-01", hoy.plusDays(20), hoy.plusDays(22), null, titular("1094000001", "Valentina Ríos"),
                CanalOrigen.PORTAL, ahora);

        // CONFIRMADA (canal DIRECTO): anticipo del 50 % pagado por transferencia
        Reserva confirmada = crearReserva("DOM-02", hoy.plusDays(10), hoy.plusDays(12), llegada18,
                titular("1094000002", "Santiago Gómez"), CanalOrigen.DIRECTO, ahora);
        pagarAnticipoYConfirmar(confirmada, hoy);

        // EN_CURSO: llegó hoy al DOM-03 con su depósito (L-22)
        Reserva enCurso = crearReserva("DOM-03", hoy, hoy.plusDays(2), llegada18,
                titular("1094000003", "Laura Martínez"), CanalOrigen.DIRECTO, ahora);
        pagarAnticipoYConfirmar(enCurso, hoy);
        folio.registrarDeposito(enCurso.getCodigo(), hoy);
        llegada.ejecutar(enCurso.getCodigo(), hoy);

        // FINALIZADA: estancia en DOM-05 con salida anticipada (7.5: es salida, no cancelación)
        Reserva finalizada = crearReserva("DOM-05", hoy, hoy.plusDays(1), llegada18,
                titular("1094000004", "Andrés Cardona"), CanalOrigen.PORTAL, ahora);
        pagarAnticipoYConfirmar(finalizada, hoy);
        folio.registrarDeposito(finalizada.getCodigo(), hoy);
        llegada.ejecutar(finalizada.getCodigo(), hoy);
        folio.registrarServicio(finalizada.getCodigo(), "KIT_FOGATA", 1, hoy);
        folio.devolverDeposito(finalizada.getCodigo(), hoy);
        var folioFinal = folios.obtenerPorReserva(finalizada.getCodigo()).orElseThrow();
        if (folioFinal.saldo().debeHuesped()) {
            folio.registrarPago(finalizada.getCodigo(), new Dinero(folioFinal.saldo().valor()), MedioPago.EFECTIVO, hoy);
        }
        salida.ejecutar(finalizada.getCodigo(), folios.obtenerPorReserva(finalizada.getCodigo()).orElseThrow()
                .saldo().esCero() ? null : new AutorizacionCierre("admin", "Saldo a favor devuelto fuera del sistema"));

        // CANCELADA (canal PORTAL), con más de 15 días: retiene el 10 %
        Reserva cancelada = crearReserva("DOM-01", hoy.plusDays(40), hoy.plusDays(43), null,
                titular("1094000005", "Camila Herrera"), CanalOrigen.PORTAL, ahora);
        cancelar.ejecutar(cancelada.getCodigo(), "El huésped cambió sus planes de viaje", ahora);

        // NO_SHOW: confirmada para hoy en DOM-01; se simula que ya pasó la hora límite (20:00)
        Reserva sinPresentarse = crearReserva("DOM-01", hoy, hoy.plusDays(1), llegada18,
                titular("1094000006", "Mateo López"), CanalOrigen.DIRECTO, ahora);
        pagarAnticipoYConfirmar(sinPresentarse, hoy);
        noShow.ejecutar(sinPresentarse.getCodigo(), hoy.atTime(21, 0));

        // Canal EXTERNO: una aceptada, un mensaje repetido (no duplica) y un conflicto registrado
        DatosReservaExterna externa = datosExternos("EXT-1001", "DOM-02", hoy.plusDays(30), hoy.plusDays(32),
                titular("1094000007", "Sofía Ramírez"));
        canalExterno.recibirReserva(externa, ahora);
        canalExterno.recibirReserva(externa, ahora);
        canalExterno.recibirReserva(datosExternos("EXT-1002", "DOM-02", hoy.plusDays(31), hoy.plusDays(33),
                titular("1094000008", "Daniel Torres")), ahora);
    }

    private void cargarEstadosOperativos() {
        LocalDate hoy = LocalDate.now(reloj);
        // 12.3: un domo fuera de servicio y un bloqueo vigente
        operacion.cambiarEstado(new IdentificacionDomo("DOM-04"),
                OperarDomoUseCase.AccionEstadoOperativo.DECLARAR_FUERA_DE_SERVICIO);
        operacion.registrarBloqueo(new IdentificacionDomo("DOM-04"), hoy, hoy.plusDays(7), MotivoBloqueo.MANTENIMIENTO,
                "Reparación del proyector de cine");
        operacion.registrarNovedad(new IdentificacionDomo("DOM-04"), TipoNovedad.DANO,
                "El proyector de cine no enciende", GravedadNovedad.MODERADA, "personal.servicio",
                LocalDateTime.now(reloj));
    }

    // ------------------------------------------------------------------ utilidades

    private Reserva crearReserva(String domo, LocalDate entrada, LocalDate salida, HoraEstimadaLlegada hora,
                                 TitularDeLaReserva titular, CanalOrigen canal, LocalDateTime ahora) {
        return crear.ejecutar(new IdentificacionDomo(domo), new Estancia(entrada, salida), hora, titular,
                List.of(comoOcupante(titular), acompanante(titular)), canal, ahora);
    }

    private void pagarAnticipoYConfirmar(Reserva reserva, LocalDate hoy) {
        folio.registrarPago(reserva.getCodigo(), confirmacion.anticipoExigido(reserva), MedioPago.TRANSFERENCIA, hoy);
        confirmar.ejecutar(reserva.getCodigo(), null);
    }

    private DatosReservaExterna datosExternos(String idExterno, String domo, LocalDate entrada, LocalDate salida,
                                              TitularDeLaReserva titular) {
        return new DatosReservaExterna(new IdentificadorExterno("BOOKING_COM", idExterno), new IdentificacionDomo(domo),
                new Estancia(entrada, salida), titular, List.of(comoOcupante(titular), acompanante(titular)),
                new HoraEstimadaLlegada(LocalTime.of(19, 0)));
    }

    private static TitularDeLaReserva titular(String documento, String nombre) {
        String usuario = nombre.toLowerCase().replace(" ", ".").replace("í", "i").replace("á", "a")
                .replace("é", "e").replace("ó", "o").replace("ú", "u");
        return new TitularDeLaReserva(new DocumentoIdentidad(documento), nombre, usuario + "@correo.com", "3001234567");
    }

    private static Ocupante comoOcupante(TitularDeLaReserva titular) {
        return new Ocupante(titular.getDocumento(), titular.getNombreCompleto(), LocalDate.of(1992, 4, 10));
    }

    private static Ocupante acompanante(TitularDeLaReserva titular) {
        return new Ocupante(new DocumentoIdentidad(titular.getDocumento().valor() + "9"), "Acompañante de "
                + titular.getNombreCompleto(), LocalDate.of(1994, 8, 21));
    }

    private static Periodo rango(String desde, String hasta) {
        return new Periodo(LocalDate.parse(desde), LocalDate.parse(hasta));
    }
}
