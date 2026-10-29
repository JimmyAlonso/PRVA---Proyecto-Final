package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.dto.Pagina;
import co.edu.uniquindio.sga.domain.entity.Usuario;
import co.edu.uniquindio.sga.domain.port.CifradorContrasenas;
import co.edu.uniquindio.sga.domain.repository.UsuarioRepository;
import co.edu.uniquindio.sga.domain.service.GestionUsuariosService;
import co.edu.uniquindio.sga.domain.valueobject.Correo;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.IdUsuario;
import co.edu.uniquindio.sga.domain.valueobject.Rol;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

/** 7.1 Administrador: gestionar usuarios y roles. 7.2: las eliminaciones son lógicas. */
@Service
@Transactional
public class AdministrarUsuariosUseCase {

    private final UsuarioRepository usuarioRepository;
    private final GestionUsuariosService gestionUsuarios;
    private final CifradorContrasenas cifrador;

    public AdministrarUsuariosUseCase(UsuarioRepository usuarioRepository, GestionUsuariosService gestionUsuarios,
                                      CifradorContrasenas cifrador) {
        this.usuarioRepository = usuarioRepository;
        this.gestionUsuarios = gestionUsuarios;
        this.cifrador = cifrador;
    }

    @Transactional(readOnly = true)
    public Pagina<Usuario> listar(int pagina) {
        return Pagina.desde(usuarioRepository.listarActivos(Pagina.solicitud(pagina)));
    }

    @Transactional(readOnly = true)
    public Usuario consultar(IdUsuario id) {
        return Buscar.usuario(usuarioRepository, id);
    }

    /** 7.2: los roles internos solo los asigna un administrador. */
    public Usuario crear(String nombre, Correo correo, String contrasena, String telefono, DocumentoIdentidad documento,
                         LocalDate fechaNacimiento, Rol rol) {
        gestionUsuarios.verificarRegistro(correo, documento);
        Usuario usuario = Usuario.crearConRol(new IdUsuario(UUID.randomUUID().toString()), nombre, correo,
                cifrador.cifrar(contrasena), telefono, documento, fechaNacimiento, rol);
        usuarioRepository.guardar(usuario);
        return usuario;
    }

    public Usuario asignarRol(IdUsuario id, Rol rol) {
        Usuario usuario = Buscar.usuario(usuarioRepository, id);
        usuario.asignarRol(rol);
        usuarioRepository.guardar(usuario);
        return usuario;
    }

    /** 7.2: no se elimina a un titular de reservas activas; eliminarlo no borra sus reservas históricas. */
    public void eliminar(IdUsuario id) {
        Usuario usuario = Buscar.usuario(usuarioRepository, id);
        gestionUsuarios.eliminar(usuario);
        usuarioRepository.guardar(usuario);
    }
}
