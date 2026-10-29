package co.edu.uniquindio.sga.infrastructure.rest.mapper;

import co.edu.uniquindio.sga.application.dto.response.UsuarioResponse;
import co.edu.uniquindio.sga.domain.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** 7.2: datos públicos del usuario. La contraseña cifrada y el código de recuperación no se exponen. */
@Mapper(componentModel = "spring")
public interface UsuarioRestMapper {

    @Mapping(target = "id", source = "id.valor")
    @Mapping(target = "correo", source = "correo.valor")
    @Mapping(target = "documento", source = "documento.valor")
    UsuarioResponse aUsuario(Usuario usuario);
}
