package org.ds.orderservice.client;

import java.util.concurrent.TimeUnit;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.ds.grpc.user.GetUserRequest;
import org.ds.grpc.user.UserResponse;
import org.ds.grpc.user.UserServiceGrpc;
import org.ds.orderservice.exception.UserNotFoundException;
import org.ds.orderservice.exception.UserServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * gRPC layer: the only class that talks to user-service.
 * It converts gRPC errors (StatusRuntimeException) into the
 * application's own exceptions so the rest of the code never sees gRPC types.
 */
@Component
public class UserGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(UserGrpcClient.class);

    private final UserServiceGrpc.UserServiceBlockingStub stub;
    private final long timeoutSeconds;

    public UserGrpcClient(UserServiceGrpc.UserServiceBlockingStub stub,
                          @Value("${user-service.timeout-seconds:3}") long timeoutSeconds) {
        this.stub = stub;
        this.timeoutSeconds = timeoutSeconds;
    }

    public UserInfo getUser(long userId) {
        GetUserRequest request = GetUserRequest.newBuilder().setUserId(userId).build();
        try {
            UserResponse response = stub
                    .withDeadlineAfter(timeoutSeconds, TimeUnit.SECONDS)
                    .getUser(request);
            return new UserInfo(response.getUserId(), response.getName(), response.getEmail());
        } catch (StatusRuntimeException ex) {
            Status.Code code = ex.getStatus().getCode();
            log.info("gRPC GetUser({}) failed with {}", userId, code);
            if (code == Status.Code.NOT_FOUND) {
                throw new UserNotFoundException(userId);
            }
            // UNAVAILABLE (service stopped), DEADLINE_EXCEEDED (too slow), or any other failure
            throw new UserServiceUnavailableException("User service is unavailable", ex);
        }
    }
}
