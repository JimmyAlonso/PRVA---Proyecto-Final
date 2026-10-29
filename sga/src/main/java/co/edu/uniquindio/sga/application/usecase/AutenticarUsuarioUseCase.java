package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.CredencialesInvalidasException;
import co.edu.uniquindio.sga.domain.entity.Usuario;
import co.edu.uniquindio.sga.domain.port.CifradorContrasenas;
import co.edu.uniquindio.sga.domain.repository.UsuarioRepository;
import co.edu.uniquindio.sga.domain.valueobject.Correo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 7.2 Inicio de sesión: verifica las credenciales. Emitir el JWT es asunto de la
 * infraestructura (3.6: la autenticación pertenece a la infraestructura).
 */
@Service
@Transactional(readOnly = true)
public class AutenticarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final CifradorContrasenas cifrador;

    public AutenticarUsuarioUseCase(UsuarioRepository usuarioRepository, CifradorContrasenas cifrador) {
        this.usuarioRepository = usuarioRepository;
        this.cifrador = cifrador;
    }

    public Usuario ejecutar(Correo correo, String contrasena) {
        return usuarioRepository.obtenerPorCorreo(correo)
                .filter(usuario -> usuario.autenticaCon(contrasena, cifrador))
                .orElseThrow(CredencialesInvalidasException::new);
    }
}
