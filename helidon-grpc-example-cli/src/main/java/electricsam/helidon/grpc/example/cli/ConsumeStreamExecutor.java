package electricsam.helidon.grpc.example.cli;

import electricsam.helidon.grpc.example.proto.ExampleGrpc.ConsumerRegistration;
import electricsam.helidon.grpc.example.proto.ExampleGrpc.ConsumerResponse;
import io.grpc.stub.StreamObserver;
import io.helidon.grpc.client.GrpcServiceClient;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConsumeStreamExecutor {

    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private final AtomicBoolean complete = new AtomicBoolean();
    private final Set<ConsumeStreamExecutorVisitor> visitors = new HashSet<>();
    private final AtomicBoolean stopping = new AtomicBoolean(false);
    private final Lock stoppingLock = new ReentrantLock();
    private final Condition stopped = stoppingLock.newCondition();
    private final String host;
    private final int port;

    public ConsumeStreamExecutor(ConsumeStreamExecutorConfiguration configuration) {
        this.host = configuration.getHost();
        this.port = configuration.getPort();
    }

    public void addVisitor(ConsumeStreamExecutorVisitor visitor) {
        visitors.add(visitor);
    }

    public void run() {
        GrpcServiceClient client = GrpcServiceClientFactory.create(GrpcServiceClientFactory.ClientType.CONSUMER, host, port);
        ConsumerRegistration registration = ConsumerRegistration.newBuilder().setStart(true).setId(UUID.randomUUID().toString()).build();
        StreamObserver<ConsumerResponse> observer = new StreamObserver<>() {
            @Override
            public void onNext(ConsumerResponse consumerResponse) {
                visitors.forEach(visitor -> visitor.onReceiveResponse(consumerResponse));
            }

            @Override
            public void onError(Throwable throwable) {
                visitors.forEach(visitor -> visitor.onResponseError(throwable));
                complete();
            }

            @Override
            public void onCompleted() {
                visitors.forEach(ConsumeStreamExecutorVisitor::onResponseCompleted);
                complete();
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
        stoppingLock.lock();
        try {
            stopping.set(false);
            stopped.signalAll();
        } finally {
            stoppingLock.unlock();
        }
    }

    private void complete() {
        lock.lock();
        try {
            complete.set(true);
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void stop() {
        stoppingLock.lock();
        try {
            if (complete.get()) {
                return;
            }
            stopping.set(true);
            complete();
            while (stopping.get()) {
                stopped.await();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("thread was interrupted while waiting for stop", e);
        } finally {
            stoppingLock.unlock();
        }
    }
}
