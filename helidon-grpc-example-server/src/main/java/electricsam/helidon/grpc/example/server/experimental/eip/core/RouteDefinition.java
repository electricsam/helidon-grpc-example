package electricsam.helidon.grpc.example.server.experimental.eip.core;

public interface RouteDefinition {

    RouteDefinition process(Processor processor);
    RouteDefinition to(Endpoint endpoint);
    RouteDefinition errorHandler(ErrorHandler errorHandler);
}
