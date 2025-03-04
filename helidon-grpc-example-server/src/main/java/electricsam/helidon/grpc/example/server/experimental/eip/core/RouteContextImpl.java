package electricsam.helidon.grpc.example.server.experimental.eip.core;

import java.util.List;

public class RouteContextImpl implements RouteContext {

    private final List<RouteBuilder> routeBuilders;

    public RouteContextImpl(List<RouteBuilder> routeBuilders) {
        this.routeBuilders = routeBuilders;
    }

    @Override
    public void start() {
        routeBuilders.forEach(RouteBuilder::start);
    }

    @Override
    public void stop() {
        routeBuilders.reversed().forEach(RouteBuilder::stop);
    }
}
