package electricsam.helidon.grpc.example.server.experimental.eip.producer;

import electricsam.helidon.grpc.example.proto.ExampleGrpc;
import electricsam.helidon.grpc.example.server.experimental.eip.module.grpc.GrpcStreamEndpoint;

import java.util.Collections;

public class ProducerEndpoint extends GrpcStreamEndpoint {
    public ProducerEndpoint() {
        super("EipProducerService", Collections.singletonList(new ProtoConfig("ProduceStream", ExampleGrpc.getDescriptor())));
    }
}
