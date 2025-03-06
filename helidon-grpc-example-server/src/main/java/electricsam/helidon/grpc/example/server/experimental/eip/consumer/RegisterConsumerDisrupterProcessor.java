package electricsam.helidon.grpc.example.server.experimental.eip.consumer;

import electricsam.helidon.grpc.example.proto.ExampleGrpc.ConsumerRegistration;
import electricsam.helidon.grpc.example.server.experimental.eip.core.Endpoint;
import electricsam.helidon.grpc.example.server.experimental.eip.core.ErrorHandler;
import electricsam.helidon.grpc.example.server.experimental.eip.core.Exchange;
import electricsam.helidon.grpc.example.server.experimental.eip.core.Processor;
import electricsam.helidon.grpc.example.server.experimental.eip.routes.RingBufferRouteBuilder;
import electricsam.helidon.grpc.example.server.experimental.eip.routes.RingBufferRouteBuilderFactory;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.ConcurrentHashMap;

import static electricsam.helidon.grpc.example.server.experimental.eip.module.grpc.GrpcStreamEndpoint.RESPONSE_STREAM_OBSERVER;
import static electricsam.helidon.grpc.example.server.experimental.eip.module.grpc.GrpcStreamEndpoint.RESPONSE_STREAM_OBSERVER_ID;

public class RegisterConsumerDisrupterProcessor implements Processor {

    private final ConcurrentHashMap<String, RingBufferRouteBuilder> dynamicRoutes = new ConcurrentHashMap<>();

    private final Endpoint ringBuffer;
    private final Endpoint consumer;
    private final ErrorHandler ringBufferToConsumerErrorHandler;
    private final RingBufferRouteBuilderFactory ringBufferRouteBuilderFactory;

    public RegisterConsumerDisrupterProcessor(
            Endpoint ringBuffer,
            Endpoint consumer,
            ErrorHandler ringBufferToConsumerErrorHandler, RingBufferRouteBuilderFactory ringBufferRouteBuilderFactory
    ) {
        this.ringBuffer = ringBuffer;
        this.consumer = consumer;
        this.ringBufferToConsumerErrorHandler = ringBufferToConsumerErrorHandler;
        this.ringBufferRouteBuilderFactory = ringBufferRouteBuilderFactory;
    }

    @Override
    public void process(Exchange exchange) {
        ConsumerRegistration registration = exchange.getBody(ConsumerRegistration.class);
        String observerId = exchange.getProperty(RESPONSE_STREAM_OBSERVER_ID, String.class);
        StreamObserver responseStream = exchange.getProperty(RESPONSE_STREAM_OBSERVER, StreamObserver.class);
        if (registration.getStart()) {
            RingBufferRouteBuilder routeBuilder = ringBufferRouteBuilderFactory
                    .create(responseStream, ringBuffer, consumer, ringBufferToConsumerErrorHandler);
            dynamicRoutes.put(observerId, routeBuilder);
            routeBuilder.configure();
        } else {
            RingBufferRouteBuilder routeBuilder = dynamicRoutes.remove(observerId);;
            if (routeBuilder != null) {
                routeBuilder.unConfigure();
            }
        }
    }
}
