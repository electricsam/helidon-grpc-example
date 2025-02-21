package electricsam.helidon.grpc.example.server.server;

import electricsam.helidon.grpc.example.server.consumer.ConsumerService;
import electricsam.helidon.grpc.example.server.producer.ProducerService;
import io.helidon.grpc.server.GrpcRouting;
import io.helidon.grpc.server.GrpcServer;

public class GrcpExampleServerImpl implements GrcpExampleServer {

    private final ProducerService producerService;
    private final ConsumerService consumerService;

    public GrcpExampleServerImpl(ProducerService producerService, ConsumerService consumerService) {
        this.producerService = producerService;
        this.consumerService = consumerService;
    }

    @Override
    public void start() {
        GrpcServer
                .create(GrpcRouting.builder()
                        .register(consumerService)
                        .register(producerService)
                        .build())
                .start()
                .toCompletableFuture()
                .thenAccept(grpcServer -> {
                    Runtime.getRuntime().addShutdownHook(new Thread(grpcServer::shutdown));
                    System.out.println("gRPC server started at: http://localhost:" + grpcServer.port());
                });
    }
}
