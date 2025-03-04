package electricsam.helidon.grpc.example.server.dagger;

import dagger.Module;
import dagger.Provides;
import electricsam.helidon.grpc.example.server.consumer.ConsumerService;
import electricsam.helidon.grpc.example.server.consumer.DisruptorConsumerService;
//import electricsam.helidon.grpc.example.server.experimental.eip.core.RouteBuilder;
//import electricsam.helidon.grpc.example.server.experimental.eip.core.RouteContext;
//import electricsam.helidon.grpc.example.server.experimental.eip.core.RouteContextImpl;
//import electricsam.helidon.grpc.example.server.experimental.eip.producer.ProducerEndpoint;
//import electricsam.helidon.grpc.example.server.experimental.eip.producer.ProducerReplyProcessor;
//import electricsam.helidon.grpc.example.server.experimental.eip.producer.ProducerRingBufferEndpoint;
//import electricsam.helidon.grpc.example.server.experimental.eip.producer.ProducerRouteErrorHandler;
//import electricsam.helidon.grpc.example.server.experimental.eip.routes.ServiceRouteBuilder;
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
//        return new LinkedBlockingQueueConsumerService();
        return new DisruptorConsumerService();
    }

    @Provides
    @Singleton
    static ProducerService producerService(ConsumerService consumerService) {
        return new ProducerServiceImpl(consumerService);
    }

    @Provides
    @Singleton
    static GrpcExampleServer grpcExampleServer(ConsumerService consumerService, ProducerService producerService) {
        return new GrpcExampleServerImpl(producerService, consumerService);
    }

    // TODO this is experimental
//    @Provides
//    @Singleton
//    static ProducerEndpoint producerEndpoint() {
//        return new ProducerEndpoint();
//    }
//
//    @Provides
//    @Singleton
//    static ProducerReplyProcessor producerReplyProcessor() {
//        return new ProducerReplyProcessor();
//    }
//
//    @Provides
//    @Singleton
//    static ProducerRouteErrorHandler producerRouteErrorHandler() {
//        return new ProducerRouteErrorHandler();
//    }
//
//    @Provides
//    @Singleton
//    static ProducerRingBufferEndpoint producerRingBufferEndpoint() {
//        return new ProducerRingBufferEndpoint();
//    }
//
//    @Provides
//    @Singleton
//    static RouteBuilder routeBuilder(
//            ProducerEndpoint producerEndpoint,
//            ProducerReplyProcessor producerReplyProcessor,
//            ProducerRouteErrorHandler producerRouteErrorHandler,
//            ProducerRingBufferEndpoint producerRingBufferEndpoint
//    ) {
//        return new ServiceRouteBuilder(
//                producerEndpoint,
//                producerReplyProcessor,
//                producerRouteErrorHandler,
//                producerRingBufferEndpoint);
//    }
//
//    @Provides
//    @Singleton
//    static RouteContext routeContext(RouteBuilder routeBuilder) {
//        return new RouteContextImpl(Collections.singletonList(routeBuilder));
//    }
}
