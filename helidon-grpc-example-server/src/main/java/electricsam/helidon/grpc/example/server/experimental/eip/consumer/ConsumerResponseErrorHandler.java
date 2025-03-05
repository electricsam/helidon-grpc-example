package electricsam.helidon.grpc.example.server.experimental.eip.consumer;

import electricsam.helidon.grpc.example.server.experimental.eip.core.ErrorHandler;
import electricsam.helidon.grpc.example.server.experimental.eip.core.Exchange;

public class ConsumerResponseErrorHandler implements ErrorHandler {


    @Override
    public void handleError(Throwable t, Exchange exchange) {
        // TODO implement me
        t.printStackTrace();
    }
}
