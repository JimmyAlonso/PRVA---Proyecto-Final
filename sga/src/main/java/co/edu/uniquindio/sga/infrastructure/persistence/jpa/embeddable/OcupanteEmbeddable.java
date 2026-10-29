package co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDate;

/** Ocupante (3.2): se guarda la fecha de nacimiento, nunca la edad. */
@Embeddable
public class OcupanteEmbeddable {

    @Column(name = "documento", length = 20)
    private String documento;

    @Column(name = "nombre_completo", length = 150)
    private String nombreCompleto;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    protected OcupanteEmbeddable() {
        // requerido por JPA
    }

    public OcupanteEmbeddable(String documento, String nombreCompleto, LocalDate fechaNacimiento) {
        this.documento = documento;
        this.nombreCompleto = nombreCompleto;
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getDocumento() {
        return documento;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }
}
