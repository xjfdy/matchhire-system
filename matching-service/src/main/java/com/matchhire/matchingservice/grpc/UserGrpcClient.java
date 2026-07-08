package com.matchhire.matchingservice.grpc;

import com.matchhire.authservice.grpc.UserGrpcServiceGrpc;
import com.matchhire.authservice.grpc.UserRequest;
import com.matchhire.authservice.grpc.UserResponse;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

@Component
public class UserGrpcClient {

    @GrpcClient("auth-service")
    private UserGrpcServiceGrpc.UserGrpcServiceBlockingStub userGrpcStub;

    public UserResponse getUserById(String userId) {
        try {
            UserRequest request = UserRequest.newBuilder()
                    .setId(userId)
                    .build();
            return userGrpcStub.getUserById(request);
        } catch (StatusRuntimeException e) {
            System.out.println("gRPC call failed with status: " + e.getStatus());
            throw new RuntimeException("User not found with ID: " + userId);
        }
    }
}

