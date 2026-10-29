package co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

/** L-17: servicio adicional del alojamiento y su cargo. */
@Embeddable
public class ServicioAdicionalEmbeddable {

    @Column(name = "codigo", length = 40)
    private String codigo;

    @Column(name = "nombre", length = 150)
    private String nombre;

    @Column(name = "valor", precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(name = "por_persona")
    private Boolean porPersona;

    protected ServicioAdicionalEmbeddable() {
        // requerido por JPA
    }

    public ServicioAdicionalEmbeddable(String codigo, String nombre, BigDecimal valor, Boolean porPersona) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.valor = valor;
        this.porPersona = porPersona;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public Boolean getPorPersona() {
        return porPersona;
    }
}
