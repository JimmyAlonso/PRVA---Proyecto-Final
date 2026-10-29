package co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** RN-17: autorización del administrador para cerrar con saldo distinto de cero. */
@Embeddable
public class AutorizacionCierreEmbeddable {

    @Column(name = "autorizacion_autor", length = 150)
    private String autor;

    @Column(name = "autorizacion_motivo", length = 300)
    private String motivo;

    protected AutorizacionCierreEmbeddable() {
        // requerido por JPA
    }

    public AutorizacionCierreEmbeddable(String autor, String motivo) {
        this.autor = autor;
        this.motivo = motivo;
    }

    public String getAutor() {
        return autor;
    }

    public String getMotivo() {
        return motivo;
    }
}
