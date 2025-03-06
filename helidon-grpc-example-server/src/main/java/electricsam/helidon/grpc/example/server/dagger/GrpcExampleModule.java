package electricsam.helidon.grpc.example.server.dagger;

import dagger.Module;
import dagger.Provides;
import electricsam.helidon.grpc.example.proto.ExampleGrpc;
import electricsam.helidon.grpc.example.server.consumer.ConsumerService;
import electricsam.helidon.grpc.example.server.consumer.DisruptorConsumerService;
import electricsam.helidon.grpc.example.server.experimental.eip.consumer.ConsumerRegistrationErrorHandler;
import electricsam.helidon.grpc.example.server.experimental.eip.consumer.ConsumerResponseErrorHandler;
import electricsam.helidon.grpc.example.server.experimental.eip.consumer.RegisterConsumerDisrupterProcessor;
import electricsam.helidon.grpc.example.server.experimental.eip.core.*;
import electricsam.helidon.grpc.example.server.experimental.eip.module.disruptor.DisruptorRingBufferEndpoint;
import electricsam.helidon.grpc.example.server.experimental.eip.module.grpc.GrcpStreamEndpointFactory;
import electricsam.helidon.grpc.example.server.experimental.eip.module.grpc.GrcpStreamService;
import electricsam.helidon.grpc.example.server.experimental.eip.producer.*;
import electricsam.helidon.grpc.example.server.experimental.eip.routes.RingBufferRouteBuilderFactory;
import electricsam.helidon.grpc.example.server.experimental.eip.routes.RingBufferRouteBuilderFactoryImpl;
import electricsam.helidon.grpc.example.server.experimental.eip.routes.ServiceRouteBuilder;
import electricsam.helidon.grpc.example.server.producer.ProducerService;
import electricsam.helidon.grpc.example.server.producer.ProducerServiceImpl;
import electricsam.helidon.grpc.example.server.server.GrpcExampleServer;
import electricsam.helidon.grpc.example.server.server.GrpcExampleServerImpl;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Arrays;
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
            RouteContext experimentalRouteContext,
            @Named("ProducerGrcpStreamService") GrcpStreamService experimentalProducerService,
            @Named("ConsumerGrcpStreamService") GrcpStreamService experimentalConsumerService
    ) {
        return new GrpcExampleServerImpl(
                producerService,
                consumerService,
                experimentalProducerService,
                experimentalConsumerService,
                experimentalRouteContext
        );
    }

    // TODO below this is experimental

    @Provides
    @Singleton
    @Named("ProduceStreamEchoEndpoint")
    static Endpoint experimentalProduceStreamEchoEndpoint(
            @Named("ProducerGrcpStreamEndpointFactory") GrcpStreamEndpointFactory factory
    ) {
        return factory.getEndpoint("ProduceStreamEcho");
    }


    @Provides
    @Singleton
    @Named("RegisterConsumerEndpoint")
    static Endpoint experimentalConsumerEndpoint(
            @Named("ConsumerGrcpStreamEndpointFactory") GrcpStreamEndpointFactory factory
    ) {
        return factory.getEndpoint("RegisterConsumer");
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
    static ProducerRouteErrorHandler experimentalProducerRouteErrorHandler(
            @Named("ProduceStreamEchoEndpoint") Endpoint endpoint,
            ProducerTemplate producerTemplate
    ) {
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
    static RingBufferRouteBuilderFactory experimentalRingBufferRouteBuilderFactory() {
        return new RingBufferRouteBuilderFactoryImpl();
    }

    @Provides
    @Singleton
    static RegisterConsumerDisrupterProcessor experimentalRegisterConsumerDisrupterProcessor(
            DisruptorRingBufferEndpoint ringBufferEndpoint,
            @Named("RegisterConsumerEndpoint") Endpoint consumerEndpoint,
            ConsumerResponseErrorHandler consumerResponseErrorHandler,
            RingBufferRouteBuilderFactory ringBufferRouteBuilderFactory
    ) {
        return new RegisterConsumerDisrupterProcessor(
                ringBufferEndpoint,
                consumerEndpoint,
                consumerResponseErrorHandler,
                ringBufferRouteBuilderFactory);
    }


    @Provides
    @Singleton
    @Named("ProduceStreamEndpoint")
    static Endpoint experimentalProduceStreamEndpoint(
            @Named("ProducerGrcpStreamEndpointFactory") GrcpStreamEndpointFactory factory
    ) {
        return factory.getEndpoint("ProduceStream");
    }

    @Provides
    @Singleton
    static NotCompletedFilter experimentalNotCompletedFilter() {
        return new NotCompletedFilter();
    }

    @Provides
    @Singleton
    @Named("ProducerGrcpStreamEndpointFactory")
    static GrcpStreamEndpointFactory experimentalProducerGrcpStreamEndpointFactory() {
        return new GrcpStreamEndpointFactory(
                "EipProducerService",
                ExampleGrpc.getDescriptor(),
                Arrays.asList("ProduceStream", "ProduceStreamEcho")
        );
    }


    @Provides
    @Singleton
    @Named("ConsumerGrcpStreamEndpointFactory")
    static GrcpStreamEndpointFactory experimentalConsumerGrcpStreamEndpointFactory() {
        return new GrcpStreamEndpointFactory(
                "EipConsumerService",
                ExampleGrpc.getDescriptor(),
                Collections.singletonList("RegisterConsumer")
        );
    }

    @Provides
    @Singleton
    @Named("ConsumerGrcpStreamService")
    static GrcpStreamService experimentalConsumerGrcpStreamService(
            @Named("ConsumerGrcpStreamEndpointFactory") GrcpStreamEndpointFactory factory
    ) {
        return factory.getService();
    }

    @Provides
    @Singleton
    @Named("ProducerGrcpStreamService")
    static GrcpStreamService experimentalProducerGrcpStreamService(
            @Named("ProducerGrcpStreamEndpointFactory") GrcpStreamEndpointFactory factory
    ) {
        return factory.getService();
    }

    @Provides
    @Singleton
    static RouteBuilder experimentalRouteBuilder(
            @Named("ProduceStreamEchoEndpoint") Endpoint producerEchoEndpoint,
            ProducerLoggingProcessor producerLoggingProcessor,
            ProducerSetReplyProcessor processorsSetReplyProcessor,
            ProducerRouteErrorHandler producerRouteErrorHandler,
            @Named("RegisterConsumerEndpoint") Endpoint consumerEndpoint,
            ConsumerRegistrationErrorHandler consumerRegistrationErrorHandler,
            RegisterConsumerDisrupterProcessor registerConsumerDisrupterProcessor,
            DisruptorRingBufferEndpoint ringBufferEndpoint,
            @Named("ProduceStreamEndpoint") Endpoint producerEndpoint,
            PrepareConsumerResponseProcessor prepareConsumerResponseProcessor,
            NotCompletedFilter notCompletedFilter
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
                producerEndpoint,
                prepareConsumerResponseProcessor,
                notCompletedFilter
        );
    }

    @Provides
    @Singleton
    static RouteContext experimentalRouteContext(RouteBuilder routeBuilder) {
        return new RouteContextImpl(Collections.singletonList(routeBuilder));
    }
}
