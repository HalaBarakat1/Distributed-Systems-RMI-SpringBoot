package org.ds.orderservice.client;

/**
 * User data returned by user-service, independent of the generated gRPC classes.
 */
public record UserInfo(long id, String name, String email) {
}
