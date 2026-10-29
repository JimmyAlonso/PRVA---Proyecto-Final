package co.edu.uniquindio.sga.infrastructure.persistence.inmemory;

import co.edu.uniquindio.sga.domain.entity.Usuario;
import co.edu.uniquindio.sga.domain.repository.Paginacion;
import co.edu.uniquindio.sga.domain.repository.ResultadoPaginado;
import co.edu.uniquindio.sga.domain.repository.UsuarioRepository;
import co.edu.uniquindio.sga.domain.valueobject.Correo;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.IdUsuario;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Adaptador en memoria (I-02) para pruebas sin base de datos. */
public class UsuarioRepositoryInMemory implements UsuarioRepository {

    private final Map<IdUsuario, Usuario> usuarios = new ConcurrentHashMap<>();

    @Override
    public Optional<Usuario> obtenerPorId(IdUsuario id) {
        return Optional.ofNullable(usuarios.get(id));
    }

    @Override
    public Optional<Usuario> obtenerPorCorreo(Correo correo) {
        return usuarios.values().stream().filter(usuario -> usuario.getCorreo().equals(correo)).findFirst();
    }

    @Override
    public boolean existeCorreo(Correo correo) {
        return obtenerPorCorreo(correo).isPresent();
    }

    @Override
    public boolean existeDocumentoActivo(DocumentoIdentidad documento) {
        return usuarios.values().stream()
                .anyMatch(usuario -> usuario.estaActivo() && usuario.getDocumento().equals(documento));
    }

    @Override
    public ResultadoPaginado<Usuario> listarActivos(Paginacion paginacion) {
        return ResultadoPaginado.de(usuarios.values().stream()
                .filter(Usuario::estaActivo)
                .sorted(Comparator.comparing(usuario -> usuario.getCorreo().valor()))
                .toList(), paginacion);
    }

    @Override
    public void guardar(Usuario usuario) {
        usuarios.put(usuario.getId(), usuario);
    }
}
