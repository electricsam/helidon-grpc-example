package electricsam.helidon.grpc.example.server.experimental.eip.module.disruptor;

import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.BatchEventProcessorBuilder;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import electricsam.helidon.grpc.example.server.experimental.eip.core.Endpoint;
import electricsam.helidon.grpc.example.server.experimental.eip.core.ErrorHandler;
import electricsam.helidon.grpc.example.server.experimental.eip.core.Exchange;
import electricsam.helidon.grpc.example.server.experimental.eip.core.RouteDefinitionInternal;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DisruptorRingBufferEndpoint implements Endpoint {

    public static final String RING_BUFFER_SIZE = "RING_BUFFER_SIZE";
    public static final String RING_BUFFER = "RING_BUFFER";

    private final int ringBufferSize;

    private final RingBuffer<DisruptorRingBufferEvent> ringBuffer;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final ConcurrentHashMap<String, BatchEventProcessor<DisruptorRingBufferEvent>> routeDefinitions = new ConcurrentHashMap<>();

    public DisruptorRingBufferEndpoint(int ringBufferSize) {
        this.ringBufferSize = ringBufferSize;
        Disruptor<DisruptorRingBufferEvent> disruptor = new Disruptor<>(DisruptorRingBufferEvent::new, ringBufferSize, r -> Thread.ofVirtual().unstarted(r));
        ringBuffer = disruptor.start();
    }

    @Override
    public void removeRouteDefinition(String routeId) {
        unsubscribe(routeId);
    }

    @Override
    public void addRouteDefinition(RouteDefinitionInternal routeDefinition) {
        subscribe(routeDefinition);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
    }

    @Override
    public boolean process(Exchange exchange, ErrorHandler errorHandler) {
        try {
            exchange.setProperty(RING_BUFFER_SIZE, ringBufferSize);
            exchange.setProperty(RING_BUFFER, ringBuffer);
            Exchange copy = exchange.shallowCopy();
            if (!ringBuffer.tryPublishEvent((event, sequence) -> event.setExchange(copy))) {
                errorHandler.handleError(new RingBufferOverflowException("Ring buffer overflow"), exchange);
            }
        } catch (Throwable t) {
            errorHandler.handleError(t, exchange);
            return false;
        }
        return true;
    }

    private void subscribe(RouteDefinitionInternal routeDefinition) {
        BatchEventProcessor<DisruptorRingBufferEvent> batchEventProcessor = new BatchEventProcessorBuilder()
                .build(ringBuffer, ringBuffer.newBarrier(), (event, sequence, endOfBatch) -> onEvent(event, routeDefinition));
        ringBuffer.addGatingSequences(batchEventProcessor.getSequence());
        executor.execute(batchEventProcessor);
        //TODO handle unsupported case where a route is registered more than once
        //TODO check if there a potential threading error case between starting the batch processor, registration, and messages flowing in
        routeDefinitions.putIfAbsent(routeDefinition.getRouteId(), batchEventProcessor);
    }

    private void unsubscribe(String routeId) {
        BatchEventProcessor<DisruptorRingBufferEvent> batchEventProcessor = routeDefinitions.remove(routeId);
        if (batchEventProcessor != null) {
            batchEventProcessor.halt();
            ringBuffer.removeGatingSequence(batchEventProcessor.getSequence());
        }
    }

    private void onEvent(DisruptorRingBufferEvent event, RouteDefinitionInternal routeDefinition) {
        Exchange exchange = event.getExchange();
        exchange.setProperty(RING_BUFFER_SIZE, ringBufferSize);
        exchange.setProperty(RING_BUFFER, ringBuffer);
        routeDefinition.processExchange(exchange);
    }

}
