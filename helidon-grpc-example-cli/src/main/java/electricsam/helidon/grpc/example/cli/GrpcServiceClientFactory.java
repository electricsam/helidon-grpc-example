package electricsam.helidon.grpc.example.cli;

import electricsam.helidon.grpc.example.proto.ConsumerServiceGrpc;
import electricsam.helidon.grpc.example.proto.ProducerServiceGrpc;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.helidon.grpc.client.ClientServiceDescriptor;
import io.helidon.grpc.client.GrpcServiceClient;

final class GrpcServiceClientFactory {

  enum ClientType {
    PRODUCER,
    CONSUMER
  }

  static GrpcServiceClient create(ClientType clientType, String host, int port) {
    ClientServiceDescriptor desc = ClientServiceDescriptor
        .builder(clientType == ClientType.PRODUCER ? ProducerServiceGrpc.getServiceDescriptor() : ConsumerServiceGrpc.getServiceDescriptor())
        .build();

    Channel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();

    return GrpcServiceClient.create(channel, desc);
  }

}
