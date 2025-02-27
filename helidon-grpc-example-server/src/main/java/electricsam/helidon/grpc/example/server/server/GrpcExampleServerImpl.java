package electricsam.helidon.grpc.example.server.server;

import electricsam.helidon.grpc.example.server.consumer.ConsumerService;
import electricsam.helidon.grpc.example.server.producer.ProducerService;
import io.helidon.grpc.server.GrpcRouting;
import io.helidon.grpc.server.GrpcServer;

import java.util.concurrent.atomic.AtomicReference;

public class GrpcExampleServerImpl implements GrpcExampleServer {

    private final ProducerService producerService;
    private final ConsumerService consumerService;
    private final AtomicReference<GrpcServer> grpcServerRef = new AtomicReference<>();

    public GrpcExampleServerImpl(ProducerService producerService, ConsumerService consumerService) {
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
                    grpcServerRef.set(grpcServer);
                    System.out.println("gRPC server started at: http://localhost:" + grpcServer.port());
                });
    }

    @Override
    public void stop() {
        producerService.onServerShutdown();
        consumerService.onServerShutdown();
        GrpcServer grpcServer = grpcServerRef.get();
        if (grpcServer != null) {
            grpcServer.shutdown();
        }
    }
}
