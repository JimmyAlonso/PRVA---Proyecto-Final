package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.CifradorFalso;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CodigoRecuperacion;
import co.edu.uniquindio.sga.domain.valueobject.Correo;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.IdUsuario;
import co.edu.uniquindio.sga.domain.valueobject.Rol;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 7.2 Usuarios: registro, perfil, contraseña, recuperación y eliminación lógica. */
class UsuarioTest {

    private static final CifradorFalso CIFRADOR = new CifradorFalso();
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 1, 9, 0);

    private Usuario huesped() {
        return Usuario.registrarHuesped(new IdUsuario("U-1"), "Ana Ramírez", new Correo("Ana@Correo.com"),
                CIFRADOR.cifrar("Clave2026"), "3001234567", new DocumentoIdentidad("1094123456"), LocalDate.of(1990, 5, 12));
    }

    @Test
    void elRegistroPublicoCreaHuespedConCorreoNormalizado() {
        Usuario usuario = huesped();
        assertEquals(Rol.HUESPED, usuario.getRol());
        assertEquals("ana@correo.com", usuario.getCorreo().valor());
        assertTrue(usuario.estaActivo());
    }

    @Test
    void correoConFormatoInvalidoSeRechaza() {
        assertThrows(ReglaDominioException.class, () -> new Correo("ana.correo.com"));
    }

    @Test
    void autenticaSoloConLaContrasenaCorrectaYCuentaActiva() {
        Usuario usuario = huesped();
        assertTrue(usuario.autenticaCon("Clave2026", CIFRADOR));
        assertFalse(usuario.autenticaCon("Otra2026", CIFRADOR));
        usuario.eliminar();
        assertFalse(usuario.autenticaCon("Clave2026", CIFRADOR));
    }

    @Test
    void cambiarContrasenaExigeLaActualYQueLaNuevaSeaDistinta() {
        Usuario usuario = huesped();
        assertThrows(ReglaDominioException.class, () -> usuario.cambiarContrasena("Equivocada1", "Nueva2026", CIFRADOR));
        assertThrows(ReglaDominioException.class, () -> usuario.cambiarContrasena("Clave2026", "Clave2026", CIFRADOR));
        usuario.cambiarContrasena("Clave2026", "Nueva2026", CIFRADOR);
        assertTrue(usuario.autenticaCon("Nueva2026", CIFRADOR));
    }

    @Test
    void elPerfilActualizaNombreYTelefonoPeroNoElCorreo() {
        Usuario usuario = huesped();
        usuario.actualizarPerfil("Ana R.", "3110000000", "https://img/ana.png");
        assertEquals("Ana R.", usuario.getNombre());
        assertEquals("3110000000", usuario.getTelefono());
        assertEquals("ana@correo.com", usuario.getCorreo().valor());
    }

    @Test
    void elCodigoDeRecuperacionVenceYEsDeUnSoloUso() {
        Usuario usuario = huesped();
        usuario.emitirCodigoRecuperacion(CodigoRecuperacion.emitir("123456", AHORA, 15));
        // vencido a los 15 minutos
        assertThrows(ReglaDominioException.class,
                () -> usuario.restablecerContrasena("123456", "Nueva2026", AHORA.plusMinutes(15), CIFRADOR));
        // código equivocado
        assertThrows(ReglaDominioException.class,
                () -> usuario.restablecerContrasena("654321", "Nueva2026", AHORA.plusMinutes(5), CIFRADOR));
        usuario.restablecerContrasena("123456", "Nueva2026", AHORA.plusMinutes(5), CIFRADOR);
        assertTrue(usuario.autenticaCon("Nueva2026", CIFRADOR));
        // un solo uso
        assertThrows(ReglaDominioException.class,
                () -> usuario.restablecerContrasena("123456", "Otra2026", AHORA.plusMinutes(6), CIFRADOR));
    }

    @Test
    void unUsuarioEliminadoNoAdmiteCambios() {
        Usuario usuario = huesped();
        usuario.eliminar();
        assertTrue(usuario.isEliminado());
        assertThrows(ReglaDominioException.class, () -> usuario.asignarRol(Rol.RECEPCIONISTA));
        assertThrows(ReglaDominioException.class, usuario::eliminar);
    }
}
