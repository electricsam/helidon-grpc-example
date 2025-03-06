package electricsam.helidon.grpc.example.server.experimental.eip.core.impl;

import electricsam.helidon.grpc.example.server.experimental.eip.core.ErrorHandler;
import electricsam.helidon.grpc.example.server.experimental.eip.core.Exchange;

public class DefaultErrorHandler implements ErrorHandler {
    @Override
    public void handleError(Exception exception, Exchange exchange) {
        // TODO use logging
        exception.printStackTrace();
    }
}
