package electricsam.helidon.grpc.example.server.experimental.eip.core;

import java.util.List;

public interface RouteDefinitionInternal extends RouteDefinition {

    List<Processor> getProcessors();

    ErrorHandler getErrorHandler();
}
