package electricsam.helidon.grpc.example.cli;


import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "stream",
        mixinStandardHelpOptions = true,
        description = "sends an infinite stream of messages to the gRPC server")
public class ProduceStreamCommand extends ProduceStreamBaseCommand {

    @Option(names = "--delay", description = "The number of milliseconds (w/ fractional ms) between each message. Set to -1 to disable.", defaultValue = "1.0")
    private double delay;

    @Option(names = "--host", description = "The server host", defaultValue = "localhost")
    private String host;

    @Option(names = "--port", description = "The server port", defaultValue = "1408")
    private int port;

    // TODO need to figure out how to really turn off Netty direct buffers.  Tried properties does not work.
//    @Option(names = "--use-heap", description = "Have Netty use the heap instead of direct buffers", defaultValue = "false")
//    private boolean useHeap;

    @Override
    protected ProduceStreamExecutorConfiguration configure() {
        return ProduceStreamExecutorConfiguration.builder()
                .setDelay(delay)
                .setHost(host)
                .setPort(port)
//                .setNoDirectBuffers(useHeap)
                .setServiceName(ServiceName.ProducerService)
                .setMethodName(ServiceMethodName.ProduceStream)
                .build();
    }

}
