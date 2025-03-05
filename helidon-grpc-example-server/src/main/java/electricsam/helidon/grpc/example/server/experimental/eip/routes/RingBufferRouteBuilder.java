package electricsam.helidon.grpc.example.server.experimental.eip.routes;

import electricsam.helidon.grpc.example.server.experimental.eip.core.Endpoint;
import electricsam.helidon.grpc.example.server.experimental.eip.core.ErrorHandler;
import electricsam.helidon.grpc.example.server.experimental.eip.core.Processor;
import electricsam.helidon.grpc.example.server.experimental.eip.core.RouteBuilder;

public class RingBufferRouteBuilder extends RouteBuilder {

    private final Endpoint ringBuffer;
    private final Endpoint consumer;
    private final Processor prepareConsumerResponse;
    private final ErrorHandler errorHandler;

    public RingBufferRouteBuilder(
            Endpoint ringBuffer,
            Endpoint consumer,
            Processor prepareConsumerResponse, ErrorHandler errorHandler
    ) {
        this.ringBuffer = ringBuffer;
        this.consumer = consumer;
        this.prepareConsumerResponse = prepareConsumerResponse;
        this.errorHandler = errorHandler;
    }

    @Override
    public void configure() {
        from(ringBuffer)
                .errorHandler(errorHandler)
                .process(prepareConsumerResponse)
                .to(consumer);
    }
}
