package electricsam.helidon.grpc.example.server.experimental.eip.routes;

import electricsam.helidon.grpc.example.server.experimental.eip.core.Endpoint;
import electricsam.helidon.grpc.example.server.experimental.eip.core.ErrorHandler;
import io.grpc.stub.StreamObserver;

public class RingBufferRouteBuilderFactoryImpl implements RingBufferRouteBuilderFactory {
    @Override
    public RingBufferRouteBuilder create(
            StreamObserver<?> responseStream,
            Endpoint ringBuffer,
            Endpoint consumer,
            ErrorHandler errorHandler
    ) {
        return new RingBufferRouteBuilder(responseStream, ringBuffer, consumer, errorHandler);
    }
}
