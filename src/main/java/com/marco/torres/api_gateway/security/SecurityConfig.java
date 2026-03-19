package com.marco.torres.api_gateway.security;

import java.util.List;

import org.apache.hc.core5.http.HttpStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers("/auth/**").permitAll()
                        .pathMatchers("/actuator/**").permitAll()
                        .anyExchange().authenticated())

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((exchange, exAuth) -> {
                            var response = exchange.getResponse();
                            var headers = response.getHeaders();

                            headers.add("Access-Control-Allow-Origin", "http://localhost:5173");
                            headers.add("Access-Control-Allow-Headers", "*");
                            headers.add("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
                            headers.add("Access-Control-Allow-Credentials", "true");

                            response.setRawStatusCode(401);
                            return response.setComplete();
                        })
                        // .authenticationEntryPoint((exchange, authEx) -> {
                        // exchange.getResponse().setRawStatusCode(HttpStatus.SC_UNAUTHORIZED);
                        // return exchange.getResponse().setComplete();
                        // })
                        .accessDeniedHandler((exchange, accessDeniedEx) -> {
                            exchange.getResponse().setRawStatusCode(HttpStatus.SC_UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        }))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt())
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("*"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
