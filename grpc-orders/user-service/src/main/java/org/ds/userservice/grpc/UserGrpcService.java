package org.ds.userservice.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.ds.grpc.user.GetUserRequest;
import org.ds.grpc.user.UserResponse;
import org.ds.grpc.user.UserServiceGrpc;
import org.ds.userservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.grpc.server.service.GrpcService;

/**
 * gRPC server side of the UserService defined in user.proto.
 * Spring Boot registers it with the gRPC server (port 9090).
 */
@GrpcService
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(UserGrpcService.class);

    private final UserRepository userRepository;

    public UserGrpcService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void getUser(GetUserRequest request, StreamObserver<UserResponse> responseObserver) {
        long userId = request.getUserId();
        log.info("gRPC GetUser called for userId={}", userId);

        userRepository.findById(userId).ifPresentOrElse(
                user -> {
                    UserResponse response = UserResponse.newBuilder()
                            .setUserId(user.id())
                            .setName(user.name())
                            .setEmail(user.email())
                            .build();
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                },
                () -> responseObserver.onError(Status.NOT_FOUND
                        .withDescription("User " + userId + " not found")
                        .asRuntimeException()));
    }
}
