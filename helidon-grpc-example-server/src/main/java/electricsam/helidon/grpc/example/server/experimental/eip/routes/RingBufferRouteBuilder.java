package electricsam.helidon.grpc.example.server.experimental.eip.routes;

import electricsam.helidon.grpc.example.server.experimental.eip.core.Endpoint;
import electricsam.helidon.grpc.example.server.experimental.eip.core.ErrorHandler;
import electricsam.helidon.grpc.example.server.experimental.eip.core.RouteBuilder;

import static electricsam.helidon.grpc.example.server.experimental.eip.module.grpc.GrpcStreamEndpoint.COMPLETED;
import static electricsam.helidon.grpc.example.server.experimental.eip.module.grpc.GrpcStreamEndpoint.RESPONSE_STREAM_OBSERVER_ID;

public class RingBufferRouteBuilder extends RouteBuilder {

    private final String responseStreamId;
    private final Endpoint ringBuffer;
    private final Endpoint consumer;
    private final ErrorHandler errorHandler;

    public RingBufferRouteBuilder(
            String responseStreamId,
            Endpoint ringBuffer,
            Endpoint consumer,
            ErrorHandler errorHandler
    ) {
        this.responseStreamId = responseStreamId;
        this.ringBuffer = ringBuffer;
        this.consumer = consumer;
        this.errorHandler = errorHandler;
    }

    @Override
    public void configure() {
        from(ringBuffer)
                .errorHandler(errorHandler)
                .process(exchange -> {
                    exchange.setProperty(RESPONSE_STREAM_OBSERVER_ID, responseStreamId);
                    exchange.setProperty(COMPLETED, false);
                })
                .to(consumer);
    }
}
