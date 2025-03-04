package electricsam.helidon.grpc.example.cli;

import electricsam.helidon.grpc.example.proto.ExampleGrpc.ProducerRequest;
import electricsam.helidon.grpc.example.proto.ExampleGrpc.ProducerResponse;
import io.helidon.grpc.client.GrpcServiceClient;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

@Command(
        name = "one",
        mixinStandardHelpOptions = true,
        description = "sends a single message to the gRPC server")
class ProduceSingleCommand implements Runnable {

    @Option(names = "--host", description = "The server host", defaultValue = "localhost")
    private String host;

    @Option(names = "--port", description = "The server port", defaultValue = "1408")
    private int port;

    @Override
    public void run() {
        GrpcServiceClient client = GrpcServiceClientFactory.create(ServiceName.ProducerService, host, port);
        String uuid = UUID.randomUUID().toString();
        System.out.print(uuid + " -> ");
        ProducerRequest request = ProducerRequest.newBuilder().setMessage(uuid).build();
        CompletionStage<ProducerResponse> responseStage = client.unary("ProduceUnary", request);
        ProducerResponse response;
        try {
            response = responseStage.toCompletableFuture().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Unable to get response", e);
        }
        System.out.println(response.getMessage());
    }
}
