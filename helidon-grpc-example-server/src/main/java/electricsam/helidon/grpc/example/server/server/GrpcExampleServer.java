package electricsam.helidon.grpc.example.server.server;

import io.helidon.grpc.server.GrpcServer;

import java.util.concurrent.CompletableFuture;

public interface GrpcExampleServer {

    CompletableFuture<GrpcServer> start();
    CompletableFuture<GrpcServer> stop();
    int getPort();
}
