package electricsam.helidon.grpc.example.server.consumer;

import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.BatchEventProcessorBuilder;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import electricsam.helidon.grpc.example.proto.ExampleGrpc.ConsumerResponse;
import electricsam.helidon.grpc.example.proto.ExampleGrpc.ProducerRequest;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DisruptorConsumerService extends BaseConsumerService {

    private final Disruptor<ProducerRequestEvent> disruptor = new Disruptor<>(ProducerRequestEvent::new, 128, VirtualThreadFactory.INSTANCE);
    private final RingBuffer<ProducerRequestEvent> producerQueue = disruptor.start();
    private final ConcurrentHashMap<StreamObserver<ConsumerResponse>, ObserverBatchProcessor> observers = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();


    @Override
    protected void subscribe(StreamObserver<ConsumerResponse> observer) {
        BatchEventProcessor<ProducerRequestEvent> batchEventProcessor = new BatchEventProcessorBuilder()
                .build(producerQueue,
                        producerQueue.newBarrier(),
                        (producerRequest, l, b) -> onEvent(observer, producerRequest)
                );
        if (observers.putIfAbsent(observer, new ObserverBatchProcessor(observer, batchEventProcessor)) == null) {
            System.out.println("Registering consumer");
        }
        producerQueue.addGatingSequences(batchEventProcessor.getSequence());
        executor.execute(batchEventProcessor);
    }

    @Override
    protected void unsubscribe(StreamObserver<ConsumerResponse> observer) {
        ObserverBatchProcessor observerBatchProcessor = observers.remove(observer);
        if (observerBatchProcessor != null) {
            System.out.println("Unregistering consumer");
            observerBatchProcessor.getBatchEventProcessor().halt();
            observerBatchProcessor.stop();
            producerQueue.removeGatingSequence(observerBatchProcessor.getBatchEventProcessor().getSequence());
        }
    }


    @Override
    public void sendToConsumers(ProducerRequest request) {
        if (!producerQueue.tryPublishEvent((event, sequence) -> event.setRequest(request))) {
            // TODO handle this better
            throw new IllegalStateException("Producer queue is full");
        }
    }

    private void onEvent(StreamObserver<ConsumerResponse> observer, ProducerRequestEvent producerRequest) {
        if (!observers.get(observer).getQueue().offer(producerRequest.getRequest())) {
            // TODO this should not happen, this queue type is temporary
            throw new IllegalStateException("Producer queue is full");
        }
    }

}
