package co.edu.uniquindio.sga.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * 7.1 y 7.2: matriz de roles. Cada endpoint queda protegido por rol aquí, en un solo lugar; los
 * recursos con dueño (reservas y folios del huésped) se verifican además por propietario con
 * {@link AccesoReservas} desde @PreAuthorize.
 *
 * <p>Las tres operaciones del canal externo (10.3.1) no usan JWT: se autentican con la
 * credencial de cada canal (X-Canal-Id y X-Canal-Credencial), que verifica AutenticadorCanales.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SeguridadConfig {

    private static final String HUESPED = "HUESPED";
    private static final String RECEPCIONISTA = "RECEPCIONISTA";
    private static final String ADMINISTRADOR = "ADMINISTRADOR";
    private static final String PERSONAL_SERVICIO = "PERSONAL_SERVICIO";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Guía 22 (4.5): en producción (perfil prod) Swagger y la consola de H2 quedan cerradas.
     */
    @Value("${sga.seguridad.herramientas-desarrollo:true}")
    private boolean herramientasDesarrollo;

    @Bean
    public SecurityFilterChain cadenaDeSeguridad(HttpSecurity http, JwtService jwtService,
                                                 RespuestasSeguridad respuestas,
                                                 UrlBasedCorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)                       // API sin sesión ni cookies
                .cors(cors -> cors.configurationSource(corsConfigurationSource))   // Guía 22 (4.3)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))   // consola de H2
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(errores -> errores
                        .authenticationEntryPoint(respuestas)                // 401
                        .accessDeniedHandler(respuestas))                    // 403
                .authorizeHttpRequests(reglas -> {
                    // --- Guía 22 (4.4): health check público para Render, y errores
                    reglas.requestMatchers("/actuator/health", "/actuator/health/**", "/error").permitAll();
                    // --- Documentación y consola de H2: solo fuera de producción (Guía 22, 4.5)
                    if (herramientasDesarrollo) {
                        reglas.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/api-docs/**", "/v3/api-docs/**",
                                "/h2-console/**").permitAll();
                    }
                    reglas
                        // --- 7.2 Registro público, inicio de sesión y recuperación de contraseña
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/**").permitAll()

                        // --- 10.3.1 Canal externo: credencial por canal, no JWT
                        .requestMatchers("/api/v1/canales/externo/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/canales/conflictos").hasRole(ADMINISTRADOR)

                        // --- 7.1 Perfil propio (cualquier usuario autenticado)
                        .requestMatchers("/api/v1/usuarios/me", "/api/v1/usuarios/me/**").authenticated()
                        // --- 7.1 Administrador: gestionar usuarios y roles
                        .requestMatchers("/api/v1/usuarios", "/api/v1/usuarios/**").hasRole(ADMINISTRADOR)

                        // --- 7.3 Alojamiento: consulta pública, gestión del administrador
                        .requestMatchers(HttpMethod.GET, "/api/v1/alojamiento").permitAll()
                        .requestMatchers("/api/v1/alojamiento", "/api/v1/alojamiento/**").hasRole(ADMINISTRADOR)

                        // --- 7.4 Temporadas y política: consulta pública, gestión del administrador
                        .requestMatchers(HttpMethod.GET, "/api/v1/temporadas", "/api/v1/temporadas/*",
                                "/api/v1/politica-cancelacion/vigente").permitAll()
                        .requestMatchers("/api/v1/temporadas", "/api/v1/temporadas/**",
                                "/api/v1/politica-cancelacion/**").hasRole(ADMINISTRADOR)

                        // --- 7.6 Operación diaria del personal de servicio (antes de las rutas públicas de domos)
                        .requestMatchers(HttpMethod.GET, "/api/v1/domos/pendientes-preparacion")
                        .hasAnyRole(PERSONAL_SERVICIO, ADMINISTRADOR)
                        .requestMatchers(HttpMethod.POST, "/api/v1/domos/*/iniciar-preparacion",
                                "/api/v1/domos/*/marcar-preparado").hasAnyRole(PERSONAL_SERVICIO, ADMINISTRADOR)
                        .requestMatchers("/api/v1/domos/*/novedades")
                        .hasAnyRole(PERSONAL_SERVICIO, RECEPCIONISTA, ADMINISTRADOR)
                        .requestMatchers(HttpMethod.GET, "/api/v1/domos/*/bloqueos")
                        .hasAnyRole(RECEPCIONISTA, ADMINISTRADOR)

                        // --- 7.3 y 7.4 Gestión de domos, tarifas, bloqueos y estado FUERA_DE_SERVICIO (administrador)
                        .requestMatchers(HttpMethod.GET, "/api/v1/domos/inventario", "/api/v1/domos/*/tarifas/historial")
                        .hasRole(ADMINISTRADOR)
                        .requestMatchers(HttpMethod.POST, "/api/v1/domos", "/api/v1/domos/*/activar",
                                "/api/v1/domos/*/retirar", "/api/v1/domos/*/fuera-de-servicio",
                                "/api/v1/domos/*/restablecer-servicio", "/api/v1/domos/*/bloqueos",
                                "/api/v1/domos/*/bloqueos/*/levantar").hasRole(ADMINISTRADOR)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/domos/*", "/api/v1/domos/*/tarifas")
                        .hasRole(ADMINISTRADOR)

                        // --- 7.5 Búsqueda, detalle, calendario y cotización: públicos
                        .requestMatchers(HttpMethod.GET, "/api/v1/domos", "/api/v1/domos/disponibles", "/api/v1/domos/*",
                                "/api/v1/domos/*/calendario").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/domos/*/cotizacion").permitAll()

                        // --- 7.1 Huésped: sus reservas; la propiedad se verifica con @PreAuthorize
                        .requestMatchers(HttpMethod.GET, "/api/v1/reservas/mias").hasRole(HUESPED)
                        .requestMatchers(HttpMethod.POST, "/api/v1/reservas")
                        .hasAnyRole(HUESPED, RECEPCIONISTA, ADMINISTRADOR)
                        .requestMatchers(HttpMethod.GET, "/api/v1/reservas/movimientos")
                        .hasAnyRole(RECEPCIONISTA, ADMINISTRADOR)
                        .requestMatchers(HttpMethod.GET, "/api/v1/reservas/*", "/api/v1/reservas/*/folio")
                        .hasAnyRole(HUESPED, RECEPCIONISTA, ADMINISTRADOR)
                        .requestMatchers(HttpMethod.POST, "/api/v1/reservas/*/cancelar")
                        .hasAnyRole(HUESPED, RECEPCIONISTA, ADMINISTRADOR)

                        // --- Pago en línea del huésped: paga el anticipo y confirma su propia reserva.
                        //     La propiedad se verifica con @PreAuthorize en los controllers.
                        .requestMatchers(HttpMethod.POST, "/api/v1/reservas/*/folio/pagos", "/api/v1/reservas/*/confirmar")
                        .hasAnyRole(HUESPED, RECEPCIONISTA, ADMINISTRADOR)

                        // --- 7.1 Recepción: listado, modificación, ciclo de vida y folio
                        .requestMatchers("/api/v1/reservas", "/api/v1/reservas/**").hasAnyRole(RECEPCIONISTA, ADMINISTRADOR)

                        .anyRequest().authenticated();
                })
                .addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Guía 22 (4.3): CORS dentro de la cadena de Spring Security. Los orígenes permitidos se leen de
     * sga.cors.allowed-origins (en Render: variable SGA_CORS_ALLOWED_ORIGINS), separados por coma.
     */
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource(
            @Value("${sga.cors.allowed-origins:http://localhost:5173,http://localhost:5174}") String origenes) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(origenes.split(","))
                .map(String::trim)
                .map(origen -> origen.endsWith("/") ? origen.substring(0, origen.length() - 1) : origen)
                .filter(origen -> !origen.isEmpty())
                .toList());
        // Métodos del API del SGA: DELETE se usa para eliminar usuarios (7.1)
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setExposedHeaders(List.of("Location"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
