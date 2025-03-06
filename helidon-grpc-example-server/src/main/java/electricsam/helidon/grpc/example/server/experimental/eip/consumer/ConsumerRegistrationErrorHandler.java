package electricsam.helidon.grpc.example.server.experimental.eip.consumer;

import electricsam.helidon.grpc.example.server.experimental.eip.core.ErrorHandler;
import electricsam.helidon.grpc.example.server.experimental.eip.core.Exchange;

public class ConsumerRegistrationErrorHandler implements ErrorHandler {


    @Override
    public void handleError(Exception e, Exchange exchange) {
        // TODO implement me
        e.printStackTrace();
    }
}
