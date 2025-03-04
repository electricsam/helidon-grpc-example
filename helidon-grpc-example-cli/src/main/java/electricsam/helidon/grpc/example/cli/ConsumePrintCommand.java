package electricsam.helidon.grpc.example.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "print",
        mixinStandardHelpOptions = true,
        description = "prints messages from the gRPC server")
class ConsumePrintCommand extends ConsumePrintBaseCommand {

    @CommandLine.Option(names = "--host", description = "The server host", defaultValue = "localhost")
    private String host;

    @CommandLine.Option(names = "--port", description = "The server port", defaultValue = "1408")
    private int port;

    @Override
    protected ConsumeStreamExecutorConfiguration configure() {
        return ConsumeStreamExecutorConfiguration.builder()
                .setHost(host)
                .setPort(port)
                .setServiceName(ServiceName.ConsumerService)
                .build();
    }

}
