package electricsam.helidon.grpc.example.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(
    name = "produce",
    subcommands = {
            ProduceSingleCommand.class,
            ProduceStreamCommand.class,
            ProduceStreamEipCommand.class
    },
    mixinStandardHelpOptions = true,
    description = "commands to send messages to the gRPC server")
class ProducerCommands implements Runnable {

  @Spec
  private CommandSpec spec;

  @Override
  public void run() {
    spec.commandLine().usage(System.out);
  }
}
