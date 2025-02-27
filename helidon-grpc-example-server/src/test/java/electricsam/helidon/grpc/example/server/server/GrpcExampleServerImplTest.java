package electricsam.helidon.grpc.example.server.server;

import electricsam.helidon.grpc.example.server.dagger.DaggerGrpcExampleComponent;
import electricsam.helidon.grpc.example.server.dagger.GrpcExampleComponent;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

class GrpcExampleServerImplTest {

    @Test
    void testStartAndStop() throws Exception {
        final AtomicReference<GrpcExampleServer> serverRef = new AtomicReference<>();
        Thread.ofVirtual().start(() -> {
            GrpcExampleComponent daggerContext = DaggerGrpcExampleComponent.builder().build();
            GrpcExampleServer server = daggerContext.server();
            server.start();
            serverRef.set(server);
        });
        try {
            //TODO add tests
            Thread.sleep(500);
        } finally {
            GrpcExampleServer server = serverRef.get();
            if (server != null) {
                server.stop();
            }
        }
    }

}