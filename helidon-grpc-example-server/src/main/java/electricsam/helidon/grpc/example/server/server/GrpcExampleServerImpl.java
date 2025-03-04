package electricsam.helidon.grpc.example.server.server;

import electricsam.helidon.grpc.example.server.consumer.ConsumerService;
import electricsam.helidon.grpc.example.server.experimental.eip.core.RouteContext;
import electricsam.helidon.grpc.example.server.experimental.eip.producer.ProducerEchoEndpoint;
import electricsam.helidon.grpc.example.server.producer.ProducerService;
import io.helidon.config.Config;
import io.helidon.grpc.server.GrpcRouting;
import io.helidon.grpc.server.GrpcServer;
import io.helidon.grpc.server.GrpcServerConfiguration;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static io.helidon.config.ConfigSources.*;

public class GrpcExampleServerImpl implements GrpcExampleServer {

    private final ProducerService producerService;
    private final ConsumerService consumerService;
    private final ProducerEchoEndpoint experimentalEipProducerEchoEndpoint;
    private final RouteContext experimentalRouteContext;
    private final AtomicReference<GrpcServer> grpcServerRef = new AtomicReference<>();

    public GrpcExampleServerImpl(
            ProducerService producerService,
            ConsumerService consumerService,
            ProducerEchoEndpoint experimentalEipProducerEchoEndpoint,
            RouteContext experimentalRouteContext
    ) {
        this.producerService = producerService;
        this.consumerService = consumerService;
        this.experimentalEipProducerEchoEndpoint = experimentalEipProducerEchoEndpoint;
        this.experimentalRouteContext = experimentalRouteContext;
    }

    @Override
    public CompletableFuture<GrpcServer> start() {

        experimentalRouteContext.start();

        GrpcServerConfiguration config = GrpcServerConfiguration.create(Config.builder().sources(systemProperties()).build());
        GrpcRouting routing = GrpcRouting.builder()
                .register(experimentalEipProducerEchoEndpoint)
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
            experimentalRouteContext.stop();
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
