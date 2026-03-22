package com.marco.torres.api_gateway.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marco.torres.api_gateway.dto.ApiError;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Configuration
@Order(-2)
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String title = "Internal Server Error";
        String detail = "Unexpected error";

        if (ex instanceof MethodNotAllowedException) {
            status = HttpStatus.METHOD_NOT_ALLOWED;
            title = "Method Not Allowed";
            detail = "HTTP method not supported for this endpoint";
        } else if (ex instanceof io.github.resilience4j.circuitbreaker.CallNotPermittedException) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            title = "Service Unavailable";
            detail = "Circuit breaker is open";
        } else if (ex instanceof java.io.IOException ||
                ex instanceof java.util.concurrent.TimeoutException) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            title = "Service Unavailable";
            detail = "Upstream service failed";
        } else if (ex instanceof ResponseStatusException rse) {
            status = (HttpStatus) rse.getStatusCode();
            title = status.getReasonPhrase();
            detail = rse.getReason();
        }

        String correlationId = exchange.getRequest().getHeaders().getFirst("X-Correlation-Id");

        ApiError error = new ApiError(
                "https://api.company.com/errors/" + status.value(),
                title,
                status.value(),
                detail,
                exchange.getRequest().getPath().value(),
                correlationId,
                Instant.now().toString());

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[] bytes;
        try {
            bytes = mapper.writeValueAsBytes(error);
        } catch (Exception e) {
            bytes = new byte[0];
        }

        return exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse()
                        .bufferFactory()
                        .wrap(bytes)));
    }
}
