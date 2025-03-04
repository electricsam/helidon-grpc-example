package electricsam.helidon.grpc.example.server.experimental.eip.core;

import java.util.ArrayList;
import java.util.List;

public abstract class RouteBuilder {

    private final List<RouteDefinitionInternal> routes = new ArrayList<>();
    protected final List<Endpoint> endpoints = new ArrayList<>();

    protected abstract void configure();

    protected RouteDefinition from(Endpoint endpoint) {
        endpoints.add(endpoint);
        RouteDefinitionInternal routeDefinition = new RouteDefinitionImpl();
        endpoint.addRouteDefinition(routeDefinition);
        routes.add(routeDefinition);
        return routeDefinition;
    }

    public void start() {
        configure();
        endpoints.forEach(Endpoint::start);
    }

    public void stop() {
        endpoints.reversed().forEach(Endpoint::stop);
    }

}
