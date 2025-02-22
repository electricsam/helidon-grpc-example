package electricsam.helidon.grpc.example.server.dagger;

import dagger.Module;
import dagger.Provides;
import electricsam.helidon.grpc.example.server.consumer.ConsumerService;
import electricsam.helidon.grpc.example.server.consumer.DisruptorConsumerService;
import electricsam.helidon.grpc.example.server.producer.ProducerService;
import electricsam.helidon.grpc.example.server.producer.ProducerServiceImpl;
import electricsam.helidon.grpc.example.server.server.GrpcExampleServer;
import electricsam.helidon.grpc.example.server.server.GrpcExampleServerImpl;

import javax.inject.Singleton;

@Module
public interface GrpcExampleModule {

    @Provides
    @Singleton
    static ConsumerService consumerService () {
//        return new LinkedBlockingQueueConsumerService();
        return new DisruptorConsumerService();
    }

    @Provides
    @Singleton
    static ProducerService producerService (ConsumerService consumerService) {
        return new ProducerServiceImpl(consumerService);
    }

    @Provides
    @Singleton
    static GrpcExampleServer grpcExampleServer(ConsumerService consumerService, ProducerService producerService) {
        return new GrpcExampleServerImpl(producerService, consumerService);
    }
}
