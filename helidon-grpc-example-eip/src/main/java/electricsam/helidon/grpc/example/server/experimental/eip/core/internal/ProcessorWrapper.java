package electricsam.helidon.grpc.example.server.experimental.eip.core.internal;

import electricsam.helidon.grpc.example.server.experimental.eip.core.ErrorHandler;
import electricsam.helidon.grpc.example.server.experimental.eip.core.Exchange;
import electricsam.helidon.grpc.example.server.experimental.eip.core.Processor;

public class ProcessorWrapper implements Processable {

    private final Processor processor;

    public ProcessorWrapper(Processor processor) {
        this.processor = processor;
    }

    @Override
    public boolean process(Exchange exchange, ErrorHandler errorHandler) {
        processor.process(exchange);
        return true;
    }
}
