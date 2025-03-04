package electricsam.helidon.grpc.example.server.experimental.eip.core;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RouteDefinitionImpl implements RouteDefinitionInternal {

    private final List<Processable> processors = new ArrayList<>();
    private ErrorHandler errorHandler = new DefaultErrorHandler();
    private String routeId = UUID.randomUUID().toString();

    @Override
    public RouteDefinition process(Processor processor) {
        processors.add(new ProcessorWrapper(processor));
        return this;
    }

    @Override
    public RouteDefinition to(Endpoint endpoint) {
        processors.add(endpoint);
        return this;
    }

    @Override
    public RouteDefinition errorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        return this;
    }

    @Override
    public RouteDefinition routeId(String routeId) {
        this.routeId = routeId;
        return this;
    }

    @Override
    public String getRouteId() {
        return routeId;
    }

    @Override
    public List<Processable> getProcessors() {
        return processors;
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }
}
