package com.marco.torres.api_gateway.dto;

public record ApiError(
        String type,
        String title,
        int status,
        String detail,
        String instance,
        String correlationId,
        String timestamp) {
}