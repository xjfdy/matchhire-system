package com.matchhire.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class GatewayConfig {

    @Value("${services.auth-service.url:http://localhost:8081}")
    private String authServiceUrl;

    @Value("${services.job-service.url:http://localhost:8080}")
    private String jobServiceUrl;

    @Value("${services.application-service.url:http://localhost:8082}")
    private String applicationServiceUrl;

    @Bean
    public RouterFunction<ServerResponse> authServiceRoute() {
        return GatewayRouterFunctions.route("auth-service")
                .route(RequestPredicates.path("/auth/**"),
                        HandlerFunctions.http(authServiceUrl))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> jobServiceRoute() {
        return GatewayRouterFunctions.route("job-service")
                .route(RequestPredicates.path("/jobs/**"),
                        HandlerFunctions.http(jobServiceUrl))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> applicationServiceRoute() {
        return GatewayRouterFunctions.route("application-service")
                .route(RequestPredicates.path("/applications/**"),
                        HandlerFunctions.http(applicationServiceUrl))
                .build();
    }
}