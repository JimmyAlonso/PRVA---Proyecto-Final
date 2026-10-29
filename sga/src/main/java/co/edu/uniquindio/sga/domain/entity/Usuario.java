package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.port.CifradorContrasenas;
import co.edu.uniquindio.sga.domain.valueobject.CodigoRecuperacion;
import co.edu.uniquindio.sga.domain.valueobject.Correo;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.IdUsuario;
import co.edu.uniquindio.sga.domain.valueobject.Rol;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Usuario: concepto de acceso (3.6), con credenciales y rol. No es el titular ni el ocupante:
 * ninguna reserva conoce al usuario. La relación con las reservas de un huésped se establece
 * por el documento de identidad del titular, así que una reserva creada por recepción o por un
 * canal externo puede tener titular sin cuenta.
 *
 * <p>La contraseña llega ya cifrada (el cifrado es un puerto del dominio implementado en la
 * infraestructura con BCrypt): el dominio nunca ve ni guarda una contraseña en claro.
 */
public class Usuario {

    private final IdUsuario id;
    private String nombre;
    private final Correo correo;              // 7.2: el correo no es editable
    private String contrasenaCifrada;
    private String telefono;
    private final DocumentoIdentidad documento;
    private final LocalDate fechaNacimiento;
    private Rol rol;
    private String fotoPerfil;
    private boolean eliminado;
    private CodigoRecuperacion codigoRecuperacion;

    private Usuario(IdUsuario id, String nombre, Correo correo, String contrasenaCifrada, String telefono,
                    DocumentoIdentidad documento, LocalDate fechaNacimiento, Rol rol) {
        if (id == null) {
            throw new ReglaDominioException("El usuario debe tener un identificador.");
        }
        if (correo == null) {
            throw new ReglaDominioException("El usuario debe tener correo electrónico.");
        }
        if (documento == null) {
            throw new ReglaDominioException("El usuario debe tener documento de identidad.");
        }
        if (fechaNacimiento == null) {
            throw new ReglaDominioException("El usuario debe tener fecha de nacimiento.");
        }
        if (rol == null) {
            throw new ReglaDominioException("El usuario debe tener un rol.");
        }
        validarNombreYTelefono(nombre, telefono);
        validarContrasena(contrasenaCifrada);
        this.id = id;
        this.nombre = nombre.trim();
        this.correo = correo;
        this.contrasenaCifrada = contrasenaCifrada;
        this.telefono = telefono.trim();
        this.documento = documento;
        this.fechaNacimiento = fechaNacimiento;
        this.rol = rol;
        this.eliminado = false;
    }

    /** 7.2: el registro público crea usuarios con rol HUESPED. */
    public static Usuario registrarHuesped(IdUsuario id, String nombre, Correo correo, String contrasenaCifrada,
                                           String telefono, DocumentoIdentidad documento, LocalDate fechaNacimiento) {
        return new Usuario(id, nombre, correo, contrasenaCifrada, telefono, documento, fechaNacimiento, Rol.HUESPED);
    }

    /** 7.2: los roles internos solo los asigna un administrador. */
    public static Usuario crearConRol(IdUsuario id, String nombre, Correo correo, String contrasenaCifrada,
                                      String telefono, DocumentoIdentidad documento, LocalDate fechaNacimiento, Rol rol) {
        return new Usuario(id, nombre, correo, contrasenaCifrada, telefono, documento, fechaNacimiento, rol);
    }

    /** Rehidrata un usuario ya guardado; no repite las validaciones de creación. */
    public static Usuario reconstruir(IdUsuario id, String nombre, Correo correo, String contrasenaCifrada,
                                      String telefono, DocumentoIdentidad documento, LocalDate fechaNacimiento, Rol rol,
                                      String fotoPerfil, boolean eliminado, CodigoRecuperacion codigoRecuperacion) {
        Usuario usuario = new Usuario(id, nombre, correo, contrasenaCifrada, telefono, documento, fechaNacimiento, rol);
        usuario.fotoPerfil = fotoPerfil;
        usuario.eliminado = eliminado;
        usuario.codigoRecuperacion = codigoRecuperacion;
        return usuario;
    }

    // ---------------------------------------------------------------- perfil (7.2)

    /** 7.2: se actualizan nombre, teléfono y foto de perfil. El correo no es editable. */
    public void actualizarPerfil(String nombre, String telefono, String fotoPerfil) {
        verificarActivo();
        validarNombreYTelefono(nombre, telefono);
        this.nombre = nombre.trim();
        this.telefono = telefono.trim();
        this.fotoPerfil = fotoPerfil == null || fotoPerfil.isBlank() ? null : fotoPerfil.trim();
    }

