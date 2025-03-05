package electricsam.helidon.grpc.example.server.experimental.eip.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class RouteDefinitionImpl implements RouteDefinitionInternal {

    private final List<Processable> processors = new ArrayList<>();
    private ErrorHandler errorHandler = new DefaultErrorHandler();
    private String routeId = UUID.randomUUID().toString();

    @Override
    public RouteDefinition filter(Predicate<Exchange> predicate) {
        processors.add(new Filter(predicate));
        return this;
    }

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
        return Collections.unmodifiableList(processors);
    }

    @Override
    public void process(Exchange exchange) {
        try {
            for (Processable processable : processors) {
                if (!processable.process(exchange, errorHandler)) {
                    break;
                }
            }
        } catch (Throwable t) {
            errorHandler.handleError(t, exchange);
        }
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }
}
