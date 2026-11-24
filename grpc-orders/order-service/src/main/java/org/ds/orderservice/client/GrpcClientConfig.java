package org.ds.orderservice.client;

import org.ds.grpc.user.UserServiceGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

/**
 * Creates the gRPC stub used to call user-service.
 * The address comes from spring.grpc.client.channel.user-service.target
 * in application.properties.
 */
@Configuration
public class GrpcClientConfig {

    @Bean
    UserServiceGrpc.UserServiceBlockingStub userServiceStub(GrpcChannelFactory channels) {
        return UserServiceGrpc.newBlockingStub(channels.createChannel("user-service"));
    }
}
