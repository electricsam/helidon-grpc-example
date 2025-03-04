package electricsam.helidon.grpc.example.server.experimental.eip.core.impl;

import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.BatchEventProcessorBuilder;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import electricsam.helidon.grpc.example.server.consumer.VirtualThreadFactory;
import electricsam.helidon.grpc.example.server.experimental.eip.core.Endpoint;
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
    private final ConcurrentHashMap<String, RouteRegistration> routeDefinitions = new ConcurrentHashMap<>();

    public DisruptorRingBufferEndpoint(int ringBufferSize) {
        this.ringBufferSize = ringBufferSize;
        Disruptor<DisruptorRingBufferEvent> disruptor = new Disruptor<>(DisruptorRingBufferEvent::new, ringBufferSize, VirtualThreadFactory.INSTANCE);
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
    public void process(Exchange exchange, RouteDefinitionInternal routeDefinition) {
        try {
            exchange.setProperty(RING_BUFFER_SIZE, ringBufferSize);
            exchange.setProperty(RING_BUFFER, ringBuffer);
            if (!ringBuffer.tryPublishEvent((event, sequence) -> event.setExchange(exchange))) {
                routeDefinition.getErrorHandler().handleError(new RingBufferOverflowException("Ring buffer overflow"), exchange);
            }
        } catch (Throwable t) {
            routeDefinition.getErrorHandler().handleError(t, exchange);
        }
    }

    private void subscribe(RouteDefinitionInternal routeDefinition) {
        BatchEventProcessor<DisruptorRingBufferEvent> batchEventProcessor = new BatchEventProcessorBuilder()
                .build(ringBuffer, ringBuffer.newBarrier(), (event, sequence, endOfBatch) -> onEvent(event, routeDefinition));
        ringBuffer.addGatingSequences(batchEventProcessor.getSequence());
        executor.execute(batchEventProcessor);
        //TODO handle unsupported case where a route is registered more than once
        //TODO check if there a potential threading error case between starting the batch processor, registration, and messages flowing in
        routeDefinitions.putIfAbsent(routeDefinition.getRouteId(), new RouteRegistration(batchEventProcessor, routeDefinition));
    }

    private void unsubscribe(String routeId) {
        RouteRegistration routeRegistration = routeDefinitions.remove(routeId);
        if (routeRegistration != null) {
            BatchEventProcessor<DisruptorRingBufferEvent> batchEventProcessor = routeRegistration.getBatchEventProcessor();
            batchEventProcessor.halt();
            ringBuffer.removeGatingSequence(batchEventProcessor.getSequence());
        }
    }

    private void onEvent(DisruptorRingBufferEvent event, RouteDefinitionInternal routeDefinition) {
        Exchange exchange = event.getExchange();
        try {
            exchange.setProperty(RING_BUFFER_SIZE, ringBufferSize);
            exchange.setProperty(RING_BUFFER, ringBuffer);
            routeDefinition.getProcessors().forEach(processor -> processor.process(exchange, routeDefinition));
        } catch (Throwable t) {
            routeDefinition.getErrorHandler().handleError(t, exchange);
        }
    }

    private static class RouteRegistration {
        private final BatchEventProcessor<DisruptorRingBufferEvent> batchEventProcessor;
        private final RouteDefinitionInternal routeDefinition;

        RouteRegistration(BatchEventProcessor<DisruptorRingBufferEvent> batchEventProcessor, RouteDefinitionInternal routeDefinition) {
            this.batchEventProcessor = batchEventProcessor;
            this.routeDefinition = routeDefinition;
        }

        BatchEventProcessor<DisruptorRingBufferEvent> getBatchEventProcessor() {
            return batchEventProcessor;
        }

        RouteDefinitionInternal getRouteDefinition() {
            return routeDefinition;
        }
    }

}
