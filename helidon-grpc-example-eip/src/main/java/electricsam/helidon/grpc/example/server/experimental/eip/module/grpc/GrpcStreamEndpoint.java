package electricsam.helidon.grpc.example.server.experimental.eip.module.grpc;

import com.google.protobuf.Descriptors.FileDescriptor;
import electricsam.helidon.grpc.example.server.experimental.eip.core.*;
import io.grpc.stub.StreamObserver;
import io.helidon.grpc.server.GrpcService;
import io.helidon.grpc.server.ServiceDescriptor;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class GrpcStreamEndpoint implements Endpoint, GrpcService {

    public static final String RESPONSE_STREAM_OBSERVER = "RESPONSE_STREAM_OBSERVER";
    public static final String RESPONSE_STREAM_OBSERVER_ID = "RESPONSE_STREAM_OBSERVER_ID";
    public static final String REQUEST_STREAM_OBSERVER = "REQUEST_STREAM_OBSERVER";
    public static final String COMPLETED = "COMPLETED";

    private final ConcurrentHashMap<String, StreamObserver<?>> responseStreams = new ConcurrentHashMap<>();

    private final String name;
    private final List<ProtoConfig> protoConfigs;
    private final AtomicReference<RouteDefinitionInternal> routeDefinitionRef = new AtomicReference<>();

    //TODO can this be broken up to support only one service name + RPC name?
    public GrpcStreamEndpoint(String name, List<ProtoConfig> protoConfigs) {
        this.name = name;
        this.protoConfigs = protoConfigs;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void removeRouteDefinition(String routeId) {
        RouteDefinitionInternal routeDefinition = routeDefinitionRef.get();
        if (routeDefinition != null && routeDefinition.getRouteId().equals(routeId)) {
            routeDefinitionRef.set(null);
        }
    }

    @Override
    public void addRouteDefinition(RouteDefinitionInternal routeDefinition) {
        if (!routeDefinitionRef.compareAndSet(null, routeDefinition)) {
            throw new IllegalStateException("Route definition already exists");
        }
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        // TODO lock / error on stopping
        responseStreams.forEachValue(10, StreamObserver::onCompleted);
        responseStreams.clear();
    }

    @Override
    public void update(ServiceDescriptor.Rules rules) {
        protoConfigs.forEach(protoConfig -> rules.proto(protoConfig.getFileDescriptor()).bidirectional(protoConfig.getName(), this::bidi));
    }

    private StreamObserver<Object> bidi(StreamObserver<?> responseStream) {
        String responseStreamId = UUID.randomUUID().toString();
        responseStreams.putIfAbsent(responseStreamId, responseStream);
        return new StreamObserver<>() {
            public void onNext(Object request) {
                final Exchange exchange = new ExchangeImpl();
                exchange.setBody(request);
                exchange.setProperty(REQUEST_STREAM_OBSERVER, this);
                exchange.setProperty(RESPONSE_STREAM_OBSERVER, responseStream);
                exchange.setProperty(RESPONSE_STREAM_OBSERVER_ID, responseStreamId);
                exchange.setProperty(COMPLETED, false);
                try {
                    routeDefinitionRef.get().getProcessors().forEach(p -> p.process(exchange, routeDefinitionRef.get().getErrorHandler()));
                } catch (Throwable t) {
                    routeDefinitionRef.get().getErrorHandler().handleError(t, exchange);
                }
            }

            public void onError(Throwable t) {
                final Exchange exchange = new ExchangeImpl();
                exchange.setProperty(REQUEST_STREAM_OBSERVER, this);
                exchange.setProperty(RESPONSE_STREAM_OBSERVER, responseStream);
                exchange.setProperty(RESPONSE_STREAM_OBSERVER_ID, responseStreamId);
                exchange.setProperty(COMPLETED, false);
                routeDefinitionRef.get().getErrorHandler().handleError(t, exchange);
            }

            public void onCompleted() {
                final Exchange exchange = new ExchangeImpl();
                exchange.setProperty(REQUEST_STREAM_OBSERVER, this);
                exchange.setProperty(RESPONSE_STREAM_OBSERVER, responseStream);
                exchange.setProperty(RESPONSE_STREAM_OBSERVER_ID, responseStreamId);
                exchange.setProperty(COMPLETED, true);
                try {
                    routeDefinitionRef.get().getProcessors().forEach(p -> p.process(exchange, routeDefinitionRef.get().getErrorHandler()));
                } catch (Throwable t) {
                    routeDefinitionRef.get().getErrorHandler().handleError(t, exchange);
                }
            }
        };
    }


    // TODO add visitors?
    @Override
    public void process(Exchange exchange, ErrorHandler errorHandler) {
        try {
            // TODO handle types better
            StreamObserver responseStream = exchange.getProperty(RESPONSE_STREAM_OBSERVER, StreamObserver.class);
            if (responseStream == null) {
                String responseStreamId = exchange.getProperty(RESPONSE_STREAM_OBSERVER_ID, String.class);
                if (responseStreamId == null) {
                    responseStream = responseStreams.get(responseStreamId);
                }
            }
            if (responseStream == null) {
                throw new IllegalStateException("Exchange must contain either a RESPONSE_STREAM_OBSERVER property or a RESPONSE_STREAM_OBSERVER_ID property that correspond to a valid StreamObserver.");
            }
            boolean completed = exchange.getProperty(COMPLETED, Boolean.class);
            if (completed) {
                responseStream.onCompleted();
            } else {
                Object body = exchange.getBody(Object.class);
                responseStream.onNext(body);
            }
        } catch (Throwable t) {
            errorHandler.handleError(t, exchange);
        }
    }


    public static class ProtoConfig {
        private final String name;
        private final FileDescriptor fileDescriptor;

        public ProtoConfig(String name, FileDescriptor fileDescriptor) {
            this.name = name;
            this.fileDescriptor = fileDescriptor;
        }

        public String getName() {
            return name;
        }

        public FileDescriptor getFileDescriptor() {
            return fileDescriptor;
        }
    }

}
