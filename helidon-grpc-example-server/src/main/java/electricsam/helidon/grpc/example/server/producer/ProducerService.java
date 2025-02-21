package electricsam.helidon.grpc.example.server.producer;

import electricsam.helidon.grpc.example.proto.ExampleGrpc;
import electricsam.helidon.grpc.example.proto.ExampleGrpc.ProducerRequest;
import electricsam.helidon.grpc.example.proto.ExampleGrpc.ProducerResponse;
import electricsam.helidon.grpc.example.server.consumer.ConsumerService;
import io.grpc.stub.StreamObserver;
import io.helidon.grpc.server.GrpcService;
import io.helidon.grpc.server.ServiceDescriptor.Rules;

public class ProducerService implements GrpcService {

  private final ConsumerService consumerService;

  public ProducerService(ConsumerService consumerService) {
    this.consumerService = consumerService;
  }

  @Override
  public void update(Rules rules) {
    rules.proto(ExampleGrpc.getDescriptor()).unary("ProduceUnary", this::unary);
    rules.proto(ExampleGrpc.getDescriptor()).bidirectional("ProduceStream", this::bidi);
  }

  private static ProducerResponse generateResponse(ProducerRequest request) {
    System.out.println("Received " + request.getMessage());
    String message = "ack-" + request.getMessage();
    return ProducerResponse.newBuilder().setMessage(message).build();
  }


  private void unary(ProducerRequest request, StreamObserver<ProducerResponse> observer) {
    ProducerResponse response = generateResponse(request);
    observer.onNext(response);
    System.out.println("Sent " + response.getMessage());
    observer.onCompleted();
    consumerService.sendToConsumers(request);
  }

  private StreamObserver<ProducerRequest> bidi(StreamObserver<ProducerResponse> observer) {
    return new StreamObserver<>() {
      public void onNext(ProducerRequest request) {
        ProducerResponse response = generateResponse(request);
        observer.onNext(response);
        System.out.println("Sent " + response.getMessage());
        consumerService.sendToConsumers(request);
      }

      public void onError(Throwable t) {
        throw new RuntimeException("Unexpected error", t);
      }

      public void onCompleted() {
        observer.onCompleted();
      }
    };
  }

}
