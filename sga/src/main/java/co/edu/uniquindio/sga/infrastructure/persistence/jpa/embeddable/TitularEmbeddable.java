package co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Titular de la reserva (3.6): persona del negocio, con o sin cuenta de usuario. */
@Embeddable
public class TitularEmbeddable {

    @Column(name = "titular_documento", length = 20)
    private String documento;

    @Column(name = "titular_nombre", length = 150)
    private String nombreCompleto;

    @Column(name = "titular_correo", length = 150)
    private String correo;

    @Column(name = "titular_telefono", length = 30)
    private String telefono;

    protected TitularEmbeddable() {
        // requerido por JPA
    }

    public TitularEmbeddable(String documento, String nombreCompleto, String correo, String telefono) {
        this.documento = documento;
        this.nombreCompleto = nombreCompleto;
        this.correo = correo;
        this.telefono = telefono;
    }

    public String getDocumento() {
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
}
