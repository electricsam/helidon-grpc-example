package electricsam.helidon.grpc.example.server.experimental.eip.core.impl;

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

    private final Disruptor<DisruptorRingBufferEvent> disruptor;
    private final RingBuffer<DisruptorRingBufferEvent> ringBuffer;
    //    private final ConcurrentHashMap<StreamObserver<ExampleGrpc.ConsumerResponse>, ObserverBatchProcessor> consumerResponseStreams = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private RouteDefinitionInternal routeDefinition;


    public DisruptorRingBufferEndpoint(int ringBufferSize) {
        this.ringBufferSize = ringBufferSize;
        disruptor = new Disruptor<>(DisruptorRingBufferEvent::new, ringBufferSize, VirtualThreadFactory.INSTANCE);
        ringBuffer = disruptor.start();
    }

    @Override
    public void setRouteDefinition(RouteDefinitionInternal routeDefinition) {
        this.routeDefinition = routeDefinition;
    }

    @Override
    public void start() {

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
