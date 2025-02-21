package electricsam.helidon.grpc.example.server.dagger;

import dagger.Component;
import electricsam.helidon.grpc.example.server.server.GrcpExampleServer;

import javax.inject.Singleton;

@Singleton
@Component(modules = GrpcExampleModule.class)
public interface GrpcExampleComponent {
    GrcpExampleServer server();
}
