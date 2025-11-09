package ru.practicum.shareit.common.exceptions;

import lombok.Data;

import java.util.Map;

@Data
public class ErrorResponse {
    private String error;
    private Map<String, String> details;

    public ErrorResponse(String error) {
        this.error = error;
    }

    public ErrorResponse(String error, Map<String, String> details) {
        this.error = error;
        this.details = details;
    }
}
