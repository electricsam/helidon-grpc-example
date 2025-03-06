package electricsam.helidon.grpc.example.server.experimental.eip.module.disruptor;

import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.BatchEventProcessorBuilder;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import electricsam.helidon.grpc.example.server.experimental.eip.core.Endpoint;
import electricsam.helidon.grpc.example.server.experimental.eip.core.ErrorHandler;
import electricsam.helidon.grpc.example.server.experimental.eip.core.Exchange;
import electricsam.helidon.grpc.example.server.experimental.eip.core.EndpointRouteDefinition;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class DisruptorRingBufferEndpoint implements Endpoint {

    private final RingBuffer<DisruptorRingBufferEvent> ringBuffer;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final ConcurrentHashMap<String, BatchEventProcessorQueue> routeDefinitions = new ConcurrentHashMap<>();
    private final AtomicBoolean running = new AtomicBoolean(true);


    public DisruptorRingBufferEndpoint(int ringBufferSize) {
        Disruptor<DisruptorRingBufferEvent> disruptor = new Disruptor<>(DisruptorRingBufferEvent::new, ringBufferSize, r -> Thread.ofVirtual().unstarted(r));
        ringBuffer = disruptor.start();
    }

    @Override
    public void removeRouteDefinition(String routeId) {
        unsubscribe(routeId);
    }

    @Override
    public void addRouteDefinition(EndpointRouteDefinition routeDefinition) {
        subscribe(routeDefinition);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        running.set(false);
        // TODO lock / error on stopping
        routeDefinitions.forEachValue(10, q -> {
            q.getRouteQueue().put(new RouteQueueElement(null, true));
        });
        //TODO wait for all queues to complete
        routeDefinitions.clear();
    }

    @Override
    public boolean process(Exchange exchange, ErrorHandler errorHandler) {
        try {
            Exchange copy = exchange.shallowCopy();
            if (!ringBuffer.tryPublishEvent((event, sequence) -> event.setExchange(copy))) {
                errorHandler.handleError(new RingBufferOverflowException("Ring buffer overflow"), exchange);
            }
        } catch (Exception e) {
            errorHandler.handleError(e, exchange);
            return false;
        }
        return true;
    }

    private void subscribe(EndpointRouteDefinition routeDefinition) {
        RouteQueue routeQueue = new RouteQueue(routeDefinition);
        BatchEventProcessor<DisruptorRingBufferEvent> batchEventProcessor = new BatchEventProcessorBuilder()
                .build(ringBuffer, ringBuffer.newBarrier(), (event, sequence, endOfBatch) -> onEvent(event, routeQueue));
        ringBuffer.addGatingSequences(batchEventProcessor.getSequence());
        executor.execute(batchEventProcessor);
        //TODO handle unsupported case where a route is registered more than once
        //TODO check if there a potential threading error case between starting the batch processor, registration, and messages flowing in
        routeDefinitions.putIfAbsent(routeDefinition.getRouteId(), new BatchEventProcessorQueue(batchEventProcessor, routeQueue));
    }

    private void unsubscribe(String routeId) {
        BatchEventProcessorQueue batchEventProcessorQueue = routeDefinitions.remove(routeId);
        if (batchEventProcessorQueue != null) {
            BatchEventProcessor<DisruptorRingBufferEvent> batchEventProcessor = batchEventProcessorQueue.getBatchEventProcessor();
            batchEventProcessor.halt();
            ringBuffer.removeGatingSequence(batchEventProcessor.getSequence());
        }
    }

    private void onEvent(DisruptorRingBufferEvent event, RouteQueue routeQueue) {
        routeQueue.put(new RouteQueueElement(event.getExchange().shallowCopy(), false));
    }

    private static class RouteQueueElement {
        private final Exchange exchange;
        private final boolean poison;

        private RouteQueueElement(Exchange exchange, boolean poison) {
            this.exchange = exchange;
            this.poison = poison;
        }

        public Exchange getExchange() {
            return exchange;
        }

        public boolean isPoison() {
            return poison;
        }
    }

    private static class RouteQueue implements Runnable {

        private final LinkedBlockingQueue<RouteQueueElement> queue = new LinkedBlockingQueue<>();
        private final EndpointRouteDefinition routeDefinition;

        private RouteQueue(EndpointRouteDefinition routeDefinition) {
            this.routeDefinition = routeDefinition;
            Thread.ofVirtual().start(this);
        }

        public void put(RouteQueueElement exchange) {
            queue.offer(exchange);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    RouteQueueElement element = queue.take();
                    if (element.isPoison()) {
                        break;
                    }
                    routeDefinition.processExchange(element.getExchange());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Exception caught while waiting for queue", e);
                }

            }
        }
    }

    private static class BatchEventProcessorQueue {
        private final BatchEventProcessor<DisruptorRingBufferEvent> batchEventProcessor;
        private final RouteQueue routeQueue;

        private BatchEventProcessorQueue(BatchEventProcessor<DisruptorRingBufferEvent> batchEventProcessor, RouteQueue routeQueue) {
            this.batchEventProcessor = batchEventProcessor;
            this.routeQueue = routeQueue;
        }

        public BatchEventProcessor<DisruptorRingBufferEvent> getBatchEventProcessor() {
            return batchEventProcessor;
        }

        public RouteQueue getRouteQueue() {
            return routeQueue;
        }
    }

}
