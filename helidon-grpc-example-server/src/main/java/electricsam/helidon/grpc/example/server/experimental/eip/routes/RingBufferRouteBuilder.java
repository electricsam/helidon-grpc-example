package electricsam.helidon.grpc.example.server.experimental.eip.routes;

import electricsam.helidon.grpc.example.server.experimental.eip.core.Endpoint;
import electricsam.helidon.grpc.example.server.experimental.eip.core.ErrorHandler;
import electricsam.helidon.grpc.example.server.experimental.eip.core.RouteBuilder;
import io.grpc.stub.StreamObserver;

import static electricsam.helidon.grpc.example.server.experimental.eip.module.grpc.GrpcStreamEndpoint.COMPLETED;
import static electricsam.helidon.grpc.example.server.experimental.eip.module.grpc.GrpcStreamEndpoint.RESPONSE_STREAM_OBSERVER;

public class RingBufferRouteBuilder extends RouteBuilder {

    private final StreamObserver<?> responseStream;
    private final Endpoint ringBuffer;
    private final Endpoint consumer;
    private final ErrorHandler errorHandler;

    public RingBufferRouteBuilder(
            StreamObserver<?> responseStream,
            Endpoint ringBuffer,
            Endpoint consumer,
            ErrorHandler errorHandler
    ) {
        this.responseStream = responseStream;
        this.ringBuffer = ringBuffer;
        this.consumer = consumer;
        this.errorHandler = errorHandler;
    }

    @Override
    public void configure() {
        from(ringBuffer)
                .errorHandler(errorHandler)
                .process(exchange -> {
                    exchange.setProperty(RESPONSE_STREAM_OBSERVER, responseStream);
                    exchange.setProperty(COMPLETED, false);
                })
                .to(consumer);
    }
}
