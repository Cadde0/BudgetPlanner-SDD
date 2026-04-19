package com.budgetplanner.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void illegalArgumentReturnsBadRequestResponse() {
        ResponseEntity<ApiErrorResponse> response = handler
                .handleIllegalArgument(new IllegalArgumentException("Invalid amount."));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("BAD_REQUEST", response.getBody().error());
        assertEquals("Invalid amount.", response.getBody().message());
    }

    @Test
    void unhandledExceptionReturnsInternalServerErrorResponse() {
        ResponseEntity<ApiErrorResponse> response = handler.handleUnhandled(new RuntimeException("Boom"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().error());
        assertEquals("An unexpected error occurred.", response.getBody().message());
    }
}
