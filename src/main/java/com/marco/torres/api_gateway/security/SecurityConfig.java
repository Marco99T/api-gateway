package com.marco.torres.api_gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .cors(ServerHttpSecurity.CorsSpec::disable)
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers("/users/hello").permitAll()
                        .pathMatchers("/orders/hello").permitAll()
                        .pathMatchers("/auth/**").permitAll()
                        .pathMatchers("/actuator/**").permitAll()
                        .anyExchange().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((exchange, authEx) -> {
                            exchange.getResponse().setRawStatusCode(401);
                            return exchange.getResponse().setComplete();
                        })
                        .accessDeniedHandler((exchange, accessDeniedEx) -> {
                            exchange.getResponse().setRawStatusCode(403);
                            return exchange.getResponse().setComplete();
                        }))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt())
                .build();
    }
}
