package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.entity.Usuario;
import co.edu.uniquindio.sga.domain.port.CifradorContrasenas;
import co.edu.uniquindio.sga.domain.repository.UsuarioRepository;
import co.edu.uniquindio.sga.domain.valueobject.IdUsuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 7.2 Perfil y contraseña del usuario que inició sesión. El correo no es editable. */
@Service
@Transactional
public class GestionarPerfilUseCase {

    private final UsuarioRepository usuarioRepository;
    private final CifradorContrasenas cifrador;

    public GestionarPerfilUseCase(UsuarioRepository usuarioRepository, CifradorContrasenas cifrador) {
        this.usuarioRepository = usuarioRepository;
        this.cifrador = cifrador;
    }

    @Transactional(readOnly = true)
    public Usuario consultar(IdUsuario id) {
        return Buscar.usuario(usuarioRepository, id);
    }

    public Usuario actualizar(IdUsuario id, String nombre, String telefono, String fotoPerfil) {
        Usuario usuario = Buscar.usuario(usuarioRepository, id);
        usuario.actualizarPerfil(nombre, telefono, fotoPerfil);
        usuarioRepository.guardar(usuario);
        return usuario;
    }

    public void cambiarContrasena(IdUsuario id, String contrasenaActual, String contrasenaNueva) {
        Usuario usuario = Buscar.usuario(usuarioRepository, id);
        usuario.cambiarContrasena(contrasenaActual, contrasenaNueva, cifrador);
        usuarioRepository.guardar(usuario);
    }
}
