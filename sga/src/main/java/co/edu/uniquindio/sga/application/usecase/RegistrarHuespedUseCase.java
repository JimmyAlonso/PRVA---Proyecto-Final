package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.entity.Usuario;
import co.edu.uniquindio.sga.domain.port.CifradorContrasenas;
import co.edu.uniquindio.sga.domain.repository.UsuarioRepository;
import co.edu.uniquindio.sga.domain.service.GestionUsuariosService;
import co.edu.uniquindio.sga.domain.valueobject.Correo;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.IdUsuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

/**
 * 7.2 Registro público: crea un usuario con rol HUESPED. El formato del correo y la fortaleza
 * de la contraseña los valida el DTO (400); la unicidad del correo es regla del dominio (409).
 */
@Service
@Transactional
public class RegistrarHuespedUseCase {

    private final UsuarioRepository usuarioRepository;
    private final GestionUsuariosService gestionUsuarios;
    private final CifradorContrasenas cifrador;

    public RegistrarHuespedUseCase(UsuarioRepository usuarioRepository, GestionUsuariosService gestionUsuarios,
                                   CifradorContrasenas cifrador) {
        this.usuarioRepository = usuarioRepository;
        this.gestionUsuarios = gestionUsuarios;
        this.cifrador = cifrador;
    }

    public Usuario ejecutar(String nombre, Correo correo, String contrasena, String telefono,
                            DocumentoIdentidad documento, LocalDate fechaNacimiento) {
        gestionUsuarios.verificarRegistro(correo, documento);
        Usuario usuario = Usuario.registrarHuesped(new IdUsuario(UUID.randomUUID().toString()), nombre, correo,
                cifrador.cifrar(contrasena), telefono, documento, fechaNacimiento);
        usuarioRepository.guardar(usuario);
        return usuario;
    }
}
