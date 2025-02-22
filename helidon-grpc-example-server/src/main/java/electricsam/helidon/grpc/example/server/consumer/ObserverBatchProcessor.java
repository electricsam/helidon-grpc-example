package electricsam.helidon.grpc.example.server.consumer;

import com.lmax.disruptor.BatchEventProcessor;
import electricsam.helidon.grpc.example.proto.ExampleGrpc;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;


class ObserverBatchProcessor implements Runnable {

    private final StreamObserver<ExampleGrpc.ConsumerResponse> observer;
    private final BatchEventProcessor<ProducerRequestEvent> batchEventProcessor;
    // TODO this queue type is temporary
    private final LinkedBlockingQueue<ExampleGrpc.ProducerRequest> queue = new LinkedBlockingQueue<>();
    private final AtomicBoolean running = new AtomicBoolean(true);

    ObserverBatchProcessor(StreamObserver<ExampleGrpc.ConsumerResponse> observer, BatchEventProcessor<ProducerRequestEvent> batchEventProcessor) {
        this.observer = observer;
        this.batchEventProcessor = batchEventProcessor;
        Runtime.getRuntime().addShutdownHook(Thread.ofVirtual().unstarted(() -> running.set(false)));
        Thread.ofVirtual().start(this);
    }

    StreamObserver<ExampleGrpc.ConsumerResponse> getObserver() {
        return observer;
    }

    BatchEventProcessor<ProducerRequestEvent> getBatchEventProcessor() {
        return batchEventProcessor;
    }

    LinkedBlockingQueue<ExampleGrpc.ProducerRequest> getQueue() {
        return queue;
    }

    void stop() {
        running.set(false);
    }

    @Override
    public void run() {
        try {
            while (running.get()) {
                ExampleGrpc.ProducerRequest request = queue.take();
                ExampleGrpc.ConsumerResponse response = ExampleGrpc.ConsumerResponse.newBuilder().setMessage(request.getMessage()).build();
                observer.onNext(response);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Consumer response processing thread was interrupted", e);
        }
    }
}
