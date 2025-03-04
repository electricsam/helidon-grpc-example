package electricsam.helidon.grpc.example.server.experimental.eip.core.impl;

import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.BatchEventProcessorBuilder;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import electricsam.helidon.grpc.example.server.consumer.VirtualThreadFactory;
import electricsam.helidon.grpc.example.server.experimental.eip.core.Endpoint;
import electricsam.helidon.grpc.example.server.experimental.eip.core.Exchange;
import electricsam.helidon.grpc.example.server.experimental.eip.core.RouteDefinitionInternal;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DisruptorRingBufferEndpoint implements Endpoint {

    public static final String RING_BUFFER_SIZE = "RING_BUFFER_SIZE";
    public static final String RING_BUFFER = "RING_BUFFER";

    private final int ringBufferSize;
    private final boolean disableDefaultOutput;

    private final Disruptor<DisruptorRingBufferEvent> disruptor;
    private final RingBuffer<DisruptorRingBufferEvent> ringBuffer;
    //    private final ConcurrentHashMap<StreamObserver<ExampleGrpc.ConsumerResponse>, ObserverBatchProcessor> consumerResponseStreams = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private RouteDefinitionInternal routeDefinition;


    public DisruptorRingBufferEndpoint(int ringBufferSize, boolean disableDefaultOutput) {
        this.ringBufferSize = ringBufferSize;
        disruptor = new Disruptor<>(DisruptorRingBufferEvent::new, ringBufferSize, VirtualThreadFactory.INSTANCE);
        this.disableDefaultOutput = disableDefaultOutput;
        ringBuffer = disruptor.start();
    }

    @Override
    public void setRouteDefinition(RouteDefinitionInternal routeDefinition) {
        this.routeDefinition = routeDefinition;
    }

    @Override
    public void start() {
        if (!disableDefaultOutput) {
            subscribe();
        }
    }

    @Override
    public void stop() {
    }

    @Override
    public void process(Exchange exchange) {
        if (!ringBuffer.tryPublishEvent((event, sequence) -> event.setExchange(exchange))) {
            exchange.setProperty(RING_BUFFER_SIZE, ringBufferSize);
            exchange.setProperty(RING_BUFFER, ringBuffer);
            routeDefinition.getErrorHandler().handleError(new RingBufferOverflowException("Ring buffer overflow"), exchange);
        }
    }

    public void subscribe() {
        BatchEventProcessor<DisruptorRingBufferEvent> batchEventProcessor = new BatchEventProcessorBuilder()
                .build(ringBuffer, ringBuffer.newBarrier(), this::onEvent);
        ringBuffer.addGatingSequences(batchEventProcessor.getSequence());
        executor.execute(batchEventProcessor);
    }

    private void onEvent(DisruptorRingBufferEvent event, long sequence, boolean endOfBatch) {
        Exchange exchange = event.getExchange();
        exchange.setProperty(RING_BUFFER_SIZE, ringBufferSize);
        exchange.setProperty(RING_BUFFER, ringBuffer);
        try {
            routeDefinition.getProcessors().forEach(processor -> processor.process(exchange));
        } catch (Throwable t) {
            routeDefinition.getErrorHandler().handleError(t, exchange);
        }
    }

    public static class RingBufferOverflowException extends RuntimeException {
        public RingBufferOverflowException(String message) {
            super(message);
        }
    }

    public static class DisruptorRingBufferEvent {
        private Exchange exchange;

        public Exchange getExchange() {
            return exchange;
        }

        public void setExchange(Exchange exchange) {
            this.exchange = exchange;
        }
    }
}
