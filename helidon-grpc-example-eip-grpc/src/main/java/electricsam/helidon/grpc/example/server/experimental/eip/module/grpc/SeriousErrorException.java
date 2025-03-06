package electricsam.helidon.grpc.example.server.experimental.eip.module.grpc;

public class SeriousErrorException extends RuntimeException {
    public SeriousErrorException(Throwable cause) {
        super("A serious error was caught in gRPC data flow.  It is unlikely that this can be handled.", cause);
    }
}
