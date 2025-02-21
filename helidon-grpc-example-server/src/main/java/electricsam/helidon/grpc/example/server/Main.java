package electricsam.helidon.grpc.example.server;

import electricsam.helidon.grpc.example.server.consumer.ConsumerService;
import electricsam.helidon.grpc.example.server.producer.ProducerService;
import io.helidon.grpc.server.GrpcRouting;
import io.helidon.grpc.server.GrpcServer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Main {

  public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
    ConsumerService consumerService = new ConsumerService();
    GrpcServer grpcServer = GrpcServer
        .create(GrpcRouting.builder()
            .register(consumerService)
            .register(new ProducerService(consumerService))
            .build())
        .start()
        .toCompletableFuture()
        .get(10, TimeUnit.SECONDS); // Implement the simplest possible gRPC service.

    System.out.println("gRPC server started at: http://localhost:" + grpcServer.port());
  }

}
