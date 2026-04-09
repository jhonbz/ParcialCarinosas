package com.carinosas.api_gateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("workflow-service", r -> r.path("/tasks/**")
                        .uri("http://workflow-service:8085"))

                // 1. Rutas de Personas (MUY ESPECÍFICO)
                .route("people-service", r -> r.path("/people/**", "/cases/*/people/**")
                        .uri("http://people-service:8083"))

                // 2. Rutas de Evidencias (MUY ESPECÍFICO)
                .route("evidence-service", r -> r.path("/evidences/**", "/cases/*/evidences/**")
                        .uri("http://evidence-service:8082"))

                // 3. Rutas de Casos (GENERAL - Debe ir al final para que no atrape a las otras dos)
                .route("case-service", r -> r.path("/cases/**")
                        .uri("http://case-service:8081"))

                .build();
    }
}