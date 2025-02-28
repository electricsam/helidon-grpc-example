package electricsam.helidon.grpc.example.cli;

import electricsam.helidon.grpc.example.proto.ExampleGrpc.ConsumerResponse;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Command(
        name = "print",
        mixinStandardHelpOptions = true,
        description = "prints messages from the gRPC server")
class ConsumePrintCommand implements Runnable {

    @CommandLine.Option(names = "--host", description = "The server host", defaultValue = "localhost")
    private String host;

    @CommandLine.Option(names = "--port", description = "The server port", defaultValue = "1408")
    private int port;

    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private final AtomicBoolean complete = new AtomicBoolean();

    @Override
    public void run() {

        ConsumeStreamExecutorConfiguration configuration = ConsumeStreamExecutorConfiguration.builder()
                .setHost(host)
                .setPort(port)
                .build();
        ConsumeStreamExecutor executor = new ConsumeStreamExecutor(configuration);
        executor.addVisitor(new ConsumeStreamExecutorVisitor() {
            @Override
            public void onReceiveResponse(ConsumerResponse response) {
                System.out.println("Received " + response.getMessage());
            }

            @Override
            public void onResponseError(Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onResponseCompleted() {

            }
        });

        Runtime.getRuntime().addShutdownHook(Thread.ofVirtual().unstarted(executor::stop));

        executor.run();

    }

}
