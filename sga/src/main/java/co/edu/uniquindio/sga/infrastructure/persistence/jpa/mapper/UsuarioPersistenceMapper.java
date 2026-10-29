package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.domain.entity.Usuario;
import co.edu.uniquindio.sga.domain.valueobject.CodigoRecuperacion;
import co.edu.uniquindio.sga.domain.valueobject.Correo;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.IdUsuario;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.CodigoRecuperacionEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.UsuarioEntity;
import org.springframework.stereotype.Component;

@Component
public class UsuarioPersistenceMapper {

    public UsuarioEntity aEntidad(Usuario usuario) {
        UsuarioEntity entidad = new UsuarioEntity();
        entidad.setId(usuario.getId().valor());
        entidad.setNombre(usuario.getNombre());
        entidad.setCorreo(usuario.getCorreo().valor());
        entidad.setContrasenaCifrada(usuario.getContrasenaCifrada());
        entidad.setTelefono(usuario.getTelefono());
        entidad.setDocumento(usuario.getDocumento().valor());
        entidad.setFechaNacimiento(usuario.getFechaNacimiento());
        entidad.setRol(usuario.getRol());
        entidad.setFotoPerfil(usuario.getFotoPerfil());
        entidad.setEliminado(usuario.isEliminado());
        CodigoRecuperacion codigo = usuario.getCodigoRecuperacion();
        entidad.setCodigoRecuperacion(codigo == null ? null
                : new CodigoRecuperacionEmbeddable(codigo.valor(), codigo.expiraEn(), codigo.usado()));
        return entidad;
    }

    public Usuario aDominio(UsuarioEntity entidad) {
        CodigoRecuperacionEmbeddable codigo = entidad.getCodigoRecuperacion();
        return Usuario.reconstruir(
                new IdUsuario(entidad.getId()),
                entidad.getNombre(),
                new Correo(entidad.getCorreo()),
                entidad.getContrasenaCifrada(),
                entidad.getTelefono(),
                new DocumentoIdentidad(entidad.getDocumento()),
                entidad.getFechaNacimiento(),
                entidad.getRol(),
                entidad.getFotoPerfil(),
                entidad.isEliminado(),
                codigo == null || codigo.getValor() == null ? null
                        : new CodigoRecuperacion(codigo.getValor(), codigo.getExpiraEn(), Boolean.TRUE.equals(codigo.getUsado())));
    }
}
