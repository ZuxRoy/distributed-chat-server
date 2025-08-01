package com.chatserver.config;

import com.chatserver.grpc.UserGrpcServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;

@Configuration
public class GrpcConfig {

    @Value("${grpc.server.port:9090}")
    private int grpcPort;

    @Autowired
    private UserGrpcServiceImpl userGrpcServiceImpl;

    private Server server;

    @PostConstruct
    public void startGrpcServer() throws IOException {
        server = ServerBuilder.forPort(grpcPort)
                .addService(userGrpcServiceImpl)
                .build()
                .start();
        
        System.out.println("gRPC server started on port: " + grpcPort);
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            stopGrpcServer();
            System.err.println("*** server shut down");
        }));
    }

    @PreDestroy
    public void stopGrpcServer() {
        if (server != null) {
            server.shutdown();
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
}
