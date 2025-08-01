package com.chatserver.grpc;

import com.chatserver.model.User;
import com.chatserver.service.UserService;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class UserGrpcServiceImpl extends UserServiceGrpc.UserServiceImplBase {
    
    @Autowired
    private UserService userService;

    @Override
    public void getUser(UserRequest request, StreamObserver<UserResponse> responseObserver) {
        try {
            Optional<User> userOpt = userService.findByUsername(request.getUsername());
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                UserResponse response = UserResponse.newBuilder()
                    .setUsername(user.getUsername())
                    .setIsOnline(user.isOnline())
                    .setCreatedAt(user.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .setLastSeen(user.getLastSeen().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .build();
                
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(
                    io.grpc.Status.NOT_FOUND
                        .withDescription("User not found: " + request.getUsername())
                        .asRuntimeException()
                );
            }
        } catch (Exception e) {
            responseObserver.onError(
                io.grpc.Status.INTERNAL
                    .withDescription("Internal server error: " + e.getMessage())
                    .asRuntimeException()
            );
        }
    }

    @Override
    public void registerUser(RegisterUserRequest request, StreamObserver<UserResponse> responseObserver) {
        try {
            User user = userService.registerUser(request.getUsername());
            
            UserResponse response = UserResponse.newBuilder()
                .setUsername(user.getUsername())
                .setIsOnline(user.isOnline())
                .setCreatedAt(user.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .setLastSeen(user.getLastSeen().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            responseObserver.onError(
                io.grpc.Status.ALREADY_EXISTS
                    .withDescription(e.getMessage())
                    .asRuntimeException()
            );
        }
    }

    @Override
    public void updateUserStatus(UpdateStatusRequest request, StreamObserver<StatusResponse> responseObserver) {
        try {
            userService.updateUserOnlineStatus(request.getUsername(), request.getIsOnline());
            
            StatusResponse response = StatusResponse.newBuilder()
                .setSuccess(true)
                .setMessage("User status updated successfully")
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            StatusResponse response = StatusResponse.newBuilder()
                .setSuccess(false)
                .setMessage("Failed to update user status: " + e.getMessage())
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
