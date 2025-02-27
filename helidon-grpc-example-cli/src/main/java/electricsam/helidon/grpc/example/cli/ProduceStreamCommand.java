package electricsam.helidon.grpc.example.cli;


import electricsam.helidon.grpc.example.cli.GrpcServiceClientFactory.ClientType;
import electricsam.helidon.grpc.example.proto.ExampleGrpc.ProducerRequest;
import electricsam.helidon.grpc.example.proto.ExampleGrpc.ProducerResponse;
import io.grpc.stub.StreamObserver;
import io.helidon.grpc.client.GrpcServiceClient;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Command(
        name = "stream",
        mixinStandardHelpOptions = true,
        description = "sends an infinite stream of messages to the gRPC server")
public class ProduceStreamCommand implements Runnable {

    private final AtomicBoolean running = new AtomicBoolean(true);

    @CommandLine.Option(names = "--delay", description = "The number of milliseconds (w/ fractional ms) between each message. Set to -1 to disable.", defaultValue = "0.1")
    private double delay;

    private void shutdown() {
        running.set(false);
    }

    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(Thread.ofVirtual().unstarted(this::shutdown));

        long delayMs = (long) delay;
        int delayNanos = (int)((delay - (double) delayMs) * 100000d);

        // Disable direct buffers in order to better analyze memory usage. May reduce performance.
        System.setProperty("io.netty.noPreferDirect", "true");
        System.setProperty("io.netty.maxDirectMemory", "0");

        GrpcServiceClient client = GrpcServiceClientFactory.create(ClientType.PRODUCER);

        StreamObserver<ProducerResponse> observer = new ProducerResponseStream();
        StreamObserver<ProducerRequest> clientStream = client.bidiStreaming("ProduceStream", observer);

        try {
            while (running.get()) {
                ProducerRequest request = ProducerRequest.newBuilder().setMessage(UUID.randomUUID().toString()).build();
                System.out.println("Sent " + request.getMessage());
                clientStream.onNext(request);
                if (delay > 0d) {
                    try {
                        Thread.sleep(delayMs, delayNanos);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw e;
                    }
                }
            }
        } catch (Throwable throwable) {
            System.out.println("Client error. Stopping. " + throwable.getMessage());
            throwable.printStackTrace();
        } finally {
            clientStream.onCompleted();
        }

    }

    private class ProducerResponseStream implements StreamObserver<ProducerResponse> {

        @Override
        public void onNext(ProducerResponse response) {
            System.out.println("Received " + response.getMessage());
        }

        @Override
        public void onError(Throwable throwable) {
            System.out.println("Server error. Stopping. " + throwable.getMessage());
            throwable.printStackTrace();
            shutdown();
        }

        @Override
        public void onCompleted() {
            System.out.println("Response stream completed");
            shutdown();
        }
    }

}
