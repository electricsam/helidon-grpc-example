package electricsam.helidon.grpc.example.server.experimental.eip.routes;

import electricsam.helidon.grpc.example.server.experimental.eip.core.ErrorHandler;
import electricsam.helidon.grpc.example.server.experimental.eip.core.Processor;
import electricsam.helidon.grpc.example.server.experimental.eip.core.RouteBuilder;
import electricsam.helidon.grpc.example.server.experimental.eip.core.impl.DisruptorRingBufferEndpoint;
import electricsam.helidon.grpc.example.server.experimental.eip.producer.ProducerEndpoint;

public class ServiceRouteBuilder extends RouteBuilder {

    private final ProducerEndpoint producerEndpoint;
    private final Processor producerReplyProcessor;
    private final ErrorHandler producerRouteErrorHandler;
    private final DisruptorRingBufferEndpoint disruptorRingBufferEndpoint;

    public ServiceRouteBuilder(
            ProducerEndpoint producerEndpoint,
            Processor producerReplyProcessor,
            ErrorHandler producerRouteErrorHandler, DisruptorRingBufferEndpoint disruptorRingBufferEndpoint) {
        this.producerEndpoint = producerEndpoint;
        this.producerReplyProcessor = producerReplyProcessor;
        this.producerRouteErrorHandler = producerRouteErrorHandler;
        this.disruptorRingBufferEndpoint = disruptorRingBufferEndpoint;
    }

    @Override
    protected void configure() {
        from(producerEndpoint)
                .errorHandler(producerRouteErrorHandler)
                .process(producerReplyProcessor)
                .to(disruptorRingBufferEndpoint);

        from(consumerRegistrationEndpoint)
                .errorHandler(consumerRegistrationErrorHandler)
                .process(consumerSubscriptionProcessor);

        from(disruptorRingBufferEndpoint)
                .errorHandler(consumerStreamErrorHandler)
                .process(consumerStreamingProcessor);
    }
}
