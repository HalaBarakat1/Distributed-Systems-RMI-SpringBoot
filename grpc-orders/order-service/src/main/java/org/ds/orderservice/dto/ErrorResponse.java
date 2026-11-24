package org.ds.orderservice.dto;

/**
 * Error body, e.g. { "message": "User not found" }.
 */
public record ErrorResponse(String message) {
}
