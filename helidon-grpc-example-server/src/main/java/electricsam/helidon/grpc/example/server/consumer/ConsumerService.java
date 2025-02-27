package electricsam.helidon.grpc.example.server.consumer;

import electricsam.helidon.grpc.example.proto.ExampleGrpc.ProducerRequest;
import io.helidon.grpc.server.GrpcService;

public interface ConsumerService extends GrpcService {
    void sendToConsumers(ProducerRequest request);
    void addVisitor(ConsumerServiceVisitor visitor);
    void onServerShutdown();
}
