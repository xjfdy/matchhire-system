package com.matchhire.authservice.grpc;

import com.matchhire.authservice.model.User;
import com.matchhire.authservice.repository.UserRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.UUID;

@GrpcService
public class UserGrpcServiceImpl extends UserGrpcServiceGrpc.UserGrpcServiceImplBase {

    private final UserRepository userRepository;

    public UserGrpcServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void getUserById(UserRequest request, StreamObserver<UserResponse> responseObserver) {
        try {
            UUID id = UUID.fromString(request.getId());
            User user = userRepository.findById(id)
                    .orElseThrow(() -> Status.NOT_FOUND
                            .withDescription("User not found with ID: " + id)
                            .asRuntimeException());

            UserResponse response = UserResponse.newBuilder()
                    .setId(user.getId().toString())
                    .setFirstName(user.getFirstName())
                    .setLastName(user.getLastName())
                    .addAllSkills(user.getSkills() != null ? user.getSkills() : java.util.List.of())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Invalid UUID format")
                    .asRuntimeException());
        }
    }
}