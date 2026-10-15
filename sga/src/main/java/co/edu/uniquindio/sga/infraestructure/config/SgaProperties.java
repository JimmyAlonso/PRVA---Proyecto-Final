package co.edu.uniquindio.sga.infraestructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Sección 6 y Anexo A: todo lo que cambia de un alojamiento a otro es configuración
 * (application.properties o variables de entorno), nunca constantes en el código. Si hubiera
 * que recompilar para cambiar un valor, el diseño estaría mal.
 */
@ConfigurationProperties(prefix = "sga")
public record SgaProperties(DatosAlojamiento alojamiento, Canales canales, DatosPrueba datosPrueba,
                            Vencimiento vencimiento) {

    public record DatosAlojamiento(
            String nombre,
            String descripcion,
            String ciudad,
            String direccion,
            BigDecimal latitud,
            BigDecimal longitud,
            LocalTime horaEntrada,
            LocalTime horaSalida,
            String normasConvivencia,
            int umbralEdadFacturable,
            int tiempoPreparacionHoras,
            int plazoConfirmacionHoras,
            LocalTime horaLimiteNoShow,
            BigDecimal anticipoPorcentaje,
            long deposito,
            int recargoOcupantesIncluidos,
            BigDecimal recargoPorcentaje,
            int jacuzziHorasPrecalentamiento,
            boolean aceptaMascotas,
            List<Servicio> servicios) {
    }

    public record Servicio(String codigo, String nombre, long valor, boolean porPersona) {
    }

    /** 7.8: canales externos registrados con su credencial de acceso (I-06: se inyectan por entorno). */
    public record Canales(Map<String, String> credenciales) {
    }

    public record DatosPrueba(boolean habilitados) {
    }

    /** RN-21: cada cuánto revisa el sistema las reservas pendientes vencidas. */
    public record Vencimiento(long intervaloMs) {
    }
}
