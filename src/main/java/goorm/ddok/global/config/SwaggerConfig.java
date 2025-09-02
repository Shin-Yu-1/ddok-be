package goorm.ddok.global.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    private OpenAPI base() {
        return new OpenAPI()
                .info(new Info().title("DDOK API").description("DDOK 프로젝트 REST API 문서").version("v1"))
                .components(new Components().addSecuritySchemes(
                        SECURITY_SCHEME_NAME, new SecurityScheme().name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }

    @Bean @org.springframework.context.annotation.Profile("local")
    public OpenAPI openAPILocal() {
        return base().servers(List.of(new Server().url("http://localhost:8080").description("Local")));
    }

    @Bean @org.springframework.context.annotation.Profile("prod")
    public OpenAPI openAPIProd() {
        return base().servers(List.of(new Server().url("https://api.ddok.site").description("Prod")));
    }
}
