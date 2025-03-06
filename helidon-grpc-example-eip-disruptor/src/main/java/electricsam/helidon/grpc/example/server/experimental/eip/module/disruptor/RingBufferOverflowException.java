package electricsam.helidon.grpc.example.server.experimental.eip.module.disruptor;

public class RingBufferOverflowException extends RuntimeException {
    public RingBufferOverflowException(String message) {
        super(message);
    }
}
