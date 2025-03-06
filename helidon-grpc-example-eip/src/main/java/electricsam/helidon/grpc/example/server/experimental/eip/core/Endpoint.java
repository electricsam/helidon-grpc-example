package electricsam.helidon.grpc.example.server.experimental.eip.core;

import electricsam.helidon.grpc.example.server.experimental.eip.core.internal.Processable;

public interface Endpoint extends Processable {

    void removeRouteDefinition(String routeId);

    void addRouteDefinition(EndpointRouteDefinition routeDefinition);

    void start();

    void stop();

}
