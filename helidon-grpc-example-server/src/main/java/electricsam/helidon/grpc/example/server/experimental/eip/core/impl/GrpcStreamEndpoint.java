package electricsam.helidon.grpc.example.server.experimental.eip.core.impl;

import com.google.protobuf.Descriptors.FileDescriptor;
import electricsam.helidon.grpc.example.server.experimental.eip.core.RouteDefinitionInternal;
import electricsam.helidon.grpc.example.server.experimental.eip.core.Endpoint;
import electricsam.helidon.grpc.example.server.experimental.eip.core.Exchange;
import electricsam.helidon.grpc.example.server.experimental.eip.core.ExchangeImpl;
import io.grpc.stub.StreamObserver;
import io.helidon.grpc.server.GrpcService;
import io.helidon.grpc.server.ServiceDescriptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GrpcStreamEndpoint implements Endpoint, GrpcService {

    public static final String RESPONSE_STREAM_OBSERVER = "RESPONSE_STREAM_OBSERVER";
    public static final String REQUEST_STREAM_OBSERVER = "REQUEST_STREAM_OBSERVER";
    public static final String COMPLETED = "COMPLETED";

    private final List<StreamObserver<?>> responseStreams = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void process(Exchange exchange) {

    }

    private final String name;
    private final List<ProtoConfig> protoConfigs;
    private RouteDefinitionInternal routeDefinition;

    public GrpcStreamEndpoint(String name, List<ProtoConfig> protoConfigs) {
        this.name = name;
        this.protoConfigs = protoConfigs;
    }

    @Override
    public String name() {
        return name;
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
        synchronized (responseStreams) {
            responseStreams.forEach(StreamObserver::onCompleted);
        }
    }

    @Override
    public void update(ServiceDescriptor.Rules rules) {
        protoConfigs.forEach(protoConfig -> rules.proto(protoConfig.getFileDescriptor()).bidirectional(protoConfig.getName(), this::bidi));
    }

    private StreamObserver<Object> bidi(StreamObserver<?> producerResponseStream) {
        responseStreams.add(producerResponseStream);
        return new StreamObserver<>() {
            public void onNext(Object request) {
                final Exchange exchange = new ExchangeImpl();
                exchange.setBody(request);
                exchange.setProperty(REQUEST_STREAM_OBSERVER, this);
                exchange.setProperty(RESPONSE_STREAM_OBSERVER, producerResponseStream);
                exchange.setProperty(COMPLETED, false);
                try {
                    routeDefinition.getProcessors().forEach(p -> p.process(exchange));
                } catch (Throwable t) {
                    routeDefinition.getErrorHandler().handleError(t, exchange);
                }
            }

            public void onError(Throwable t) {
                final Exchange exchange = new ExchangeImpl();
                exchange.setProperty(REQUEST_STREAM_OBSERVER, this);
                exchange.setProperty(RESPONSE_STREAM_OBSERVER, producerResponseStream);
                exchange.setProperty(COMPLETED, false);
                routeDefinition.getErrorHandler().handleError(t, exchange);
            }

            public void onCompleted() {
                final Exchange exchange = new ExchangeImpl();
                exchange.setProperty(REQUEST_STREAM_OBSERVER, this);
                exchange.setProperty(RESPONSE_STREAM_OBSERVER, producerResponseStream);
                exchange.setProperty(COMPLETED, true);
                try {
                    routeDefinition.getProcessors().forEach(p -> p.process(exchange));
                } catch (Throwable t) {
                    routeDefinition.getErrorHandler().handleError(t, exchange);
                }
            }
        };
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
