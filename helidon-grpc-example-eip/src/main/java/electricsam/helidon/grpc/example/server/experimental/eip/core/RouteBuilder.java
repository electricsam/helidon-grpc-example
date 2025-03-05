package electricsam.helidon.grpc.example.server.experimental.eip.core;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class RouteBuilder {

    protected final Map<Endpoint, RouteDefinitionInternal> routeDefinitions = new HashMap<>();

    public abstract void configure();

    protected RouteDefinition from(Endpoint endpoint) {
        RouteDefinitionInternal routeDefinition = new RouteDefinitionImpl();
        endpoint.addRouteDefinition(routeDefinition);
        routeDefinitions.put(endpoint, routeDefinition);
        return routeDefinition;
    }

    public void unConfigure() {
        routeDefinitions.forEach((endpoint, routeDefinition) -> endpoint.removeRouteDefinition(routeDefinition.getRouteId()));
    }

    public Set<Endpoint> getEndpoints() {
        return Collections.unmodifiableSet(routeDefinitions.keySet());
    }

}
