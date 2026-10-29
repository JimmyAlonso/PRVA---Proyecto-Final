package co.edu.uniquindio.sga.infrastructure.persistence.jpa.adapter;

import co.edu.uniquindio.sga.domain.entity.Usuario;
import co.edu.uniquindio.sga.domain.repository.Paginacion;
import co.edu.uniquindio.sga.domain.repository.ResultadoPaginado;
import co.edu.uniquindio.sga.domain.repository.UsuarioRepository;
import co.edu.uniquindio.sga.domain.valueobject.Correo;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.IdUsuario;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.UsuarioEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.UsuarioPersistenceMapper;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.UsuarioSpringDataRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UsuarioRepositoryJpaAdapter implements UsuarioRepository {

    private final UsuarioSpringDataRepository repositorio;
    private final UsuarioPersistenceMapper mapper;

    public UsuarioRepositoryJpaAdapter(UsuarioSpringDataRepository repositorio, UsuarioPersistenceMapper mapper) {
        this.repositorio = repositorio;
        this.mapper = mapper;
    }

    @Override
    public Optional<Usuario> obtenerPorId(IdUsuario id) {
        return repositorio.findById(id.valor()).map(mapper::aDominio);
    }

    @Override
    public Optional<Usuario> obtenerPorCorreo(Correo correo) {
        return repositorio.findByCorreo(correo.valor()).map(mapper::aDominio);
    }

    @Override
    public boolean existeCorreo(Correo correo) {
        return repositorio.existsByCorreo(correo.valor());
    }

    @Override
    public boolean existeDocumentoActivo(DocumentoIdentidad documento) {
        return repositorio.existsByDocumentoAndEliminadoFalse(documento.valor());
    }

    @Override
    public ResultadoPaginado<Usuario> listarActivos(Paginacion paginacion) {
        Page<UsuarioEntity> pagina = repositorio.findByEliminadoFalse(
                PageRequest.of(paginacion.pagina(), paginacion.tamano(), Sort.by("correo")));
        return new ResultadoPaginado<>(pagina.getContent().stream().map(mapper::aDominio).toList(),
                paginacion.pagina(), paginacion.tamano(), pagina.getTotalElements());
    }

    @Override
    public void guardar(Usuario usuario) {
        repositorio.save(mapper.aEntidad(usuario));
    }
}
