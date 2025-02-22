package electricsam.helidon.grpc.example.server.tubes.module.grcp;

import electricsam.helidon.grpc.example.proto.ExampleGrpc;
import electricsam.helidon.grpc.example.proto.ExampleGrpc.ProducerRequest;
import electricsam.helidon.grpc.example.proto.ExampleGrpc.ProducerResponse;
import electricsam.helidon.grpc.example.server.producer.ProducerService;
import electricsam.helidon.grpc.example.server.tubes.core.TubeSink;
import io.grpc.stub.StreamObserver;
import io.helidon.grpc.server.ServiceDescriptor.Rules;

public class ProducerServiceImpl2 extends GrcpSource<ProducerRequest, ProducerResponse> implements ProducerService {

    private static final String SERVICE_NAME = "ProducerService";
    private static final String PRODUCE_STREAM_METHOD_NAME = "ProduceStream";

    @Override
    public String name() {
        return SERVICE_NAME;
    }

    @Override
    public void update(Rules rules) {
        rules.proto(ExampleGrpc.getDescriptor()).bidirectional(PRODUCE_STREAM_METHOD_NAME, this::bidirectional);
    }

    private StreamObserver<ProducerRequest> bidirectional(StreamObserver<ProducerResponse> observer) {
        return this.bidi(PRODUCE_STREAM_METHOD_NAME, observer);
    }

}
