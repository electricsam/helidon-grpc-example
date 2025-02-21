package electricsam.helidon.grpc.example.server.dagger;

import dagger.Module;
import dagger.Provides;
import electricsam.helidon.grpc.example.server.consumer.ConsumerService;
import electricsam.helidon.grpc.example.server.consumer.ConsumerServiceImpl;
import electricsam.helidon.grpc.example.server.producer.ProducerService;
import electricsam.helidon.grpc.example.server.producer.ProducerServiceImpl;
import electricsam.helidon.grpc.example.server.server.GrcpExampleServer;
import electricsam.helidon.grpc.example.server.server.GrcpExampleServerImpl;

import javax.inject.Singleton;

@Module
public interface GrpcExampleModule {

    @Provides
    @Singleton
    static ConsumerService consumerService () {
        return new ConsumerServiceImpl();
    }

    @Provides
    @Singleton
    static ProducerService producerService (ConsumerService consumerService) {
        return new ProducerServiceImpl(consumerService);
    }

    @Provides
    @Singleton
    static GrcpExampleServer grpcExampleServer(ConsumerService consumerService, ProducerService producerService) {
        return new GrcpExampleServerImpl(producerService, consumerService);
    }
}
