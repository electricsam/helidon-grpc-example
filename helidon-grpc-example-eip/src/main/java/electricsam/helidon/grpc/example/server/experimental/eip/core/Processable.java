package electricsam.helidon.grpc.example.server.experimental.eip.core;

public interface Processable {

    boolean process(Exchange exchange, ErrorHandler errorHandler);
}
