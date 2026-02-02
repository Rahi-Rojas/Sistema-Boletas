package com.rojas.spring.appgestion.productos.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // Cambiamos el nombre a algo que indique JWT
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("API de Gestión de Productos y Ventas")
                        .version("1.0")
                        .description("Documentación de los endpoints para el sistema de venta de Productos Rojas"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP) // Sigue siendo HTTP
                                        .scheme("bearer")            // CAMBIO AQUÍ: de "basic" a "bearer"
                                        .bearerFormat("JWT")));      // Indicamos que es un JWT
    }
}