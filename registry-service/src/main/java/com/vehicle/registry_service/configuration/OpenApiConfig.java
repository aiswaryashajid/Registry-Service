package com.vehicle.registry_service.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI vehicleRegistryOpenAPI() {
    return new OpenAPI()
        .info(new Info().title("Vehicle Registry Service API")
            .description(
                "APIs for registering, updating, retrieving, and deleting vehicle metadata")
            .version("v1.0")
            .contact(new Contact().name("Vehicle Platform Team").email("vehicle-team@company.com"))
            .license(new License().name("Internal Use")))

        .components(new Components().addSecuritySchemes("bearerAuth",
            new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer")
                .bearerFormat("JWT")))

        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
  }

}
