package electricsam.helidon.grpc.example.server.experimental.eip.producer;

import electricsam.helidon.grpc.example.server.experimental.eip.module.disruptor.DisruptorRingBufferEndpoint;

public class ProducerRingBufferEndpoint extends DisruptorRingBufferEndpoint {
    public ProducerRingBufferEndpoint() {
        super(1024);
    }
}
