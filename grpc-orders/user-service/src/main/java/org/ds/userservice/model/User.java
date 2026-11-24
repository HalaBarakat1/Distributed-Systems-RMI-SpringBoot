package org.ds.userservice.model;

/**
 * A user managed by user-service.
 */
public record User(long id, String name, String email) {
}
