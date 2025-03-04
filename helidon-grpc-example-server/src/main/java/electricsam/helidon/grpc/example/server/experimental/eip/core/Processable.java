package electricsam.helidon.grpc.example.server.experimental.eip.core;

public interface Processable {

    void process(Exchange exchange, RouteDefinitionInternal routeDefinition);
}
