package electricsam.helidon.grpc.example.server.experimental.eip.core.impl;

public class RingBufferOverflowException extends RuntimeException {
    public RingBufferOverflowException(String message) {
        super(message);
    }
}
