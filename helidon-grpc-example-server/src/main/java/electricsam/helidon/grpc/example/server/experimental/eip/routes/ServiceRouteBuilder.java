package electricsam.helidon.grpc.example.server.experimental.eip.routes;

import electricsam.helidon.grpc.example.server.experimental.eip.core.*;

import java.util.function.Predicate;

public class ServiceRouteBuilder extends RouteBuilder {

    private final Endpoint producerEcho;
    private final Processor setProducerReply;
    private final ErrorHandler producerErrorHandler;
    private final Endpoint consumer;
    private final ErrorHandler consumerErrorHandler;
    private final Processor registerConsumerDisruptor;
    private final Endpoint ringBuffer;
    private final Endpoint producer;
    private final Processor prepareConsumerResponse;
    private final Predicate<Exchange> notCompleted;

    public ServiceRouteBuilder(
            Endpoint producerEcho,
            Processor setProducerReply,
            ErrorHandler producerErrorHandler,
            Endpoint consumer,
            ErrorHandler consumerErrorHandler,
            Processor registerConsumerDisruptor,
            Endpoint ringBuffer,
            Endpoint producer,
            Processor prepareConsumerResponse,
            Predicate<Exchange> notCompleted
    ) {
        this.producerEcho = producerEcho;
        this.setProducerReply = setProducerReply;
        this.producerErrorHandler = producerErrorHandler;
        this.consumer = consumer;
        this.consumerErrorHandler = consumerErrorHandler;
        this.registerConsumerDisruptor = registerConsumerDisruptor;
        this.ringBuffer = ringBuffer;
        this.producer = producer;
        this.prepareConsumerResponse = prepareConsumerResponse;
        this.notCompleted = notCompleted;
    }

    @Override
    public void configure() {
        from(producerEcho)
                .errorHandler(producerErrorHandler)
                .process(setProducerReply)
                .to(producerEcho);

        from(producer)
                .errorHandler(producerErrorHandler)
                .process(setProducerReply)
                .to(producer)
                .filter(notCompleted)
                .process(prepareConsumerResponse)
                .to(ringBuffer);

        from(consumer)
                .errorHandler(consumerErrorHandler)
                .process(registerConsumerDisruptor);
    }

}
