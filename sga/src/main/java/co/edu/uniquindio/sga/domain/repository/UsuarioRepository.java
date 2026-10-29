package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.entity.Usuario;
import co.edu.uniquindio.sga.domain.valueobject.Correo;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.IdUsuario;

import java.util.Optional;

public interface UsuarioRepository {

    Optional<Usuario> obtenerPorId(IdUsuario id);

    Optional<Usuario> obtenerPorCorreo(Correo correo);

    /** 7.2: el correo es único (también entre usuarios eliminados, porque no se reutiliza). */
    boolean existeCorreo(Correo correo);

    boolean existeDocumentoActivo(DocumentoIdentidad documento);

    /** 7.1 Administrador: usuarios no eliminados, paginados. */
    ResultadoPaginado<Usuario> listarActivos(Paginacion paginacion);

    void guardar(Usuario usuario);
}
