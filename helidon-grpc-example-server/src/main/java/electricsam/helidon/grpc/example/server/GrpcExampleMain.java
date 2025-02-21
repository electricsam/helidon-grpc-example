package electricsam.helidon.grpc.example.server;

import electricsam.helidon.grpc.example.server.dagger.DaggerGrpcExampleComponent;
import electricsam.helidon.grpc.example.server.dagger.GrpcExampleComponent;

public class GrpcExampleMain {

    public static void main(String[] args) {
        GrpcExampleComponent daggerContext = DaggerGrpcExampleComponent.builder().build();
        daggerContext.server().start();
    }

}
