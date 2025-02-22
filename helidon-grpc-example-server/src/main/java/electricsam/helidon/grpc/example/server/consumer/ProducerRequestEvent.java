package electricsam.helidon.grpc.example.server.consumer;

import electricsam.helidon.grpc.example.proto.ExampleGrpc;

public class ProducerRequestEvent {

    private ExampleGrpc.ProducerRequest request;

    public ExampleGrpc.ProducerRequest getRequest() {
        return request;
    }

    public void setRequest(ExampleGrpc.ProducerRequest request) {
        this.request = request;
    }

}
