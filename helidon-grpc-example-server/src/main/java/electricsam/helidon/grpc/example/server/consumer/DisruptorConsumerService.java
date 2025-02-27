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
    private final ConcurrentHashMap<StreamObserver<ConsumerResponse>, ObserverBatchProcessor> consumerResponseStreams = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @Override
    protected void onShutdown() {
        completeAllResponseStreams();
    }

    private void completeAllResponseStreams() {
        consumerResponseStreams.forEachKey(100, this::complete);
    }

    @Override
    protected void subscribe(StreamObserver<ConsumerResponse> consumerResponseStream) {
        BatchEventProcessor<ProducerRequestEvent> batchEventProcessor
                = new BatchEventProcessorBuilder().build(
                producerQueue,
                producerQueue.newBarrier(),
                (producerRequest, l, b) -> onEvent(consumerResponseStream, producerRequest)
        );
        if (consumerResponseStreams.putIfAbsent(consumerResponseStream, new ObserverBatchProcessor(consumerResponseStream, batchEventProcessor)) == null) {
            System.out.println("Registering consumer");
        }
        producerQueue.addGatingSequences(batchEventProcessor.getSequence());
        executor.execute(batchEventProcessor);
    }

    @Override
    protected void unsubscribe(StreamObserver<ConsumerResponse> consumerResponseStream) {
        ObserverBatchProcessor observerBatchProcessor = consumerResponseStreams.remove(consumerResponseStream);
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
            System.out.println("Producer queue is full");
            stopAcceptingConsumers();
            completeAllResponseStreams();
            synchronized (visitors) {
                visitors.forEach(ConsumerServiceVisitor::onProducerServiceStop);
            }
        }
    }

    private void onEvent(StreamObserver<ConsumerResponse> consumerResponseStream, ProducerRequestEvent producerRequest) {
        ObserverBatchProcessor observerBatchProcessor = consumerResponseStreams.get(consumerResponseStream);
        if(observerBatchProcessor != null) {
            observerBatchProcessor.process(producerRequest.getRequest());
        }
    }

}
