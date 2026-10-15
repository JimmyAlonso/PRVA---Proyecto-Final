package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;

import java.util.Objects;

/**
 * Titular: responsable de la reserva y de su pago (sección 4). Es un concepto del negocio,
 * no un usuario (3.6): existe aunque nunca use el sistema. Además de ser ocupante, es quien
 * recibe las notificaciones, por eso se guardan correo y teléfono.
 */
public class TitularDeLaReserva {

    private final DocumentoIdentidad documento;
    private final String nombreCompleto;
    private String correo;
    private String telefono;

    public TitularDeLaReserva(DocumentoIdentidad documento, String nombreCompleto, String correo, String telefono) {
        if (documento == null) {
            throw new ReglaDominioException("El titular debe tener documento de identidad.");
        }
        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            throw new ReglaDominioException("El titular debe tener nombre completo.");
        }
        validarContacto(correo, telefono);
        this.documento = documento;
        this.nombreCompleto = nombreCompleto.trim();
        this.correo = correo.trim();
        this.telefono = telefono.trim();
    }

    public void actualizarContacto(String correo, String telefono) {
        validarContacto(correo, telefono);
        this.correo = correo.trim();
        this.telefono = telefono.trim();
    }

    private static void validarContacto(String correo, String telefono) {
        if (correo == null || !correo.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new ReglaDominioException("El titular debe tener un correo válido.");
        }
        if (telefono == null || telefono.isBlank()) {
            throw new ReglaDominioException("El titular debe tener teléfono.");
        }
    }

    public DocumentoIdentidad getDocumento() {
        return documento;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getCorreo() {
        return correo;
    }

    public String getTelefono() {
        return telefono;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof TitularDeLaReserva t && Objects.equals(documento, t.documento));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(documento);
    }
}
