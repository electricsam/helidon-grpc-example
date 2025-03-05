package electricsam.helidon.grpc.example.server.experimental.eip.core;

import java.util.List;

public interface RouteDefinitionInternal extends RouteDefinition {

    String getRouteId();
    List<Processable> getProcessors();
    void process(Exchange exchange);
    ErrorHandler getErrorHandler();
}
