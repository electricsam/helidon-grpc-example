package electricsam.helidon.grpc.example.server.experimental.eip.core;

import electricsam.helidon.grpc.example.server.experimental.eip.core.internal.Processable;

import java.util.List;

public interface EndpointRouteDefinition extends RouteDefinition {

    String getRouteId();
    List<Processable> getProcessors();
    void processExchange(Exchange exchange);
    ErrorHandler getErrorHandler();
}
