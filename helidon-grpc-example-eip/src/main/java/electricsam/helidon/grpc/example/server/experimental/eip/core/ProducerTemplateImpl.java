package electricsam.helidon.grpc.example.server.experimental.eip.core;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ProducerTemplateImpl implements ProducerTemplate {

    private final Executor executor = Executors.newVirtualThreadPerTaskExecutor();

    @Override
    public void sendAsync(Exchange exchange, Endpoint endpoint, ErrorHandler errorHandler) {
        final Exchange copy = exchange.shallowCopy();
        executor.execute(() -> endpoint.process(copy, errorHandler));
    }
}
