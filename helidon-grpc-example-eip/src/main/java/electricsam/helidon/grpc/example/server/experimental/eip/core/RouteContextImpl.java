package electricsam.helidon.grpc.example.server.experimental.eip.core;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class RouteContextImpl implements RouteContext {

    private final List<RouteBuilder> routeBuilders;

    public RouteContextImpl(List<RouteBuilder> routeBuilders) {
        this.routeBuilders = routeBuilders;
    }

    @Override
    public void start() {
        Set<Endpoint> endpoints = new LinkedHashSet<>();
        routeBuilders.forEach(routeBuilder -> {
            routeBuilder.configure();
            endpoints.addAll(routeBuilder.getEndpoints());
        });
        endpoints.forEach(Endpoint::start);
    }

    @Override
    public void stop() {
        Set<Endpoint> endpoints = new LinkedHashSet<>();
        routeBuilders.forEach(routeBuilder -> {
            endpoints.addAll(routeBuilder.getEndpoints());
        });
        new ArrayList<>(endpoints).reversed().forEach(Endpoint::stop);
        routeBuilders.reversed().forEach(RouteBuilder::unConfigure);
    }
}
