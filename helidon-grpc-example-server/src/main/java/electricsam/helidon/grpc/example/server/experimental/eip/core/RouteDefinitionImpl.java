package electricsam.helidon.grpc.example.server.experimental.eip.core;

import java.util.ArrayList;
import java.util.List;

public class RouteDefinitionImpl implements RouteDefinitionInternal {

    private final List<Processor> processors = new ArrayList<>();
    private ErrorHandler errorHandler = new DefaultErrorHandler();
    private final Endpoint from;

    RouteDefinitionImpl(Endpoint from) {
        this.from = from;
    }

    @Override
    public RouteDefinition process(Processor processor) {
        processors.add(processor);
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
    public List<Processor> getProcessors() {
        return processors;
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }
}
