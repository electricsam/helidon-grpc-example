package electricsam.helidon.grpc.example.server.producer;

import io.helidon.grpc.server.GrpcService;

public interface ProducerService extends GrpcService {

    void onServerShutdown();
}
