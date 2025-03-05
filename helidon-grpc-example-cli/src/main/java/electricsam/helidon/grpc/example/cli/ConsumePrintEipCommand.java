package electricsam.helidon.grpc.example.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "experimental-eip",
        mixinStandardHelpOptions = true,
        description = "prints messages from the gRPC server")
class ConsumePrintEipCommand extends ConsumePrintBaseCommand {

    @CommandLine.Option(names = "--host", description = "The server host", defaultValue = "localhost")
    private String host;

    @CommandLine.Option(names = "--port", description = "The server port", defaultValue = "1408")
    private int port;

    @Override
    protected ConsumeStreamExecutorConfiguration configure() {
        return ConsumeStreamExecutorConfiguration.builder()
                .setHost(host)
                .setPort(port)
                .setServiceName(ServiceName.EipConsumerService)
                .build();
    }

}
