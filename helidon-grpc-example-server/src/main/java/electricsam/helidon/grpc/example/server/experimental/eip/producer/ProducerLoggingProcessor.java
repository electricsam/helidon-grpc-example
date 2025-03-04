package electricsam.helidon.grpc.example.server.experimental.eip.producer;

import electricsam.helidon.grpc.example.proto.ExampleGrpc;
import electricsam.helidon.grpc.example.server.experimental.eip.core.Exchange;
import electricsam.helidon.grpc.example.server.experimental.eip.core.Processor;
import io.grpc.stub.StreamObserver;

import static electricsam.helidon.grpc.example.server.experimental.eip.core.impl.GrpcStreamEndpoint.COMPLETED;
import static electricsam.helidon.grpc.example.server.experimental.eip.core.impl.GrpcStreamEndpoint.RESPONSE_STREAM_OBSERVER;

public class ProducerLoggingProcessor implements Processor {
    @Override
    public void process(Exchange exchange) {
        // TODO add split / filter to RouteDefinition to remove conditional here
        boolean completed = exchange.getProperty(COMPLETED, Boolean.class);
        // TODO create helper to better cast with generics
        StreamObserver<ExampleGrpc.ProducerResponse> producerResponseStream = exchange.getProperty(RESPONSE_STREAM_OBSERVER, StreamObserver.class);
        if (completed) {
            System.out.println("Completed producer response stream");
            producerResponseStream.onCompleted();
        } else {
            ExampleGrpc.ProducerRequest request = exchange.getBody(ExampleGrpc.ProducerRequest.class);
            System.out.println("Received " + request.getMessage());
            String message = "ack-" + request.getMessage();
            producerResponseStream.onNext(ExampleGrpc.ProducerResponse.newBuilder().setMessage(message).build());
        }
    }

}
