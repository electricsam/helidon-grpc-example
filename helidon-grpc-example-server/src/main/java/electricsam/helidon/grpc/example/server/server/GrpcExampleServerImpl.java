package electricsam.helidon.grpc.example.server.server;

import electricsam.helidon.grpc.example.server.consumer.ConsumerService;
import electricsam.helidon.grpc.example.server.experimental.eip.core.RouteContextLifecycle;
import electricsam.helidon.grpc.example.server.producer.ProducerService;
import io.helidon.config.Config;
import io.helidon.grpc.server.GrpcRouting;
import io.helidon.grpc.server.GrpcServer;
import io.helidon.grpc.server.GrpcServerConfiguration;
import io.helidon.grpc.server.GrpcService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static io.helidon.config.ConfigSources.systemProperties;

public class GrpcExampleServerImpl implements GrpcExampleServer {

    private final ProducerService producerService;
    private final ConsumerService consumerService;
    private final GrpcService experimentalEipProducerService;
    private final RouteContextLifecycle experimentalRouteContextLifecycle;
    private final GrpcService experimentalEipConsumerService;


    private final AtomicReference<GrpcServer> grpcServerRef = new AtomicReference<>();

    public GrpcExampleServerImpl(
            ProducerService producerService,
            ConsumerService consumerService,
            GrpcService experimentalEipProducerService,
            GrpcService experimentalEipConsumerService,
            RouteContextLifecycle experimentalRouteContextLifecycle
    ) {
        this.producerService = producerService;
        this.consumerService = consumerService;
        this.experimentalEipProducerService = experimentalEipProducerService;
        this.experimentalRouteContextLifecycle = experimentalRouteContextLifecycle;
        this.experimentalEipConsumerService = experimentalEipConsumerService;
    }

    @Override
    public CompletableFuture<GrpcServer> start() {

        experimentalRouteContextLifecycle.start();

        GrpcServerConfiguration config = GrpcServerConfiguration.create(Config.builder().sources(systemProperties()).build());
        GrpcRouting routing = GrpcRouting.builder()
                .register(experimentalEipProducerService)
                .register(experimentalEipConsumerService)
                .register(consumerService)
                .register(producerService)
                .build();
        return GrpcServer
                .create(config, routing)
                .start()
                .toCompletableFuture()
                .thenApply(grpcServer -> {
                    grpcServerRef.set(grpcServer);
                    System.out.println("gRPC server started at: http://localhost:" + grpcServer.port());
                    return grpcServer;
                });
    }

    @Override
    public CompletableFuture<GrpcServer> stop() {
        GrpcServer grpcServer = grpcServerRef.get();
        if (grpcServer != null) {
            producerService.onServerShutdown();
            consumerService.onServerShutdown();
            experimentalRouteContextLifecycle.stop();
            return grpcServer.shutdown().toCompletableFuture();
        } else {
            throw new IllegalStateException("server not started");
        }
    }

    @Override
    public int getPort() {
        GrpcServer grpcServer = grpcServerRef.get();
        if (grpcServer != null) {
            return grpcServer.port();
        }
        throw new IllegalStateException("server not started");
    }
}
