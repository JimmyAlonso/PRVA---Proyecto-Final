package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Alojamiento<ServicioAdicional, RecargoPersonaAdicional> {
        private String nombre, descripcion, ciudad, direccion, normasConvivencia;
        private BigDecimal latitud, longitud;
        private LocalTime horaEntrada, horaSalida, horaLimiteNoShow;
        @Getter
        private UmbralEdadFacturable umbralEdad;
        @Getter
        private TiempoPreparacion tiempoPreparacion;
        @Getter
        private PlazoConfirmacion plazoConfirmacion;
        private Porcentaje anticipo;                 // 0 = no se exige (L-11)
        @Getter
        private Deposito deposito;
        @Getter
        private RecargoPersonaAdicional recargo;
        @Getter
        private List<ServicioAdicional> servicios = List.of();

        public Alojamiento(String nombre, String descripcion, String ciudad, String direccion,
                           BigDecimal latitud, BigDecimal longitud,
                           LocalTime horaEntrada, LocalTime horaSalida, String normasConvivencia,
                           UmbralEdadFacturable umbralEdad, TiempoPreparacion tiempoPreparacion,
                           PlazoConfirmacion plazoConfirmacion, LocalTime horaLimiteNoShow,
                           Porcentaje anticipo, Deposito deposito, RecargoPersonaAdicional recargo) {
            if (nombre == null || nombre.isBlank()) throw new ReglaDominioException("El alojamiento debe tener nombre.");
            if (ciudad == null || ciudad.isBlank()) throw new ReglaDominioException("El alojamiento debe indicar la ciudad.");
            if (latitud == null || longitud == null) throw new ReglaDominioException("Falta la ubicación exacta.");
            if (horaEntrada == null || horaSalida == null) throw new ReglaDominioException("Faltan hora de entrada y de salida.");
            if (umbralEdad == null || tiempoPreparacion == null || plazoConfirmacion == null
                    || horaLimiteNoShow == null || anticipo == null || deposito == null || recargo == null)
                throw new ReglaDominioException("Faltan parámetros de configuración del alojamiento.");
            this.nombre = nombre; this.descripcion = descripcion; this.ciudad = ciudad; this.direccion = direccion;
            this.latitud = latitud; this.longitud = longitud;
            this.horaEntrada = horaEntrada; this.horaSalida = horaSalida; this.normasConvivencia = normasConvivencia;
            this.umbralEdad = umbralEdad; this.tiempoPreparacion = tiempoPreparacion;
            this.plazoConfirmacion = plazoConfirmacion; this.horaLimiteNoShow = horaLimiteNoShow;
            this.anticipo = anticipo; this.deposito = deposito; this.recargo = recargo;
        }

        public void agregarServicio(ServicioAdicional servicio) {
            if (servicio == null) throw new ReglaDominioException("El servicio adicional es obligatorio.");
            List<ServicioAdicional> nuevos = new ArrayList<>(servicios);
            nuevos.add(servicio);
            this.servicios = List.copyOf(nuevos);
        }

        public boolean exigeAnticipo() { return anticipo.valor().signum() > 0; }
        public Dinero anticipoSobre(Dinero valorReserva) { return anticipo.aplicarA(valorReserva); }

        public String getNombre() { return nombre; }
        public String getCiudad() { return ciudad; }
        public LocalTime getHoraEntrada() { return horaEntrada; }
        public LocalTime getHoraSalida() { return horaSalida; }
        public LocalTime getHoraLimiteNoShow() { return horaLimiteNoShow; }

    public Porcentaje getAnticipo() { return anticipo; }

}
