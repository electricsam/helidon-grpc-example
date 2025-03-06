package electricsam.helidon.grpc.example.server.experimental.eip.core.impl;


import electricsam.helidon.grpc.example.server.experimental.eip.core.Endpoint;
import electricsam.helidon.grpc.example.server.experimental.eip.core.EndpointRouteDefinition;
import electricsam.helidon.grpc.example.server.experimental.eip.core.RouteBuilder;
import electricsam.helidon.grpc.example.server.experimental.eip.core.RouteDefinition;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class RouteBuilderBase implements RouteBuilder {

    private final Map<Endpoint, EndpointRouteDefinition> routeDefinitions = new HashMap<>();

    protected RouteDefinition from(Endpoint endpoint) {
        EndpointRouteDefinition routeDefinition = new EndpointRouteDefinitionImpl();
        endpoint.addRouteDefinition(routeDefinition);
        routeDefinitions.put(endpoint, routeDefinition);
        return routeDefinition;
    }

    public void unConfigure() {
        routeDefinitions.forEach((endpoint, routeDefinition) -> endpoint.removeRouteDefinition(routeDefinition.getRouteId()));
    }

    public Collection<Endpoint> getEndpoints() {
        return Collections.unmodifiableSet(routeDefinitions.keySet());
    }

}
