package electricsam.helidon.grpc.example.cli;

import electricsam.helidon.grpc.example.proto.ExampleGrpc.ProducerResponse;
import electricsam.helidon.grpc.example.proto.ExampleGrpc.ProducerRequest;

public interface ProduceStreamExecutorVisitor {
    void beforeSendRequest(ProducerRequest request);
    void afterSendRequest(ProducerRequest request);
    void onReceiveResponse(ProducerResponse response);
    void onResponseError(Throwable throwable);
    void onResponseCompleted();

}
