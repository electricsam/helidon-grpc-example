package electricsam.helidon.grpc.example.server.experimental.eip.core.impl;

import electricsam.helidon.grpc.example.server.experimental.eip.core.Endpoint;
import electricsam.helidon.grpc.example.server.experimental.eip.core.EndpointRouteDefinition;
import electricsam.helidon.grpc.example.server.experimental.eip.core.ErrorHandler;
import electricsam.helidon.grpc.example.server.experimental.eip.core.Exchange;
import electricsam.helidon.grpc.example.server.experimental.eip.core.Processor;
import electricsam.helidon.grpc.example.server.experimental.eip.core.RouteDefinition;
import electricsam.helidon.grpc.example.server.experimental.eip.core.internal.Filter;
import electricsam.helidon.grpc.example.server.experimental.eip.core.internal.Processable;
import electricsam.helidon.grpc.example.server.experimental.eip.core.internal.ProcessorWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class EndpointRouteDefinitionImpl implements EndpointRouteDefinition {

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
    public void processExchange(Exchange exchange) {
        try {
            for (Processable processable : processors) {
                if (!processable.process(exchange, errorHandler)) {
                    break;
                }
            }
        } catch (Exception e) {
            errorHandler.handleError(e, exchange);
        }
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }
}
