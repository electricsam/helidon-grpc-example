package electricsam.helidon.grpc.example.server.experimental.eip.module.grpc;

import com.google.protobuf.Descriptors.FileDescriptor;
import electricsam.helidon.grpc.example.server.experimental.eip.core.Endpoint;
import electricsam.helidon.grpc.example.server.experimental.eip.core.ErrorHandler;
import electricsam.helidon.grpc.example.server.experimental.eip.core.Exchange;
import electricsam.helidon.grpc.example.server.experimental.eip.core.impl.DefaultExchange;
import electricsam.helidon.grpc.example.server.experimental.eip.core.EndpointRouteDefinition;
import io.grpc.stub.StreamObserver;
import io.helidon.grpc.server.ServiceDescriptor;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class GrpcStreamEndpoint implements Endpoint {

    public static final String RESPONSE_STREAM_OBSERVER_ID = "RESPONSE_STREAM_OBSERVER_ID";
    public static final String COMPLETED = "COMPLETED";

    private final ConcurrentHashMap<String, StreamObserverQueue> responseStreams = new ConcurrentHashMap<>();
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final String methodName;
    private final FileDescriptor fileDescriptor;
    private final AtomicReference<EndpointRouteDefinition> routeDefinitionRef = new AtomicReference<>();

    GrpcStreamEndpoint(String methodName, FileDescriptor fileDescriptor) {
        this.methodName = methodName;
        this.fileDescriptor = fileDescriptor;
    }

    @Override
    public void removeRouteDefinition(String routeId) {
        EndpointRouteDefinition routeDefinition = routeDefinitionRef.get();
        if (routeDefinition != null && routeDefinition.getRouteId().equals(routeId)) {
            routeDefinitionRef.set(null);
        }
    }

    @Override
    public void addRouteDefinition(EndpointRouteDefinition routeDefinition) {
        if (!routeDefinitionRef.compareAndSet(null, routeDefinition)) {
            throw new IllegalStateException("Route definition already exists");
        }
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        running.set(false);
        // TODO lock / error on stopping
        responseStreams.forEachValue(10, q -> {
            Exchange exchange = new DefaultExchange();
            exchange.setProperty(COMPLETED, true);
            q.put(exchange, (t, e) -> {}, true);
        });
        //TODO wait for all queues to complete
//        responseStreams.clear();
    }

    public void update(ServiceDescriptor.Rules rules) {
        rules.proto(fileDescriptor).bidirectional(methodName, this::bidi);
    }

    private StreamObserver<Object> bidi(StreamObserver<Object> responseStream) {
        String responseStreamId = UUID.randomUUID().toString();
        responseStreams.putIfAbsent(responseStreamId, new StreamObserverQueue(responseStream));
        return new StreamObserver<>() {
            public void onNext(Object request) {
                final Exchange exchange = new DefaultExchange();
                exchange.setBody(request);
                exchange.setProperty(RESPONSE_STREAM_OBSERVER_ID, responseStreamId);
                exchange.setProperty(COMPLETED, false);
                routeDefinitionRef.get().processExchange(exchange);
            }

            public void onError(Throwable t) {
                final Exchange exchange = new DefaultExchange();
                exchange.setProperty(RESPONSE_STREAM_OBSERVER_ID, responseStreamId);
                exchange.setProperty(COMPLETED, false);
                if(t instanceof Exception ) {
                    routeDefinitionRef.get().getErrorHandler().handleError((Exception) t, exchange);
                } else {
                    routeDefinitionRef.get().getErrorHandler().handleError(new SeriousErrorException(t), exchange);
                }
            }

            public void onCompleted() {
                final Exchange exchange = new DefaultExchange();
                exchange.setProperty(RESPONSE_STREAM_OBSERVER_ID, responseStreamId);
                exchange.setProperty(COMPLETED, true);
                routeDefinitionRef.get().processExchange(exchange);
            }
        };
    }

    @Override
    public boolean process(Exchange exchange, ErrorHandler errorHandler) {
        Exchange copy = exchange.shallowCopy();
        if (!running.get()) {
            errorHandler.handleError(new IllegalStateException("gRPC stream is shutting down"), exchange);
            return false;
        }
        try {
            String responseStreamId = copy.getProperty(RESPONSE_STREAM_OBSERVER_ID, String.class);
            if (responseStreamId == null) {
                throw new IllegalStateException("Exchange must contain a RESPONSE_STREAM_OBSERVER_ID property that corresponds to a valid StreamObserver.");
            }
            StreamObserverQueue responseStream = responseStreams.get(responseStreamId);
            if (responseStream == null) {
                throw new IllegalStateException("No StreamObserver found for RESPONSE_STREAM_OBSERVER_ID property " + responseStreamId);
            }
            responseStream.put(copy, errorHandler, false);
        } catch (Exception e) {
            errorHandler.handleError(e, copy);
            return false;
        }
        return true;
    }

    private static class StreamObserverQueueElement {
        private final Exchange exchange;
        private final ErrorHandler errorHandler;
        private final boolean poison;

        private StreamObserverQueueElement(Exchange exchange, ErrorHandler errorHandler, boolean poison) {
            this.exchange = exchange;
            this.errorHandler = errorHandler;
            this.poison = poison;
        }

        public Exchange getExchange() {
            return exchange;
        }

        public ErrorHandler getErrorHandler() {
            return errorHandler;
        }

        public boolean isPoison() {
            return poison;
        }
    }

    private class StreamObserverQueue implements Runnable {

        /*
            StreamObservers must be interacted in a thread safe way.  A queue is used to ensure that only one
            thread accesses the observer at any given time.

            https://github.com/thingsboard/thingsboard/issues/7910
            From StreamObserver JavaDoc:
            Implementations are not required to be thread-safe (but should be thread-compatible  ). Separate StreamObservers
            do not need to be synchronized together; incoming and outgoing directions are independent. Since individual
            StreamObservers are not thread-safe, if multiple threads will be writing to a StreamObserver concurrently,
            the application must synchronize calls.
         */
        private final LinkedBlockingQueue<StreamObserverQueueElement> queue = new LinkedBlockingQueue<>();
        private final StreamObserver<Object> observer;

        private StreamObserverQueue(StreamObserver<Object> observer) {
            this.observer = observer;
            Thread.ofVirtual().start(this);
        }

        public void put(Exchange exchange, ErrorHandler errorHandler, boolean poison) {
            queue.offer(new StreamObserverQueueElement(exchange, errorHandler, poison));
        }

        @Override
        public void run() {
            while (true) {
                StreamObserverQueueElement element;
                try {
                    element = queue.take();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Exception caught while waiting for queue", e);
                }
                Exchange exchange = element.getExchange();
                ErrorHandler errorHandler = element.getErrorHandler();
                try {
                    boolean completed = exchange.getProperty(COMPLETED, Boolean.class);
                    if (completed) {
                        observer.onCompleted();
                    } else {
                        Object body = exchange.getBody(Object.class);
                        observer.onNext(body);
                    }
                } catch (Exception e) {
                    errorHandler.handleError(e, exchange);
                }
                if (element.isPoison()) {
                    break;
                }
            }
        }
    }

}
