package electricsam.helidon.grpc.example.cli;

import electricsam.helidon.grpc.example.cli.GrpcServiceClientFactory.ClientType;
import electricsam.helidon.grpc.example.proto.ExampleGrpc.ConsumerRegistration;
import electricsam.helidon.grpc.example.proto.ExampleGrpc.ConsumerResponse;
import io.grpc.stub.StreamObserver;
import io.helidon.grpc.client.GrpcServiceClient;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import picocli.CommandLine.Command;

@Command(
    name = "print",
    mixinStandardHelpOptions = true,
    description = "prints messages from the gRPC server")
class ConsumePrintCommand implements Runnable {

  private final Lock lock = new ReentrantLock();
  private final Condition condition = lock.newCondition();
  private final AtomicBoolean complete = new AtomicBoolean();

  @Override
  public void run() {
    GrpcServiceClient client = GrpcServiceClientFactory.create(ClientType.CONSUMER);
    ConsumerRegistration registration = ConsumerRegistration.newBuilder().setStart(true).setId(UUID.randomUUID().toString()).build();
    StreamObserver<ConsumerResponse> observer = new StreamObserver<>() {
      @Override
      public void onNext(ConsumerResponse consumerResponse) {
        System.out.println("Received " + consumerResponse.getMessage());
      }

      @Override
      public void onError(Throwable throwable) {
        throwable.printStackTrace();
        lock.lock();
        try {
          complete.set(true);
          condition.signalAll();
        }finally {
          lock.unlock();
        }
      }

      @Override
      public void onCompleted() {
        lock.lock();
        try {
          complete.set(true);
          condition.signalAll();
        }finally {
          lock.unlock();
        }
      }
    };
    StreamObserver<ConsumerRegistration> clientStream = client.bidiStreaming("RegisterConsumer", observer);
    clientStream.onNext(registration);
    System.out.println("Registered " + registration.getId());
    lock.lock();
    try {
      while (!complete.get()) {
        condition.await();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Waiting for response queue was interrupted", e);
    } finally {
      lock.unlock();
    }
    clientStream.onCompleted();

  }

}
