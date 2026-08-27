package com.vizu.identidade.config;
import io.swagger.v3.oas.models.*; import io.swagger.v3.oas.models.info.Info; import io.swagger.v3.oas.models.security.SecurityScheme; import org.springframework.context.annotation.*;
@Configuration public class OpenApiConfig { @Bean OpenAPI vizuOpenApi(){return new OpenAPI().info(new Info().title("Vizu Identidade API").version("v1")).schemaRequirement("bearerAuth",new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT"));} }
