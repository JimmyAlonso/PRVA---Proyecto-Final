package co.edu.uniquindio.sga.infraestructure.config;

import co.edu.uniquindio.sga.application.usecase.BuscarDomosDisponiblesUseCase;
import co.edu.uniquindio.sga.application.usecase.CanalExternoUseCase;
import co.edu.uniquindio.sga.application.usecase.CancelarReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.ConfirmarReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarMovimientosDelDiaUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarReservasUseCase;
import co.edu.uniquindio.sga.application.usecase.CotizarEstanciaUseCase;
import co.edu.uniquindio.sga.application.usecase.CrearReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.DeclararNoShowUseCase;
import co.edu.uniquindio.sga.application.usecase.GestionarFolioUseCase;
import co.edu.uniquindio.sga.application.usecase.ModificarReservaUseCase;
import co.edu.uniquindio.sga.application.usecase.OperarDomoUseCase;
import co.edu.uniquindio.sga.application.usecase.RegistrarLlegadaUseCase;
import co.edu.uniquindio.sga.application.usecase.RegistrarSalidaUseCase;
import co.edu.uniquindio.sga.application.usecase.VencerReservasPendientesUseCase;
import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.port.NotificacionService;
import co.edu.uniquindio.sga.domain.repository.BloqueoRepository;
import co.edu.uniquindio.sga.domain.repository.DomoRepository;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.GeneradorCodigoReserva;
import co.edu.uniquindio.sga.domain.repository.NovedadRepository;
import co.edu.uniquindio.sga.domain.repository.PoliticaCancelacionRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;
import co.edu.uniquindio.sga.domain.service.CalendarioTemporadasService;
import co.edu.uniquindio.sga.domain.service.ConciliacionCanalExternoService;
import co.edu.uniquindio.sga.domain.service.ConfirmacionReservaService;
import co.edu.uniquindio.sga.domain.service.CotizadorEstanciaService;
import co.edu.uniquindio.sga.domain.service.DisponibilidadDomoService;
import co.edu.uniquindio.sga.domain.service.EntregaDomoService;
import co.edu.uniquindio.sga.domain.service.LiquidacionSalidaService;
import co.edu.uniquindio.sga.domain.service.RetencionService;
import co.edu.uniquindio.sga.domain.valueobject.Deposito;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.HoraLimiteNoShow;
import co.edu.uniquindio.sga.domain.valueobject.HorarioAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.ParametrosOperacion;
import co.edu.uniquindio.sga.domain.valueobject.PlazoConfirmacion;
import co.edu.uniquindio.sga.domain.valueobject.Porcentaje;
import co.edu.uniquindio.sga.domain.valueobject.RecargoPersonaAdicional;
import co.edu.uniquindio.sga.domain.valueobject.ServicioAdicional;
import co.edu.uniquindio.sga.domain.valueobject.TiempoPreparacion;
import co.edu.uniquindio.sga.domain.valueobject.UmbralEdadFacturable;
import co.edu.uniquindio.sga.infraestructure.notificacion.NotificacionLogAdapter;
import co.edu.uniquindio.sga.infraestructure.persistence.inmemory.BloqueoRepositoryInMemory;
import co.edu.uniquindio.sga.infraestructure.persistence.inmemory.DomoRepositoryInMemory;
import co.edu.uniquindio.sga.infraestructure.persistence.inmemory.FolioRepositoryInMemory;
import co.edu.uniquindio.sga.infraestructure.persistence.inmemory.GeneradorCodigoReservaInMemory;
import co.edu.uniquindio.sga.infraestructure.persistence.inmemory.NovedadRepositoryInMemory;
import co.edu.uniquindio.sga.infraestructure.persistence.inmemory.PoliticaCancelacionRepositoryInMemory;
import co.edu.uniquindio.sga.infraestructure.persistence.inmemory.ReservaRepositoryInMemory;
import co.edu.uniquindio.sga.infraestructure.persistence.inmemory.TemporadaRepositoryInMemory;
import co.edu.uniquindio.sga.infraestructure.rest.mapper.OperacionMapper;
import co.edu.uniquindio.sga.infraestructure.rest.mapper.ReservaMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Registro en Spring de lo que vive fuera de la infraestructura (Guía 05: "el registro en
 * Spring se hace en una clase de configuración fuera del dominio"). El dominio y los casos de
 * uso no tienen anotaciones; aquí se conectan con sus adaptadores.
 *
 * <p>Corrección: en el repositorio guía el controller pedía casos de uso que nadie había
 * registrado como beans, así que la aplicación no arrancaba.
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(SgaProperties.class)
public class DominioConfig {

    // ------------------------------------------------------------------ reloj y alojamiento

    /** 3.1: todas las fechas se manejan en la zona horaria de Colombia. */
    @Bean
    public Clock reloj() {
        return Clock.system(ZoneId.of("America/Bogota"));
    }

    @Bean
    public Alojamiento alojamiento(SgaProperties propiedades) {
        SgaProperties.DatosAlojamiento p = propiedades.alojamiento();
        HorarioAlojamiento horario = new HorarioAlojamiento(p.horaEntrada(), p.horaSalida());
        ParametrosOperacion parametros = new ParametrosOperacion(
                new UmbralEdadFacturable(p.umbralEdadFacturable()),
                new TiempoPreparacion(p.tiempoPreparacionHoras()),
                new PlazoConfirmacion(p.plazoConfirmacionHoras()),
                new HoraLimiteNoShow(p.horaLimiteNoShow()),
                new Porcentaje(p.anticipoPorcentaje()),
                new Deposito(Dinero.de(p.deposito())),
                new RecargoPersonaAdicional(p.recargoOcupantesIncluidos(), new Porcentaje(p.recargoPorcentaje())));
        Alojamiento alojamiento = new Alojamiento(p.nombre(), p.descripcion(), p.ciudad(), p.direccion(),
                p.latitud(), p.longitud(), horario, p.normasConvivencia(), parametros, p.aceptaMascotas());
        if (p.servicios() != null) {
            p.servicios().forEach(s -> alojamiento.agregarServicio(
                    new ServicioAdicional(s.codigo(), s.nombre(), Dinero.de(s.valor()), s.porPersona())));
        }
        return alojamiento;
    }

    // ------------------------------------------------------------------ adaptadores de salida (I-02)

    @Bean
    public ReservaRepository reservaRepository() {
        return new ReservaRepositoryInMemory();
    }

    @Bean
    public DomoRepository domoRepository() {
        return new DomoRepositoryInMemory();
    }

    @Bean
    public FolioRepository folioRepository() {
        return new FolioRepositoryInMemory();
    }

    @Bean
    public TemporadaRepository temporadaRepository() {
        return new TemporadaRepositoryInMemory();
    }

    @Bean
    public BloqueoRepository bloqueoRepository() {
        return new BloqueoRepositoryInMemory();
    }

    @Bean
    public NovedadRepository novedadRepository() {
        return new NovedadRepositoryInMemory();
    }

    @Bean
    public PoliticaCancelacionRepository politicaCancelacionRepository() {
        return new PoliticaCancelacionRepositoryInMemory();
    }

    @Bean
    public GeneradorCodigoReserva generadorCodigoReserva(Clock reloj) {
        return new GeneradorCodigoReservaInMemory(reloj);
    }

    @Bean
    public NotificacionService notificacionService() {
        return new NotificacionLogAdapter();
    }

    // ------------------------------------------------------------------ servicios de dominio

    @Bean
    public DisponibilidadDomoService disponibilidadDomoService(ReservaRepository reservas, BloqueoRepository bloqueos,
                                                               Alojamiento alojamiento) {
        return new DisponibilidadDomoService(reservas, bloqueos, alojamiento.getParametros().tiempoPreparacion(),
                alojamiento.getHorario());
    }

    @Bean
    public CotizadorEstanciaService cotizadorEstanciaService(Alojamiento alojamiento) {
        return new CotizadorEstanciaService(alojamiento.getParametros().umbralEdad(),
                alojamiento.getParametros().recargoPersonaAdicional());
    }

    @Bean
    public ConfirmacionReservaService confirmacionReservaService(Alojamiento alojamiento) {
        return new ConfirmacionReservaService(alojamiento.getParametros().anticipo(), alojamiento.getHorario());
    }

    @Bean
    public EntregaDomoService entregaDomoService() {
        return new EntregaDomoService();
    }

    @Bean
    public LiquidacionSalidaService liquidacionSalidaService() {
        return new LiquidacionSalidaService();
    }

    @Bean
    public RetencionService retencionService(Alojamiento alojamiento) {
        return new RetencionService(alojamiento.getHorario());
    }

    @Bean
    public CalendarioTemporadasService calendarioTemporadasService() {
        return new CalendarioTemporadasService();
    }

    @Bean
    public ConciliacionCanalExternoService conciliacionCanalExternoService(ReservaRepository reservas,
                                                                           DisponibilidadDomoService disponibilidad,
                                                                           CotizadorEstanciaService cotizador,
                                                                           Alojamiento alojamiento) {
        return new ConciliacionCanalExternoService(reservas, disponibilidad, cotizador,
                alojamiento.getParametros().umbralEdad());
    }

    // ------------------------------------------------------------------ casos de uso

    @Bean
    public CotizarEstanciaUseCase cotizarEstanciaUseCase(DomoRepository domos, TemporadaRepository temporadas,
                                                         CotizadorEstanciaService cotizador) {
        return new CotizarEstanciaUseCase(domos, temporadas, cotizador);
    }

    @Bean
    public BuscarDomosDisponiblesUseCase buscarDomosDisponiblesUseCase(DomoRepository domos,
                                                                       TemporadaRepository temporadas,
                                                                       DisponibilidadDomoService disponibilidad,
                                                                       CotizadorEstanciaService cotizador) {
        return new BuscarDomosDisponiblesUseCase(domos, temporadas, disponibilidad, cotizador);
    }

    @Bean
    public CrearReservaUseCase crearReservaUseCase(ReservaRepository reservas, DomoRepository domos,
                                                   FolioRepository folios, TemporadaRepository temporadas,
                                                   PoliticaCancelacionRepository politicas,
                                                   GeneradorCodigoReserva generador,
                                                   DisponibilidadDomoService disponibilidad,
                                                   CotizadorEstanciaService cotizador,
                                                   NotificacionService notificaciones, Alojamiento alojamiento) {
        return new CrearReservaUseCase(reservas, domos, folios, temporadas, politicas, generador, disponibilidad,
                cotizador, notificaciones, alojamiento.getParametros().umbralEdad());
    }

    @Bean
    public ConsultarReservaUseCase consultarReservaUseCase(ReservaRepository reservas) {
        return new ConsultarReservaUseCase(reservas);
    }

    @Bean
    public ConsultarReservasUseCase consultarReservasUseCase(ReservaRepository reservas) {
        return new ConsultarReservasUseCase(reservas);
    }

    @Bean
    public ConfirmarReservaUseCase confirmarReservaUseCase(ReservaRepository reservas, DomoRepository domos,
                                                           FolioRepository folios,
                                                           ConfirmacionReservaService confirmacion,
                                                           NotificacionService notificaciones) {
        return new ConfirmarReservaUseCase(reservas, domos, folios, confirmacion, notificaciones);
    }

    @Bean
    public ModificarReservaUseCase modificarReservaUseCase(ReservaRepository reservas, DomoRepository domos,
                                                           FolioRepository folios, TemporadaRepository temporadas,
                                                           DisponibilidadDomoService disponibilidad,
                                                           CotizadorEstanciaService cotizador, Alojamiento alojamiento) {
        return new ModificarReservaUseCase(reservas, domos, folios, temporadas, disponibilidad, cotizador,
                alojamiento.getParametros().umbralEdad());
    }

    @Bean
    public CancelarReservaUseCase cancelarReservaUseCase(ReservaRepository reservas, FolioRepository folios,
                                                         PoliticaCancelacionRepository politicas,
                                                         RetencionService retencion,
                                                         NotificacionService notificaciones) {
        return new CancelarReservaUseCase(reservas, folios, politicas, retencion, notificaciones);
    }

    @Bean
    public DeclararNoShowUseCase declararNoShowUseCase(ReservaRepository reservas, FolioRepository folios,
                                                       PoliticaCancelacionRepository politicas,
                                                       RetencionService retencion, Alojamiento alojamiento) {
        return new DeclararNoShowUseCase(reservas, folios, politicas, retencion,
                alojamiento.getParametros().horaLimiteNoShow());
    }

    @Bean
    public VencerReservasPendientesUseCase vencerReservasPendientesUseCase(ReservaRepository reservas,
                                                                           FolioRepository folios,
                                                                           NotificacionService notificaciones,
                                                                           Alojamiento alojamiento) {
        return new VencerReservasPendientesUseCase(reservas, folios, notificaciones,
                alojamiento.getParametros().plazoConfirmacion());
    }

    @Bean
    public RegistrarLlegadaUseCase registrarLlegadaUseCase(ReservaRepository reservas, DomoRepository domos,
                                                           FolioRepository folios, EntregaDomoService entrega) {
        return new RegistrarLlegadaUseCase(reservas, domos, folios, entrega);
    }

    @Bean
    public RegistrarSalidaUseCase registrarSalidaUseCase(ReservaRepository reservas, DomoRepository domos,
                                                         FolioRepository folios, LiquidacionSalidaService liquidacion) {
        return new RegistrarSalidaUseCase(reservas, domos, folios, liquidacion);
    }

    @Bean
    public GestionarFolioUseCase gestionarFolioUseCase(FolioRepository folios, Alojamiento alojamiento) {
        return new GestionarFolioUseCase(folios, alojamiento);
    }

    @Bean
    public OperarDomoUseCase operarDomoUseCase(DomoRepository domos, NovedadRepository novedades,
                                               BloqueoRepository bloqueos, ReservaRepository reservas) {
        return new OperarDomoUseCase(domos, novedades, bloqueos, reservas);
    }

    @Bean
    public ConsultarMovimientosDelDiaUseCase consultarMovimientosDelDiaUseCase(ReservaRepository reservas) {
        return new ConsultarMovimientosDelDiaUseCase(reservas);
    }

    @Bean
    public CanalExternoUseCase canalExternoUseCase(ConciliacionCanalExternoService conciliacion,
                                                   ReservaRepository reservas, DomoRepository domos,
                                                   TemporadaRepository temporadas,
                                                   PoliticaCancelacionRepository politicas, FolioRepository folios,
                                                   NovedadRepository novedades, GeneradorCodigoReserva generador,
                                                   NotificacionService notificaciones,
                                                   CancelarReservaUseCase cancelar) {
        return new CanalExternoUseCase(conciliacion, reservas, domos, temporadas, politicas, folios, novedades,
                generador, notificaciones, cancelar);
    }

    // ------------------------------------------------------------------ mappers de la API

    @Bean
    public ReservaMapper reservaMapper(Alojamiento alojamiento) {
        return new ReservaMapper(alojamiento.getParametros().umbralEdad());
    }

    @Bean
    public OperacionMapper operacionMapper() {
        return new OperacionMapper();
    }
}
