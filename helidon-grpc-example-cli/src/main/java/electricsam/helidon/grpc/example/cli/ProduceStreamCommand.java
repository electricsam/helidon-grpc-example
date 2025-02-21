package electricsam.helidon.grpc.example.cli;


import electricsam.helidon.grpc.example.cli.GrpcServiceClientFactory.ClientType;
import electricsam.helidon.grpc.example.proto.ExampleGrpc.ProducerRequest;
import electricsam.helidon.grpc.example.proto.ExampleGrpc.ProducerResponse;
import io.grpc.stub.StreamObserver;
import io.helidon.grpc.client.GrpcServiceClient;
import java.util.UUID;
import java.util.stream.Stream;
import picocli.CommandLine.Command;

@Command(
    name = "stream",
    mixinStandardHelpOptions = true,
    description = "sends an infinite stream of messages to the gRPC server")
public class ProduceStreamCommand implements Runnable {

  private static class ProducerResponseStream implements StreamObserver<ProducerResponse> {

    @Override
    public void onNext(ProducerResponse response) {
      System.out.println("Received " + response.getMessage());
    }

    @Override
    public void onError(Throwable throwable) {
      throw new RuntimeException("An error occurred in the response stream", throwable);
    }

    @Override
    public void onCompleted() {
      System.out.println("Response stream completed");
    }
  }

  @Override
  public void run() {
    GrpcServiceClient client = GrpcServiceClientFactory.create(ClientType.PRODUCER);

    StreamObserver<ProducerResponse> observer = new ProducerResponseStream();
    StreamObserver<ProducerRequest> clientStream = client.bidiStreaming("ProduceStream", observer);

    Stream<ProducerRequest> infinityStream = Stream.generate(() -> UUID.randomUUID().toString())
        .map(uuid -> ProducerRequest.newBuilder().setMessage(uuid).build());

    infinityStream.forEach(request -> {
      System.out.println("Sent " + request.getMessage());
      clientStream.onNext(request);
    });

    clientStream.onCompleted();
  }

}
