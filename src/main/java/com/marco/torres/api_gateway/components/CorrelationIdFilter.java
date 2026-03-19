package com.marco.torres.api_gateway.components;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.core.Ordered;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    private static final String HEADER = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,GatewayFilterChain chain) {

        String correlationId = exchange.getRequest().getHeaders().getFirst(HEADER);

        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }

        exchange.getResponse().getHeaders().add(HEADER, correlationId);

        return chain.filter(
                exchange.mutate()
                        .request(exchange.getRequest()
                                .mutate()
                                .header(HEADER, correlationId)
                                .build())
                        .build());
    }

    @Override
    public int getOrder() {
        return -1;
    }
}