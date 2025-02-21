package electricsam.helidon.grpc.example.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(
    name = "grpc",
    subcommands = {
    ProducerCommands.class,
    ConsumerCommands.class},
    mixinStandardHelpOptions = true,
    description = "commands to interact with the gRPC server")
public class Cli implements Runnable {

  @Spec
  private CommandSpec spec;

  public static void main(String[] args) {
    int exitCode = new CommandLine(new Cli()).execute(args);
    System.exit(exitCode);
  }

  @Override
  public void run() {
    spec.commandLine().usage(System.out);
  }
}
