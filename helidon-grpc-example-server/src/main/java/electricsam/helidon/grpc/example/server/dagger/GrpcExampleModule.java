package electricsam.helidon.grpc.example.server.dagger;

import dagger.Module;
import dagger.Provides;
import electricsam.helidon.grpc.example.server.consumer.ConsumerService;
import electricsam.helidon.grpc.example.server.consumer.DisruptorConsumerService;
import electricsam.helidon.grpc.example.server.experimental.eip.consumer.*;
import electricsam.helidon.grpc.example.server.experimental.eip.core.*;
import electricsam.helidon.grpc.example.server.experimental.eip.module.disruptor.DisruptorRingBufferEndpoint;
import electricsam.helidon.grpc.example.server.experimental.eip.producer.*;
import electricsam.helidon.grpc.example.server.experimental.eip.routes.ServiceRouteBuilder;
import electricsam.helidon.grpc.example.server.producer.ProducerService;
import electricsam.helidon.grpc.example.server.producer.ProducerServiceImpl;
import electricsam.helidon.grpc.example.server.server.GrpcExampleServer;
import electricsam.helidon.grpc.example.server.server.GrpcExampleServerImpl;

import javax.inject.Singleton;
import java.util.Collections;

@Module
public interface GrpcExampleModule {

    @Provides
    @Singleton
    static ConsumerService consumerService() {
        return new DisruptorConsumerService();
    }

    @Provides
    @Singleton
    static ProducerService producerService(ConsumerService consumerService) {
        return new ProducerServiceImpl(consumerService);
    }

    @Provides
    @Singleton
    static GrpcExampleServer grpcExampleServer(
            ConsumerService consumerService,
            ProducerService producerService,
            ProducerEchoEndpoint expermientalProducerEchoEndpoint,
            RouteContext experimentalRouteContext
    ) {
        return new GrpcExampleServerImpl(
                producerService,
                consumerService,
                expermientalProducerEchoEndpoint,
                experimentalRouteContext
        );
    }

    // TODO below this is experimental
    @Provides
    @Singleton
    static ProducerEchoEndpoint experimentalProducerEchoEndpoint() {
        return new ProducerEchoEndpoint();
    }

    @Provides
    @Singleton
    static ConsumerEndpoint experimentalConsumerEndpoint() {
        return new ConsumerEndpoint();
    }

    @Provides
    @Singleton
    static ProducerLoggingProcessor experimentalProducerLoggingProcessor() {
        return new ProducerLoggingProcessor();
    }

    @Provides
    @Singleton
    static ProducerSetReplyProcessor experimentalProducerSetReplyProcessor() {
        return new ProducerSetReplyProcessor();
    }

    @Provides
    @Singleton
    static ProducerTemplate experimentalProducerTemplate() {
        return new ProducerTemplateImpl();
    }

    @Provides
    @Singleton
    static ProducerRouteErrorHandler experimentalProducerRouteErrorHandler(ProducerEchoEndpoint endpoint, ProducerTemplate producerTemplate) {
        return new ProducerRouteErrorHandler(endpoint, producerTemplate);
    }

    @Provides
    @Singleton
    static ConsumerRegistrationErrorHandler experimentalConsumerRegistrationErrorHandler() {
        return new ConsumerRegistrationErrorHandler();
    }

    @Provides
    @Singleton
    static DisruptorRingBufferEndpoint experimentalDisruptorRingBufferEndpoint() {
        return new DisruptorRingBufferEndpoint(1024);
    }

    @Provides
    @Singleton
    static PrepareConsumerResponseProcessor experimentalPrepareConsumerResponseProcessor() {
        return new PrepareConsumerResponseProcessor();
    }

    @Provides
    @Singleton
    static ConsumerResponseErrorHandler experimentalConsumerResponseErrorHandler() {
        return new ConsumerResponseErrorHandler();
    }

    @Provides
    @Singleton
    static RegisterConsumerDisrupterProcessor experimentalRegisterConsumerDisrupterProcessor(
            DisruptorRingBufferEndpoint ringBufferEndpoint,
            ConsumerEndpoint consumerEndpoint,
            PrepareConsumerResponseProcessor prepareConsumerResponseProcessor,
            ConsumerResponseErrorHandler consumerResponseErrorHandler
    ) {
        return new RegisterConsumerDisrupterProcessor(
                ringBufferEndpoint,
                consumerEndpoint,
                prepareConsumerResponseProcessor,
                consumerResponseErrorHandler);
    }

    @Provides
    @Singleton
    static ProducerEndpoint experimentalProducerEndpoint() {
        return new ProducerEndpoint();
    }

    @Provides
    @Singleton
    static RouteBuilder experimentalRouteBuilder(
            ProducerEchoEndpoint producerEchoEndpoint,
            ProducerLoggingProcessor producerLoggingProcessor,
            ProducerSetReplyProcessor processorsSetReplyProcessor,
            ProducerRouteErrorHandler producerRouteErrorHandler,
            ConsumerEndpoint consumerEndpoint,
            ConsumerRegistrationErrorHandler consumerRegistrationErrorHandler,
            RegisterConsumerDisrupterProcessor registerConsumerDisrupterProcessor,
            DisruptorRingBufferEndpoint ringBufferEndpoint,
            ProducerEndpoint producerEndpoint
    ) {
        return new ServiceRouteBuilder(
                producerEchoEndpoint,
                producerLoggingProcessor,
                processorsSetReplyProcessor,
                producerRouteErrorHandler,
                consumerEndpoint,
                consumerRegistrationErrorHandler,
                registerConsumerDisrupterProcessor,
                ringBufferEndpoint,
                producerEndpoint
        );
    }

    @Provides
    @Singleton
    static RouteContext experimentalRouteContext(RouteBuilder routeBuilder) {
        return new RouteContextImpl(Collections.singletonList(routeBuilder));
    }
}
