package electricsam.helidon.grpc.example.server.experimental.eip.core.internal;

import electricsam.helidon.grpc.example.server.experimental.eip.core.ErrorHandler;
import electricsam.helidon.grpc.example.server.experimental.eip.core.Exchange;

public interface Processable {

    boolean process(Exchange exchange, ErrorHandler errorHandler);
}
