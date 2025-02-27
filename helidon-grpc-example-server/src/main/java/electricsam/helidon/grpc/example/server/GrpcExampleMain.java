package electricsam.helidon.grpc.example.server;

import electricsam.helidon.grpc.example.server.dagger.DaggerGrpcExampleComponent;
import electricsam.helidon.grpc.example.server.dagger.GrpcExampleComponent;
import electricsam.helidon.grpc.example.server.server.GrpcExampleServer;

public class GrpcExampleMain {

    public static void main(String[] args) {
        GrpcExampleComponent daggerContext = DaggerGrpcExampleComponent.builder().build();
        GrpcExampleServer server = daggerContext.server();
        server.start();
        Runtime.getRuntime().addShutdownHook(Thread.ofVirtual().unstarted(server::stop));
    }

}
