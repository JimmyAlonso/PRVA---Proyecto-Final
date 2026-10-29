package co.edu.uniquindio.sga.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger UI (Guía 08) con el botón Authorize: se pega el token que devuelve
 * POST /api/v1/auth/login y Swagger lo envía como "Authorization: Bearer {token}".
 * El contrato de referencia es docs/api/openapi.yaml.
 */
@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA = "bearerAuth";

    @Bean
    public OpenAPI sgaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("SGA — JiyJu Glamping")
                        .version("2.0.0")
                        .description("Sistema de Gestión de Alojamiento. Corte 2: usuarios y seguridad, alojamiento "
                                + "y domos, temporadas, tarifas y política, disponibilidad y reservas."))
                .components(new Components().addSecuritySchemes(ESQUEMA, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA));
    }
}
