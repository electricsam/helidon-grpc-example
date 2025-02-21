package electricsam.helidon.grpc.example.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(
    name = "consume",
    subcommands = { ConsumePrintCommand.class },
    mixinStandardHelpOptions = true,
    description = "commands to send messages to the gRPC server")
class ConsumerCommands implements Runnable {

  @Spec
  private CommandSpec spec;

  @Override
  public void run() {
    spec.commandLine().usage(System.out);
  }
}
