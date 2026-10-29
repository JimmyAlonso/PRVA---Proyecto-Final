package co.edu.uniquindio.sga.domain.port;

/**
 * 7.2: la contraseña se guarda cifrada. El dominio solo conoce este puerto; la infraestructura
 * lo implementa con BCrypt, así el algoritmo puede cambiar sin tocar el dominio.
 */
public interface CifradorContrasenas {

    String cifrar(String contrasenaPlana);

    boolean coincide(String contrasenaPlana, String contrasenaCifrada);
}