    /** 7.2: cambio voluntario de contraseña, con la contraseña actual y la nueva. */
    public void cambiarContrasena(String contrasenaActual, String contrasenaNueva, CifradorContrasenas cifrador) {
        verificarActivo();
        if (cifrador == null) {
            throw new ReglaDominioException("Se requiere el cifrador de contraseñas.");
        }
        if (contrasenaActual == null || !cifrador.coincide(contrasenaActual, contrasenaCifrada)) {
            throw new ReglaDominioException("La contraseña actual no es correcta.");
        }
        if (contrasenaNueva == null || contrasenaNueva.isBlank()) {
            throw new ReglaDominioException("La nueva contraseña es obligatoria.");
        }
        if (cifrador.coincide(contrasenaNueva, contrasenaCifrada)) {
            throw new ReglaDominioException("La nueva contraseña debe ser distinta de la actual.");
        }
        this.contrasenaCifrada = cifrador.cifrar(contrasenaNueva);
    }

    /** 7.2 Autenticación: las credenciales coinciden y la cuenta no está eliminada. */
    public boolean autenticaCon(String contrasenaPlana, CifradorContrasenas cifrador) {
        return !eliminado && contrasenaPlana != null && cifrador.coincide(contrasenaPlana, contrasenaCifrada);
    }

    // ---------------------------------------------------------------- recuperación (7.2)

    public void emitirCodigoRecuperacion(CodigoRecuperacion codigo) {
        verificarActivo();
        if (codigo == null) {
            throw new ReglaDominioException("El código de recuperación es obligatorio.");
        }
        this.codigoRecuperacion = codigo;
    }

    /** 7.2: el código tiene vigencia y es de un solo uso. */
    public void restablecerContrasena(String codigo, String contrasenaNueva, LocalDateTime momento,
                                      CifradorContrasenas cifrador) {
        verificarActivo();
        if (codigoRecuperacion == null || !codigoRecuperacion.esValido(codigo, momento)) {
            throw new ReglaDominioException("El código de recuperación no es válido, ya se usó o está vencido.");
        }
        if (contrasenaNueva == null || contrasenaNueva.isBlank()) {
            throw new ReglaDominioException("La nueva contraseña es obligatoria.");
        }
        this.contrasenaCifrada = cifrador.cifrar(contrasenaNueva);
        this.codigoRecuperacion = codigoRecuperacion.marcarUsado();
    }

    // ---------------------------------------------------------------- administración (7.1)

    public void asignarRol(Rol nuevoRol) {
        verificarActivo();
        if (nuevoRol == null) {
            throw new ReglaDominioException("El rol es obligatorio.");
        }
        this.rol = nuevoRol;
    }

    /**
     * 7.2: la eliminación es lógica. La condición de no ser titular de reservas activas la
     * verifica GestionUsuariosService, porque este agregado no ve las reservas.
     */
    public void eliminar() {
        verificarActivo();
        this.eliminado = true;
    }

    public boolean estaActivo() {
        return !eliminado;
    }

    private void verificarActivo() {
        if (eliminado) {
            throw new ReglaDominioException("El usuario está eliminado.");
        }
    }

    private static void validarNombreYTelefono(String nombre, String telefono) {
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaDominioException("El usuario debe tener nombre.");
        }
        if (telefono == null || telefono.isBlank()) {
            throw new ReglaDominioException("El usuario debe tener teléfono.");
        }
    }

    private static void validarContrasena(String contrasenaCifrada) {
        if (contrasenaCifrada == null || contrasenaCifrada.isBlank()) {
            throw new ReglaDominioException("El usuario debe tener contraseña.");
        }
    }

    // ---------------------------------------------------------------- getters

    public IdUsuario getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Correo getCorreo() {
        return correo;
    }

    public String getContrasenaCifrada() {
        return contrasenaCifrada;
    }

    public String getTelefono() {
        return telefono;
    }

    public DocumentoIdentidad getDocumento() {
        return documento;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public Rol getRol() {
        return rol;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public boolean isEliminado() {
        return eliminado;
    }

    public CodigoRecuperacion getCodigoRecuperacion() {
        return codigoRecuperacion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario usuario)) return false;
        return Objects.equals(id, usuario.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
