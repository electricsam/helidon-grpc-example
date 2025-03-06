package electricsam.helidon.grpc.example.server.experimental.eip.core.impl;

import electricsam.helidon.grpc.example.server.experimental.eip.core.Endpoint;
import electricsam.helidon.grpc.example.server.experimental.eip.core.ErrorHandler;
import electricsam.helidon.grpc.example.server.experimental.eip.core.Exchange;
import electricsam.helidon.grpc.example.server.experimental.eip.core.ProducerTemplate;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DefaultProducerTemplate implements ProducerTemplate {

    private final Executor executor = Executors.newVirtualThreadPerTaskExecutor();

    @Override
    public void sendAsync(Exchange exchange, Endpoint endpoint, ErrorHandler errorHandler) {
        final Exchange copy = exchange.shallowCopy();
        executor.execute(() -> endpoint.process(copy, errorHandler));
    }
}
