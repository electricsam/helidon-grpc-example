package electricsam.helidon.grpc.example.server.server;

import electricsam.helidon.grpc.example.server.dagger.DaggerGrpcExampleComponent;
import electricsam.helidon.grpc.example.server.dagger.GrpcExampleComponent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GrpcExampleServerImplTest {

    @Test
    void testStartAndStop() throws Exception {
        System.setProperty("port", "0");
        GrpcExampleComponent daggerContext = DaggerGrpcExampleComponent.builder().build();
        GrpcExampleServer server = daggerContext.server();
        server.start().get();
        int port = server.getPort();
        assertTrue(port > 0);
        server.stop().get();
    }

}