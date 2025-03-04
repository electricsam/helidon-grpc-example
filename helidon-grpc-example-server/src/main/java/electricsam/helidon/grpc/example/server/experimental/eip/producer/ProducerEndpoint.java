package electricsam.helidon.grpc.example.server.experimental.eip.producer;

import electricsam.helidon.grpc.example.proto.ExampleGrpc;
import electricsam.helidon.grpc.example.server.experimental.eip.core.impl.GrpcStreamEndpoint;

import java.util.Collections;

public class ProducerEndpoint extends GrpcStreamEndpoint {
    public ProducerEndpoint() {
        super("ProducerService", Collections.singletonList(new ProtoConfig("ProduceStream", ExampleGrpc.getDescriptor())));
    }
}
