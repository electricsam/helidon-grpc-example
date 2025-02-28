package electricsam.helidon.grpc.example.cli;


import electricsam.helidon.grpc.example.proto.ExampleGrpc.ProducerRequest;
import electricsam.helidon.grpc.example.proto.ExampleGrpc.ProducerResponse;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "stream",
        mixinStandardHelpOptions = true,
        description = "sends an infinite stream of messages to the gRPC server")
public class ProduceStreamCommand implements Runnable {

    @CommandLine.Option(names = "--delay", description = "The number of milliseconds (w/ fractional ms) between each message. Set to -1 to disable.", defaultValue = "0.1")
    private double delay;

    @CommandLine.Option(names = "--host", description = "The server host", defaultValue = "localhost")
    private String host;

    @CommandLine.Option(names = "--port", description = "The server port", defaultValue = "1408")
    private int port;

    @Override
    public void run() {

        // Disable direct buffers in order to better analyze memory usage. May reduce performance.
//        System.setProperty("io.netty.noPreferDirect", "true");
//        System.setProperty("io.netty.maxDirectMemory", "0");

        ProduceStreamExecutorConfiguration configuration = ProduceStreamExecutorConfiguration.builder()
                .setDelay(delay)
                .setHost(host)
                .setPort(port)
                .build();
        ProduceStreamExecutor executor = new ProduceStreamExecutor(configuration);
        executor.addVisitor(new ProduceStreamExecutorVisitor() {
            @Override
            public void beforeSendRequest(ProducerRequest request) {
                System.out.println("Sent " + request.getMessage());
            }

            @Override
            public void afterSendRequest(ProducerRequest request) {

            }

            @Override
            public void onReceiveResponse(ProducerResponse response) {
                System.out.println("Received " + response.getMessage());
            }

            @Override
            public void onResponseError(Throwable throwable) {
                System.out.println("Server error. Stopping. " + throwable.getMessage());
                throwable.printStackTrace();
            }

            @Override
            public void onResponseCompleted() {
                System.out.println("Response stream completed");
            }
        });

        Runtime.getRuntime().addShutdownHook(Thread.ofVirtual().unstarted(executor::stop));

        executor.run();

    }


}
