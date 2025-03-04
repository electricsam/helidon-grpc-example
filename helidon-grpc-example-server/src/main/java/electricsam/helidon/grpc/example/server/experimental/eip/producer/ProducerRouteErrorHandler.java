package electricsam.helidon.grpc.example.server.experimental.eip.producer;

import electricsam.helidon.grpc.example.proto.ExampleGrpc;
import electricsam.helidon.grpc.example.server.experimental.eip.core.ErrorHandler;
import electricsam.helidon.grpc.example.server.experimental.eip.core.Exchange;
import io.grpc.stub.StreamObserver;

import static electricsam.helidon.grpc.example.server.experimental.eip.core.impl.GrpcStreamEndpoint.RESPONSE_STREAM_OBSERVER;

public class ProducerRouteErrorHandler implements ErrorHandler {
    @Override
    public void handleError(Throwable t, Exchange exchange) {
        // TODO create helper to better cast with generics
        StreamObserver<ExampleGrpc.ProducerResponse> producerResponseStream = exchange.getProperty(RESPONSE_STREAM_OBSERVER, StreamObserver.class);
        t.printStackTrace();
        producerResponseStream.onCompleted();
    }
}
