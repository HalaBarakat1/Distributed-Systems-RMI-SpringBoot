package org.ds.orderservice.exception;

/**
 * user-service answered NOT_FOUND for the given userId.
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(long userId) {
        super("User not found");
    }
}
