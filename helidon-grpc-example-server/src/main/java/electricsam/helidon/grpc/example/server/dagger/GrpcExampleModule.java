package electricsam.helidon.grpc.example.server.dagger;

import dagger.Module;
import dagger.Provides;
import electricsam.helidon.grpc.example.server.consumer.ConsumerService;
import electricsam.helidon.grpc.example.server.consumer.DisruptorConsumerService;
import electricsam.helidon.grpc.example.server.experimental.eip.core.RouteBuilder;
import electricsam.helidon.grpc.example.server.experimental.eip.core.RouteContext;
import electricsam.helidon.grpc.example.server.experimental.eip.core.RouteContextImpl;
import electricsam.helidon.grpc.example.server.experimental.eip.producer.ProducerEchoEndpoint;
import electricsam.helidon.grpc.example.server.experimental.eip.producer.ProducerLoggingProcessor;
import electricsam.helidon.grpc.example.server.experimental.eip.producer.ProducerSetReplyProcessor;
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
    static ProducerLoggingProcessor producerLoggingProcessor() {
        return new ProducerLoggingProcessor();
    }

    @Provides
    @Singleton
    static ProducerSetReplyProcessor producerSetReplyProcessor() {
        return new ProducerSetReplyProcessor();
    }
//
//    @Provides
//    @Singleton
//    static ProducerRingBufferEndpoint producerRingBufferEndpoint() {
//        return new ProducerRingBufferEndpoint();
//    }
//
    @Provides
    @Singleton
    static RouteBuilder experimentalRouteBuilder(
            ProducerEchoEndpoint producerEchoEndpoint,
            ProducerLoggingProcessor producerLoggingProcessor,
            ProducerSetReplyProcessor processorsSetReplyProcessor
    ) {
        return new ServiceRouteBuilder(
                producerEchoEndpoint,
                producerLoggingProcessor,
                processorsSetReplyProcessor
        );
    }

    @Provides
    @Singleton
    static RouteContext experimentalRouteContext(RouteBuilder routeBuilder) {
        return new RouteContextImpl(Collections.singletonList(routeBuilder));
    }
}
