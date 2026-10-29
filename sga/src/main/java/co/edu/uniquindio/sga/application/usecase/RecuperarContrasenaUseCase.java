package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.entity.Usuario;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.port.CifradorContrasenas;
import co.edu.uniquindio.sga.domain.port.NotificacionService;
import co.edu.uniquindio.sga.domain.repository.UsuarioRepository;
import co.edu.uniquindio.sga.domain.valueobject.CodigoRecuperacion;
import co.edu.uniquindio.sga.domain.valueobject.Correo;
import co.edu.uniquindio.sga.domain.valueobject.VigenciaCodigoRecuperacion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 7.2 Recuperación: código enviado al correo registrado, con vigencia de 15 minutos
 * (configuración) y de un solo uso. Solicitar un código responde igual exista o no el correo,
 * para no revelar qué cuentas están registradas.
 */
@Service
@Transactional
public class RecuperarContrasenaUseCase {

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final NotificacionService notificacionService;
    private final CifradorContrasenas cifrador;
    private final VigenciaCodigoRecuperacion vigencia;

    public RecuperarContrasenaUseCase(UsuarioRepository usuarioRepository, NotificacionService notificacionService,
                                      CifradorContrasenas cifrador, VigenciaCodigoRecuperacion vigencia) {
        this.usuarioRepository = usuarioRepository;
        this.notificacionService = notificacionService;
        this.cifrador = cifrador;
        this.vigencia = vigencia;
    }

    public void solicitarCodigo(Correo correo, LocalDateTime momento) {
        Optional<Usuario> encontrado = usuarioRepository.obtenerPorCorreo(correo).filter(Usuario::estaActivo);
        if (encontrado.isEmpty()) {
            return;
        }
        Usuario usuario = encontrado.get();
        String codigo = "%06d".formatted(ALEATORIO.nextInt(1_000_000));
        usuario.emitirCodigoRecuperacion(CodigoRecuperacion.emitir(codigo, momento, vigencia.minutos()));
        usuarioRepository.guardar(usuario);
        notificacionService.codigoRecuperacion(usuario.getCorreo(), codigo, vigencia.minutos());
    }

    public void restablecer(Correo correo, String codigo, String contrasenaNueva, LocalDateTime momento) {
        Usuario usuario = usuarioRepository.obtenerPorCorreo(correo)
                .orElseThrow(() -> new ReglaDominioException(
                        "El código de recuperación no es válido, ya se usó o está vencido."));
        usuario.restablecerContrasena(codigo, contrasenaNueva, momento, cifrador);
        usuarioRepository.guardar(usuario);
    }
}
