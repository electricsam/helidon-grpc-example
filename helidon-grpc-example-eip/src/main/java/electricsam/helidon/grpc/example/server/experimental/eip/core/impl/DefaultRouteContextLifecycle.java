package electricsam.helidon.grpc.example.server.experimental.eip.core.impl;

import electricsam.helidon.grpc.example.server.experimental.eip.core.Endpoint;
import electricsam.helidon.grpc.example.server.experimental.eip.core.RouteBuilder;
import electricsam.helidon.grpc.example.server.experimental.eip.core.RouteContextLifecycle;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DefaultRouteContextLifecycle implements RouteContextLifecycle {

    private final List<RouteBuilder> routeBuilders;

    public DefaultRouteContextLifecycle(List<RouteBuilder> routeBuilders) {
        this.routeBuilders = List.copyOf(routeBuilders);
    }

    @Override
    public void start() {
        final Set<Endpoint> endpoints = new LinkedHashSet<>();
        routeBuilders.forEach(routeBuilder -> {
            routeBuilder.configure();
            endpoints.addAll(routeBuilder.getEndpoints());
        });
        endpoints.forEach(Endpoint::start);
    }

    @Override
    public void stop() {
        final Set<Endpoint> endpoints = new LinkedHashSet<>();
        routeBuilders.forEach(routeBuilder -> {
            endpoints.addAll(routeBuilder.getEndpoints());
        });
        new ArrayList<>(endpoints).reversed().forEach(Endpoint::stop);
        routeBuilders.reversed().forEach(RouteBuilder::unConfigure);
    }

}
