package electricsam.helidon.grpc.example.cli;

import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.helidon.grpc.client.ClientServiceDescriptor;
import io.helidon.grpc.client.GrpcServiceClient;

final class GrpcServiceClientFactory {

    static GrpcServiceClient create(ServiceName serviceName, String host, int port) {
        ClientServiceDescriptor desc = ClientServiceDescriptor.builder(serviceName.getServiceDescriptor()).build();
        Channel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        return GrpcServiceClient.create(channel, desc);
    }

}
