package electricsam.helidon.grpc.example.server.experimental.eip.routes;

import electricsam.helidon.grpc.example.server.experimental.eip.core.Endpoint;
import electricsam.helidon.grpc.example.server.experimental.eip.core.ErrorHandler;

public class RingBufferRouteBuilderFactoryImpl implements RingBufferRouteBuilderFactory {
    @Override
    public RingBufferRouteBuilder create(
            String responseStreamId,
            Endpoint ringBuffer,
            Endpoint consumer,
            ErrorHandler errorHandler
    ) {
        return new RingBufferRouteBuilder(responseStreamId, ringBuffer, consumer, errorHandler);
    }
}
