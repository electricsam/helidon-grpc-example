package electricsam.helidon.grpc.example.server.experimental.eip.core;

public interface Endpoint extends Processable {

    void removeRouteDefinition(String routeId);
    void addRouteDefinition(RouteDefinitionInternal routeDefinition);
    void start();
    void stop();

}
