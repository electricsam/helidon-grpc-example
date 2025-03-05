package electricsam.helidon.grpc.example.server.experimental.eip.core;

public interface ProducerTemplate {
    void sendAsync(Exchange exchange, Endpoint endpoint, ErrorHandler errorHandler);
}
