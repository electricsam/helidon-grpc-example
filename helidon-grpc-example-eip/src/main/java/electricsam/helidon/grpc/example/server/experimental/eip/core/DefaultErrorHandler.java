package electricsam.helidon.grpc.example.server.experimental.eip.core;

public class DefaultErrorHandler implements ErrorHandler {
    @Override
    public void handleError(Throwable t, Exchange exchange) {
        // TODO use logging
        t.printStackTrace();
    }
}
