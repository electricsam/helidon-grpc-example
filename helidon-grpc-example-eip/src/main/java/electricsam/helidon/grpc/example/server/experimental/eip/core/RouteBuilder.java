package electricsam.helidon.grpc.example.server.experimental.eip.core;

import java.util.Collection;

public interface RouteBuilder {

    void configure();

    void unConfigure();

    Collection<Endpoint> getEndpoints();
}
