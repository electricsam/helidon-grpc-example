package electricsam.helidon.grpc.example.server.experimental.eip.producer;

import electricsam.helidon.grpc.example.server.experimental.eip.core.impl.DisruptorRingBufferEndpoint;

public class ProducerRingBufferEndpoint extends DisruptorRingBufferEndpoint {
    public ProducerRingBufferEndpoint() {
        super(1024, false);
    }
}
