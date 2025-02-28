package electricsam.helidon.grpc.example.cli;

import electricsam.helidon.grpc.example.proto.ExampleGrpc.ProducerRequest;
import electricsam.helidon.grpc.example.proto.ExampleGrpc.ProducerResponse;
import io.grpc.stub.StreamObserver;
import io.helidon.grpc.client.GrpcServiceClient;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class ProduceStreamExecutor {

    private final Set<ProduceStreamExecutorVisitor> visitors = new HashSet<>();
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final AtomicBoolean stopping = new AtomicBoolean(false);
    private final Lock lock = new ReentrantLock();
    private final Condition stopped = lock.newCondition();
    private final double delay;
    private final long delayMs;
    private final int delayNanos;
    private final String host;
    private final int port;


    public ProduceStreamExecutor(ProduceStreamExecutorConfiguration configuration) {
        this.delay = configuration.getDelay();
        delayMs = (long) delay;
        delayNanos = (int)((delay - (double) delayMs) * 100000d);
        this.host = configuration.getHost();
        this.port = configuration.getPort();
    }

    public void addVisitor(ProduceStreamExecutorVisitor visitor) {
        visitors.add(visitor);
    }

    public void run() {
        running.set(true);
        GrpcServiceClient client = GrpcServiceClientFactory.create(GrpcServiceClientFactory.ClientType.PRODUCER, host, port);
        StreamObserver<ProducerResponse> observer = new ProducerResponseStream();
        StreamObserver<ProducerRequest> clientStream = client.bidiStreaming("ProduceStream", observer);
        try {
            while (running.get()) {
                ProducerRequest request = ProducerRequest.newBuilder().setMessage(UUID.randomUUID().toString()).build();
                visitors.forEach(visitor -> visitor.beforeSendRequest(request));
                clientStream.onNext(request);
                visitors.forEach(visitor -> visitor.afterSendRequest(request));
                if (delay > 0d) {
                    try {
                        Thread.sleep(delayMs, delayNanos);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw e;
                    }
                }
            }
        } catch (Throwable throwable) {
            System.out.println("Client error. Stopping. " + throwable.getMessage());
            throwable.printStackTrace();
        } finally {
            clientStream.onCompleted();
            lock.lock();
            try {
                stopping.set(false);
                stopped.signalAll();
            } finally {
                lock.unlock();
            }
        }
    }

    public void stop() {
        lock.lock();
        try {
            if (!running.get()) {
                return;
            }
            stopping.set(true);
            running.set(false);
            while (stopping.get()) {
                stopped.await();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("thread was interrupted while waiting for stop", e);
        } finally {
            lock.unlock();
        }
    }

    private class ProducerResponseStream implements StreamObserver<ProducerResponse> {

        @Override
        public void onNext(ProducerResponse response) {
            visitors.forEach(visitor -> visitor.onReceiveResponse(response));
        }

        @Override
        public void onError(Throwable throwable) {
            visitors.forEach(visitor -> visitor.onResponseError(throwable));
            stop();
        }

        @Override
        public void onCompleted() {
            visitors.forEach(ProduceStreamExecutorVisitor::onResponseCompleted);
            stop();
        }
    }

}
