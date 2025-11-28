package com.hacom.orderprocessing.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order Processing API")
                        .version("1.0.0")
                        .description("Reactive API for order processing with MongoDB, Akka, SMPP and gRPC"));
    }
}