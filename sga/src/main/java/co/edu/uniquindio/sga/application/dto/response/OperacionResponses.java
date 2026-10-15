package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.domain.valueobject.GravedadNovedad;
import co.edu.uniquindio.sga.domain.valueobject.MotivoBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.TipoNovedad;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/** Respuestas de operación diaria (7.6), canal externo (7.8) y alojamiento (7.3). */
public final class OperacionResponses {

    private OperacionResponses() {
    }

    public record NovedadResponse(String id, String idDomo, TipoNovedad tipo, String descripcion,
                                  GravedadNovedad gravedad, String autor, LocalDateTime momentoReporte,
                                  boolean atendida) {
    }

    public record BloqueoResponse(String id, String idDomo, LocalDate desde, LocalDate hasta,
                                  MotivoBloqueo motivo, String observacion, boolean vigente) {
    }

    public record MovimientosDelDiaResponse(LocalDate fecha, List<ReservaResumenResponse> llegadas,
                                            List<ReservaResumenResponse> salidas) {
    }

    /** CREADA (201), DUPLICADA (200, RN-19) o CONFLICTO (409, RN-18). */
    public record ResultadoReservaExternaResponse(String resultado, String codigoReserva, String estado,
                                                  long valorTotal, String mensaje) {
    }

    public record ServicioAdicionalResponse(String codigo, String nombre, long valor, boolean porPersona) {
    }

    public record AlojamientoResponse(String nombre, String descripcion, String ciudad, String direccion,
                                      String latitud, String longitud, LocalTime horaEntrada, LocalTime horaSalida,
                                      String normasConvivencia, int umbralEdadFacturable, int tiempoPreparacionHoras,
                                      int plazoConfirmacionHoras, LocalTime horaLimiteNoShow, long anticipoPorcentaje,
                                      long deposito, boolean aceptaMascotas, List<ServicioAdicionalResponse> servicios) {
    }
}
