package electricsam.helidon.grpc.example.server.experimental.eip.producer;

import electricsam.helidon.grpc.example.proto.ExampleGrpc;
import electricsam.helidon.grpc.example.proto.ExampleGrpc.ProducerRequest;
import electricsam.helidon.grpc.example.proto.ExampleGrpc.ProducerResponse;
import electricsam.helidon.grpc.example.server.experimental.eip.core.Exchange;
import electricsam.helidon.grpc.example.server.experimental.eip.core.Processor;

import static electricsam.helidon.grpc.example.server.experimental.eip.module.grpc.GrpcStreamEndpoint.COMPLETED;

public class ProducerSetReplyProcessor implements Processor {

    public static final String PRODUCER_REQUEST = "PRODUCER_REQUEST";

    @Override
    public void process(Exchange exchange) {
        boolean completed = exchange.getProperty(COMPLETED, Boolean.class);
        if (completed) {
            System.out.println("Completed producer response stream");
        } else {
            ProducerRequest request = exchange.getBody(ProducerRequest.class);
            System.out.println("Received " + request.getMessage());
            String message = "ack-" + request.getMessage();
            exchange.setProperty(PRODUCER_REQUEST, request);
            ProducerResponse response = ExampleGrpc.ProducerResponse.newBuilder().setMessage(message).build();
            exchange.setBody(response);
        }
    }

}
