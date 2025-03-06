package electricsam.helidon.grpc.example.server.experimental.eip.core;

import java.util.function.Predicate;

public interface RouteDefinition {

    RouteDefinition filter(Predicate<Exchange> predicate);

    RouteDefinition process(Processor processor);

    RouteDefinition to(Endpoint endpoint);

    RouteDefinition errorHandler(ErrorHandler errorHandler);

    RouteDefinition routeId(String routeId);
}
