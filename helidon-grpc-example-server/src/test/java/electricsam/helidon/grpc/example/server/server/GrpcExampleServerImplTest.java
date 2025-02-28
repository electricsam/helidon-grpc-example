package electricsam.helidon.grpc.example.server.server;

import electricsam.helidon.grpc.example.server.dagger.DaggerGrpcExampleComponent;
import electricsam.helidon.grpc.example.server.dagger.GrpcExampleComponent;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GrpcExampleServerImplTest {

    @Test
    void testStartAndStop() throws Exception {
        System.setProperty("port", "0");
        System.setProperty("name", UUID.randomUUID().toString());
        GrpcExampleComponent daggerContext = DaggerGrpcExampleComponent.builder().build();
        GrpcExampleServer server = daggerContext.server();
        try {
            server.start().get();
            int port = server.getPort();
            assertTrue(port > 0);
        } finally {
            server.stop().get();
        }
    }

}