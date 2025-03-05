package electricsam.helidon.grpc.example.server.experimental.eip.routes;

import electricsam.helidon.grpc.example.server.experimental.eip.core.Endpoint;
import electricsam.helidon.grpc.example.server.experimental.eip.core.ErrorHandler;
import electricsam.helidon.grpc.example.server.experimental.eip.core.Processor;
import electricsam.helidon.grpc.example.server.experimental.eip.core.RouteBuilder;

public class ServiceRouteBuilder extends RouteBuilder {

    private final Endpoint producerEcho;
    private final Processor logRequest;
    private final Processor setReply;
    private final ErrorHandler producerErrorHandler;
    private final Endpoint consumer;
    private final ErrorHandler consumerErrorHandler;
    private final Processor registerConsumerDisruptor;
    private final Endpoint ringBuffer;
    private final Endpoint producer;

    public ServiceRouteBuilder(
            Endpoint producerEcho,
            Processor logRequest,
            Processor setReply,
            ErrorHandler producerErrorHandler,
            Endpoint consumer,
            ErrorHandler consumerErrorHandler,
            Processor registerConsumerDisruptor,
            Endpoint ringBuffer,
            Endpoint producer
    ) {
        this.producerEcho = producerEcho;
        this.logRequest = logRequest;
        this.setReply = setReply;
        this.producerErrorHandler = producerErrorHandler;
        this.consumer = consumer;
        this.consumerErrorHandler = consumerErrorHandler;
        this.registerConsumerDisruptor = registerConsumerDisruptor;
        this.ringBuffer = ringBuffer;
        this.producer = producer;
    }

    @Override
    public void configure() {
        from(producerEcho)
                .errorHandler(producerErrorHandler)
                .process(logRequest)
                .process(setReply)
                .to(producerEcho);

        from(producer)
                .errorHandler(producerErrorHandler)
                .process(logRequest)
                .process(setReply)
                .to(producer)
                .to(ringBuffer);

        from(consumer)
                .errorHandler(consumerErrorHandler)
                .process(registerConsumerDisruptor);
    }

}
