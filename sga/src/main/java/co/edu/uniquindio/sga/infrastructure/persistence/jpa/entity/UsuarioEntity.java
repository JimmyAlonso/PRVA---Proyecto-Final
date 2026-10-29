package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import co.edu.uniquindio.sga.domain.valueobject.Rol;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.CodigoRecuperacionEmbeddable;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDate;

/** Tabla de usuarios (7.2). La contraseña se guarda cifrada con BCrypt; la eliminación es lógica. */
@Entity
@Table(name = "usuario", indexes = @Index(name = "ix_usuario_documento", columnList = "documento"))
public class UsuarioEntity {

    @Id
    @Column(name = "id", length = 40)
    private String id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "correo", nullable = false, unique = true, length = 150)
    private String correo;

    @Column(name = "contrasena_cifrada", nullable = false, length = 100)
    private String contrasenaCifrada;

    @Column(name = "telefono", nullable = false, length = 30)
    private String telefono;

    @Column(name = "documento", nullable = false, length = 20)
    private String documento;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 20)
    private Rol rol;

    @Column(name = "foto_perfil", length = 500)
    private String fotoPerfil;

    @Column(name = "eliminado", nullable = false)
    private boolean eliminado;

    @Embedded
    private CodigoRecuperacionEmbeddable codigoRecuperacion;

    public UsuarioEntity() {
        // requerido por JPA
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasenaCifrada() {
        return contrasenaCifrada;
    }

    public void setContrasenaCifrada(String contrasenaCifrada) {
        this.contrasenaCifrada = contrasenaCifrada;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public boolean isEliminado() {
        return eliminado;
    }

    public void setEliminado(boolean eliminado) {
        this.eliminado = eliminado;
    }

    public CodigoRecuperacionEmbeddable getCodigoRecuperacion() {
        return codigoRecuperacion;
    }

    public void setCodigoRecuperacion(CodigoRecuperacionEmbeddable codigoRecuperacion) {
        this.codigoRecuperacion = codigoRecuperacion;
    }
}
