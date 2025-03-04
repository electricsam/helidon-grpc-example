package electricsam.helidon.grpc.example.server.experimental.eip.producer;

import electricsam.helidon.grpc.example.proto.ExampleGrpc;
import electricsam.helidon.grpc.example.server.experimental.eip.core.impl.GrpcStreamEndpoint;

import java.util.Collections;

public class ProducerEchoEndpoint extends GrpcStreamEndpoint {
    public ProducerEchoEndpoint() {
        super("EipProducerService", Collections.singletonList(new ProtoConfig("ProduceStreamEcho", ExampleGrpc.getDescriptor())));
    }
}
