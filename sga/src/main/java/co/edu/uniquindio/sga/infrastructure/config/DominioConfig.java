package co.edu.uniquindio.sga.infrastructure.config;

import co.edu.uniquindio.sga.domain.repository.BloqueoRepository;
import co.edu.uniquindio.sga.domain.repository.DomoRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.repository.UsuarioRepository;
import co.edu.uniquindio.sga.domain.service.CalendarioOcupacionService;
import co.edu.uniquindio.sga.domain.service.CalendarioTemporadasService;
import co.edu.uniquindio.sga.domain.service.ConciliacionCanalExternoService;
import co.edu.uniquindio.sga.domain.service.ConfirmacionReservaService;
import co.edu.uniquindio.sga.domain.service.CotizadorEstanciaService;
import co.edu.uniquindio.sga.domain.service.DisponibilidadDomoService;
import co.edu.uniquindio.sga.domain.service.EntregaDomoService;
import co.edu.uniquindio.sga.domain.service.GestionUsuariosService;
import co.edu.uniquindio.sga.domain.service.InventarioDomoService;
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
import co.edu.uniquindio.sga.domain.valueobject.TiempoPreparacion;
import co.edu.uniquindio.sga.domain.valueobject.UmbralEdadFacturable;
import co.edu.uniquindio.sga.domain.valueobject.VigenciaCodigoRecuperacion;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Registro en Spring de lo que vive fuera de la infraestructura. El dominio sigue sin conocer a
 * Spring: sus servicios son Java puro y se crean aquí como beans. Los casos de uso se registran
 * solos con @Service y los adaptadores con @Repository o @Component.
 *
 * <p>Los parámetros de la Ficha (sección 6) se leen de application.properties a través de
 * {@link SgaProperties}: cambiarlos no requiere recompilar.
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(SgaProperties.class)
public class DominioConfig {

    // ------------------------------------------------------------------ reloj y parámetros de la Ficha

    /** 3.1: todas las fechas se manejan en la zona horaria de Colombia. */
    @Bean
    public Clock reloj() {
        return Clock.system(ZoneId.of("America/Bogota"));
    }

    /** L-12: horas de entrada y salida. */
    @Bean
    public HorarioAlojamiento horarioAlojamiento(SgaProperties propiedades) {
        SgaProperties.DatosAlojamiento p = propiedades.alojamiento();
        return new HorarioAlojamiento(p.horaEntrada(), p.horaSalida());
    }

    /** L-09, L-11, L-13, L-14, L-15, L-20 y L-22: parámetros de operación del alojamiento. */
    @Bean
    public ParametrosOperacion parametrosOperacion(SgaProperties propiedades) {
        SgaProperties.DatosAlojamiento p = propiedades.alojamiento();
        return new ParametrosOperacion(
                new UmbralEdadFacturable(p.umbralEdadFacturable()),
                new TiempoPreparacion(p.tiempoPreparacionHoras()),
                new PlazoConfirmacion(p.plazoConfirmacionHoras()),
                new HoraLimiteNoShow(p.horaLimiteNoShow()),
                new Porcentaje(p.anticipoPorcentaje()),
                new Deposito(Dinero.de(p.deposito())),
                new RecargoPersonaAdicional(p.recargoOcupantesIncluidos(), new Porcentaje(p.recargoPorcentaje())));
    }

    @Bean
    public UmbralEdadFacturable umbralEdadFacturable(ParametrosOperacion parametros) {
        return parametros.umbralEdad();
    }

    @Bean
    public PlazoConfirmacion plazoConfirmacion(ParametrosOperacion parametros) {
        return parametros.plazoConfirmacion();
    }

    @Bean
    public HoraLimiteNoShow horaLimiteNoShow(ParametrosOperacion parametros) {
        return parametros.horaLimiteNoShow();
    }

    /** 7.2: vigencia del código de recuperación de contraseña. */
    @Bean
    public VigenciaCodigoRecuperacion vigenciaCodigoRecuperacion(SgaProperties propiedades) {
        int minutos = propiedades.seguridad() == null ? 15 : propiedades.seguridad().recuperacionMinutos();
        return new VigenciaCodigoRecuperacion(minutos);
    }

    // ------------------------------------------------------------------ servicios de dominio

    @Bean
    public DisponibilidadDomoService disponibilidadDomoService(ReservaRepository reservas, BloqueoRepository bloqueos,
                                                               ParametrosOperacion parametros,
                                                               HorarioAlojamiento horario) {
        return new DisponibilidadDomoService(reservas, bloqueos, parametros.tiempoPreparacion(), horario);
    }

    @Bean
    public CotizadorEstanciaService cotizadorEstanciaService(ParametrosOperacion parametros) {
        return new CotizadorEstanciaService(parametros.umbralEdad(), parametros.recargoPersonaAdicional());
    }

    @Bean
    public ConfirmacionReservaService confirmacionReservaService(ParametrosOperacion parametros,
                                                                 HorarioAlojamiento horario) {
        return new ConfirmacionReservaService(parametros.anticipo(), horario);
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
    public RetencionService retencionService(HorarioAlojamiento horario) {
        return new RetencionService(horario);
    }

    @Bean
    public CalendarioTemporadasService calendarioTemporadasService() {
        return new CalendarioTemporadasService();
    }

    @Bean
    public CalendarioOcupacionService calendarioOcupacionService(ReservaRepository reservas, BloqueoRepository bloqueos) {
        return new CalendarioOcupacionService(reservas, bloqueos);
    }

    @Bean
    public InventarioDomoService inventarioDomoService(ReservaRepository reservas, DomoRepository domos) {
        return new InventarioDomoService(reservas, domos);
    }

    @Bean
    public GestionUsuariosService gestionUsuariosService(UsuarioRepository usuarios, ReservaRepository reservas) {
        return new GestionUsuariosService(usuarios, reservas);
    }

    @Bean
    public ConciliacionCanalExternoService conciliacionCanalExternoService(ReservaRepository reservas,
                                                                           DisponibilidadDomoService disponibilidad,
                                                                           CotizadorEstanciaService cotizador,
                                                                           ParametrosOperacion parametros) {
        return new ConciliacionCanalExternoService(reservas, disponibilidad, cotizador, parametros.umbralEdad());
    }
}
