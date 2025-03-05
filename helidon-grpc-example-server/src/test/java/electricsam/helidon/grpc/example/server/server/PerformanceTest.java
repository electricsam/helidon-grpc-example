package electricsam.helidon.grpc.example.server.server;

import electricsam.helidon.grpc.example.cli.*;
import electricsam.helidon.grpc.example.proto.ExampleGrpc;
import electricsam.helidon.grpc.example.server.dagger.DaggerGrpcExampleComponent;
import electricsam.helidon.grpc.example.server.dagger.GrpcExampleComponent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled("work in progress")
public class PerformanceTest {

    private GrpcExampleServer server;
    private int port;

    @BeforeEach
    void beforeEach() throws Exception {
        System.setProperty("port", "0");
        System.setProperty("name", UUID.randomUUID().toString());
        GrpcExampleComponent daggerContext = DaggerGrpcExampleComponent.builder().build();
        server = daggerContext.server();
        server.start().get();
        port = server.getPort();
    }

    @AfterEach
    void afterEach() throws Exception {
        server.stop().get();
    }

    @Test
    void throughputTest() throws Exception {

        int runTimeSeconds = 5;

        AtomicInteger messagesSent = new AtomicInteger();
        AtomicInteger messagesReceived = new AtomicInteger();

        ProduceStreamExecutor producer = new ProduceStreamExecutor(ProduceStreamExecutorConfiguration.builder()
                .setPort(port)
                .setDelay(0.1)
                .setServiceName(ServiceName.ProducerService)
                .setMethodName(ServiceMethodName.ProduceStream)
                .build());

        producer.addVisitor(new ProduceStreamExecutorVisitor() {

            @Override
            public void beforeSendRequest(ExampleGrpc.ProducerRequest request) {
                messagesSent.incrementAndGet();
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

        ConsumeStreamExecutor consumer = new ConsumeStreamExecutor(ConsumeStreamExecutorConfiguration.builder()
                .setPort(port)
                .setServiceName(ServiceName.ConsumerService)
                .build());

        consumer.addVisitor(new ConsumeStreamExecutorVisitor() {

            @Override
            public void onReceiveResponse(ExampleGrpc.ConsumerResponse response) {
                messagesReceived.incrementAndGet();
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
                Thread.sleep(runTimeSeconds * 1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("timer thread interrupted", e);
            }
            producer.stop();
            consumer.stop();
        }, Executors.newVirtualThreadPerTaskExecutor());

        Thread.ofVirtual().start(producer::run);
        Thread.ofVirtual().start(consumer::run);

        timer.join();

        //TODO update this
        // 115452
        System.out.println("messagesSent: " + messagesSent.get());
        System.out.println("messagesReceived: " + messagesReceived.get());
        assertTrue(messagesReceived.get() > 100_000);
        assertTrue(messagesSent.get() - messagesReceived.get() < 100);

    }
}
