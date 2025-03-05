package electricsam.helidon.grpc.example.server.experimental.eip.producer;

import electricsam.helidon.grpc.example.proto.ExampleGrpc;
import electricsam.helidon.grpc.example.proto.ExampleGrpc.ProducerRequest;
import electricsam.helidon.grpc.example.proto.ExampleGrpc.ProducerResponse;
import electricsam.helidon.grpc.example.server.experimental.eip.core.Exchange;
import electricsam.helidon.grpc.example.server.experimental.eip.core.Processor;

import static electricsam.helidon.grpc.example.server.experimental.eip.module.grpc.GrpcStreamEndpoint.COMPLETED;

public class ProducerSetReplyProcessor implements Processor {
    @Override
    public void process(Exchange exchange) {
        boolean completed = exchange.getProperty(COMPLETED, Boolean.class);
        if (!completed) {
            ProducerRequest request = exchange.getBody(ExampleGrpc.ProducerRequest.class);
            ProducerResponse response = ExampleGrpc.ProducerResponse.newBuilder().setMessage("ack-" + request.getMessage()).build();
            exchange.setBody(response);
        }
    }

}
