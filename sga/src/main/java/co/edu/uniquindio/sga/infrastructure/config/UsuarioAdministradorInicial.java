package co.edu.uniquindio.sga.infrastructure.config;

import co.edu.uniquindio.sga.application.usecase.AdministrarUsuariosUseCase;
import co.edu.uniquindio.sga.domain.repository.UsuarioRepository;
import co.edu.uniquindio.sga.domain.valueobject.Correo;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.Rol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Guía 22 (4.6): cuenta de administrador de producción. Sus datos no están en el repositorio:
 * llegan por las variables de entorno SGA_ADMIN_* que se definen en Render.
 *
 * <p>Se activa con sga.usuarios-iniciales.habilitados=true (por defecto en el perfil prod). En el
 * plan gratuito de Render la base H2 vuelve a cero en cada arranque, así que el administrador se
 * recrea cada vez; si ya existe (H2 en archivo o disco persistente) no se duplica.
 */
@Component
@Order(2)
public class UsuarioAdministradorInicial implements CommandLineRunner {

    private static final Logger LOG = Logger.getLogger(UsuarioAdministradorInicial.class.getName());

    private final boolean habilitado;
    private final AdministrarUsuariosUseCase usuarios;
    private final UsuarioRepository repositorio;
    private final String nombre;
    private final String correo;
    private final String contrasena;
    private final String telefono;
    private final String documento;
    private final String fechaNacimiento;

    public UsuarioAdministradorInicial(AdministrarUsuariosUseCase usuarios, UsuarioRepository repositorio,
                                       @Value("${sga.usuarios-iniciales.habilitados:false}") boolean habilitado,
                                       @Value("${sga.usuarios-iniciales.administrador.nombre:Administrador JiyJu}") String nombre,
                                       @Value("${sga.usuarios-iniciales.administrador.correo:}") String correo,
                                       @Value("${sga.usuarios-iniciales.administrador.contrasena:}") String contrasena,
                                       @Value("${sga.usuarios-iniciales.administrador.telefono:}") String telefono,
                                       @Value("${sga.usuarios-iniciales.administrador.documento:}") String documento,
                                       @Value("${sga.usuarios-iniciales.administrador.fecha-nacimiento:}") String fechaNacimiento) {
        this.habilitado = habilitado;
        this.usuarios = usuarios;
        this.repositorio = repositorio;
        this.nombre = nombre;
        this.correo = correo;
        this.contrasena = contrasena;
        this.telefono = telefono;
        this.documento = documento;
        this.fechaNacimiento = fechaNacimiento;
    }

    @Override
    public void run(String... args) {
        if (!habilitado) {
            return;
        }
        if (Stream.of(correo, contrasena, telefono, documento, fechaNacimiento)
                .anyMatch(valor -> valor == null || valor.isBlank())) {
            throw new IllegalStateException("Complete los datos de la cuenta inicial del SGA "
                    + "(SGA_ADMIN_CORREO, SGA_ADMIN_CONTRASENA, SGA_ADMIN_TELEFONO, SGA_ADMIN_DOCUMENTO, "
                    + "SGA_ADMIN_FECHA_NACIMIENTO).");
        }
        Correo correoAdmin = new Correo(correo.trim());
        if (repositorio.existeCorreo(correoAdmin)) {
            LOG.info("La cuenta inicial del administrador ya existe: no se vuelve a crear.");
            return;
        }
        usuarios.crear(nombre.trim(), correoAdmin, contrasena, telefono.trim(),
                new DocumentoIdentidad(documento.trim()), LocalDate.parse(fechaNacimiento.trim()), Rol.ADMINISTRADOR);
        LOG.info("Cuenta inicial del administrador creada: " + correoAdmin.valor());
    }
}
