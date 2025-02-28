package electricsam.helidon.grpc.example.server.server;

import electricsam.helidon.grpc.example.cli.ProduceStreamExecutor;
import electricsam.helidon.grpc.example.cli.ProduceStreamExecutorConfiguration;
import electricsam.helidon.grpc.example.cli.ProduceStreamExecutorVisitor;
import electricsam.helidon.grpc.example.proto.ExampleGrpc;
import electricsam.helidon.grpc.example.server.dagger.DaggerGrpcExampleComponent;
import electricsam.helidon.grpc.example.server.dagger.GrpcExampleComponent;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

@Disabled("work in progress")
public class PerformanceTest {

    // TODO finish me
    @Test
    void throughputTest() throws Exception {
        System.setProperty("port", "0");
        GrpcExampleComponent daggerContext = DaggerGrpcExampleComponent.builder().build();
        GrpcExampleServer server = daggerContext.server();
        server.start().get();
        int port = server.getPort();

        ProduceStreamExecutorConfiguration configuration = ProduceStreamExecutorConfiguration.builder()
                .setPort(port)
                .setDelay(0.1)
                .build();
        ProduceStreamExecutor executor = new ProduceStreamExecutor(configuration);

        executor.addVisitor(new ProduceStreamExecutorVisitor() {

            @Override
            public void beforeSendRequest(ExampleGrpc.ProducerRequest request) {

            }

            @Override
            public void afterSendRequest(ExampleGrpc.ProducerRequest request) {

            }

            @Override
            public void onReceiveResponse(ExampleGrpc.ProducerResponse response) {

            }

            @Override
            public void onResponseError(Throwable throwable) {

            }

            @Override
            public void onResponseCompleted() {

            }
        });

        CompletableFuture<Void> timer = CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("timer thread interrupted", e);
            }
            executor.stop();
        }, Executors.newVirtualThreadPerTaskExecutor());


        executor.run();

        timer.join();

    }
}
