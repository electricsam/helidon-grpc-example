package electricsam.helidon.grpc.example.server.consumer;

import electricsam.helidon.grpc.example.proto.ExampleGrpc.ConsumerResponse;
import electricsam.helidon.grpc.example.proto.ExampleGrpc.ProducerRequest;
import io.grpc.stub.StreamObserver;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

public class LinkedBlockingQueueConsumerService extends BaseConsumerService implements Runnable {

    // TODO what is GC behavior with a linked list based queue
    private final LinkedBlockingDeque<ProducerRequest> producerQueue = new LinkedBlockingDeque<>(100);
    private final Set<StreamObserver<ConsumerResponse>> observers = ConcurrentHashMap.newKeySet();
    private final AtomicBoolean running = new AtomicBoolean(true);


    public LinkedBlockingQueueConsumerService() {
        Runtime.getRuntime().addShutdownHook(Thread.ofVirtual().unstarted(this::onShutdown));
        Thread.ofVirtual().start(this);
    }

    @Override
    protected void subscribe(StreamObserver<ConsumerResponse> observer) {
        if (observers.add(observer)) {
            System.out.println("Registering consumer");
        }
    }

    @Override
    protected void unsubscribe(StreamObserver<ConsumerResponse> observer) {
        if (observers.remove(observer)) {
            System.out.println("Unregistering consumer");
        }
    }

    @Override
    protected void onShutdown() {
        running.set(false);
    }

    @Override
    public void sendToConsumers(ProducerRequest request) {
        if (!producerQueue.offer(request)) {
            // TODO handle this better
            throw new IllegalStateException("Producer queue is full");
        }
    }

    @Override
    public void run() {
        try {
            while (running.get()) {
                final ProducerRequest request = producerQueue.take();
                final ConsumerResponse response = ConsumerResponse.newBuilder().setMessage(request.getMessage()).build();
                //TODO are there any side effects when concurrently removing?
                observers.forEach(observer -> observer.onNext(response));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Consumer response processing thread was interrupted", e);
        }
    }

}
