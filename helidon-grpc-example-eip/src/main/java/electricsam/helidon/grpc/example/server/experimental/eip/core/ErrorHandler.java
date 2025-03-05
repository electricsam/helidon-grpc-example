package electricsam.helidon.grpc.example.server.experimental.eip.core;

public interface ErrorHandler {
    void handleError(Throwable t, Exchange exchange);
}
