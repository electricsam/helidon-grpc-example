package electricsam.helidon.grpc.example.server.producer;

import electricsam.helidon.grpc.example.proto.ExampleGrpc;
import electricsam.helidon.grpc.example.proto.ExampleGrpc.ProducerRequest;
import electricsam.helidon.grpc.example.proto.ExampleGrpc.ProducerResponse;
import electricsam.helidon.grpc.example.server.consumer.ConsumerService;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.helidon.grpc.server.ServiceDescriptor.Rules;

public class ProducerServiceImpl implements ProducerService {

    private final ConsumerService consumerService;

    public ProducerServiceImpl(ConsumerService consumerService) {
        this.consumerService = consumerService;
    }

    @Override
    public String name() {
        return "ProducerService";
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
        consumerService.sendToConsumers(request);
        observer.onNext(response);
        System.out.println("Sent " + response.getMessage());
        observer.onCompleted();
    }

    private StreamObserver<ProducerRequest> bidi(StreamObserver<ProducerResponse> clientResponseStream) {
        return new StreamObserver<>() {
            public void onNext(ProducerRequest request) {
                ProducerResponse response = generateResponse(request);
                System.out.println("Sent " + response.getMessage());
                consumerService.sendToConsumers(request);
                clientResponseStream.onNext(response);
            }

            public void onError(Throwable t) {
                if (!isStreamClosed(t)) {
                    t.printStackTrace();
                    onCompleted();
                }
            }

            public void onCompleted() {
                System.out.println("Completed producer response stream");
                clientResponseStream.onCompleted();
            }
        };
    }

    private boolean isStreamClosed(Throwable t) {
        return t instanceof StatusRuntimeException && Status.CANCELLED == ((StatusRuntimeException) t).getStatus();
    }

}
