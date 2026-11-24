package org.ds.orderservice.exception;

/**
 * user-service could not be reached (stopped, timed out, or failed).
 */
public class UserServiceUnavailableException extends RuntimeException {

    public UserServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
