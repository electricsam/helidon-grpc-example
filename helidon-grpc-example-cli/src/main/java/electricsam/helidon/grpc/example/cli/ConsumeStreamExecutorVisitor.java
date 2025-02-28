package electricsam.helidon.grpc.example.cli;

import electricsam.helidon.grpc.example.proto.ExampleGrpc.ConsumerResponse;


public interface ConsumeStreamExecutorVisitor {
    void onReceiveResponse(ConsumerResponse response);
    void onResponseError(Throwable throwable);
    void onResponseCompleted();

}
