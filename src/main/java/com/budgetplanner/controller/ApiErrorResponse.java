package com.budgetplanner.controller;

/**
 * Error payload returned by the API exception handler.
 *
 * @param error the error code
 * @param message the human-readable error message
 */
public record ApiErrorResponse(String error, String message) {
}
