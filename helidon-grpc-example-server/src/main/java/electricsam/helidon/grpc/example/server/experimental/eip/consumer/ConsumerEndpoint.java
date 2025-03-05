package electricsam.helidon.grpc.example.server.experimental.eip.consumer;

import electricsam.helidon.grpc.example.proto.ExampleGrpc;
import electricsam.helidon.grpc.example.server.experimental.eip.module.grpc.GrpcStreamEndpoint;

import java.util.Collections;

public class ConsumerEndpoint extends GrpcStreamEndpoint {
    public ConsumerEndpoint() {
        super("EipConsumerService", Collections.singletonList(new ProtoConfig("RegisterConsumer", ExampleGrpc.getDescriptor())));
    }
}
