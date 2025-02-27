package electricsam.helidon.grpc.example.server.consumer;

import com.lmax.disruptor.BatchEventProcessor;
import electricsam.helidon.grpc.example.proto.ExampleGrpc.ConsumerResponse;
import electricsam.helidon.grpc.example.proto.ExampleGrpc.ProducerRequest;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;


class ObserverBatchProcessor implements Runnable {

    private final StreamObserver<ConsumerResponse> consumerResponseStream;
    private final BatchEventProcessor<ProducerRequestEvent> batchEventProcessor;
    // TODO this queue type is temporary
    private final LinkedBlockingQueue<ProducerRequest> queue = new LinkedBlockingQueue<>();
    private final AtomicBoolean running = new AtomicBoolean(true);

    ObserverBatchProcessor(StreamObserver<ConsumerResponse> consumerResponseStream, BatchEventProcessor<ProducerRequestEvent> batchEventProcessor) {
        this.consumerResponseStream = consumerResponseStream;
        this.batchEventProcessor = batchEventProcessor;
        Thread.ofVirtual().start(this);
    }

    BatchEventProcessor<ProducerRequestEvent> getBatchEventProcessor() {
        return batchEventProcessor;
    }

    void process(ProducerRequest producerRequest) {
        if (!queue.offer(producerRequest)) {
            // TODO this should not happen, this queue type is temporary
            throw new IllegalStateException("Producer queue is full");
        }
    }

    void stop() {
        running.set(false);
    }

    @Override
    public void run() {
        try {
            while (running.get()) {
                ProducerRequest request = queue.take();
                ConsumerResponse response = ConsumerResponse.newBuilder().setMessage(request.getMessage()).build();
                consumerResponseStream.onNext(response);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Consumer response processing thread was interrupted", e);
        }
    }
}
