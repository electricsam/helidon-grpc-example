package electricsam.helidon.grpc.example.server.experimental.eip.consumer;

import electricsam.helidon.grpc.example.proto.ExampleGrpc.ConsumerRegistration;
import electricsam.helidon.grpc.example.server.experimental.eip.core.Endpoint;
import electricsam.helidon.grpc.example.server.experimental.eip.core.ErrorHandler;
import electricsam.helidon.grpc.example.server.experimental.eip.core.Exchange;
import electricsam.helidon.grpc.example.server.experimental.eip.core.Processor;
import electricsam.helidon.grpc.example.server.experimental.eip.routes.RingBufferRouteBuilder;

import java.util.concurrent.ConcurrentHashMap;

import static electricsam.helidon.grpc.example.server.experimental.eip.module.grpc.GrpcStreamEndpoint.RESPONSE_STREAM_OBSERVER_ID;

public class RegisterConsumerDisrupterProcessor implements Processor {

    private final ConcurrentHashMap<String, RingBufferRouteBuilder> dynamicRoutes = new ConcurrentHashMap<>();

    private final Endpoint ringBuffer;
    private final Endpoint consumer;
    private final Processor prepareConsumerResponse;
    private final ErrorHandler ringBufferToConsumerErrorHandler;

    public RegisterConsumerDisrupterProcessor(
            Endpoint ringBuffer,
            Endpoint consumer,
            Processor prepareConsumerResponse,
            ErrorHandler ringBufferToConsumerErrorHandler
    ) {
        this.ringBuffer = ringBuffer;
        this.consumer = consumer;
        this.prepareConsumerResponse = prepareConsumerResponse;
        this.ringBufferToConsumerErrorHandler = ringBufferToConsumerErrorHandler;
    }

    @Override
    public void process(Exchange exchange) {
        ConsumerRegistration registration = exchange.getBody(ConsumerRegistration.class);
        String observerId = exchange.getProperty(RESPONSE_STREAM_OBSERVER_ID, String.class);
        if (registration.getStart()) {
            RingBufferRouteBuilder routeBuilder = new RingBufferRouteBuilder(
                    ringBuffer, consumer, prepareConsumerResponse, ringBufferToConsumerErrorHandler);
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
