package electricsam.helidon.grpc.example.server.experimental.eip.core;

public interface Endpoint extends Processor {

    void setRouteDefinition(RouteDefinitionInternal routeDefinition);
    void start();
    void stop();

}
