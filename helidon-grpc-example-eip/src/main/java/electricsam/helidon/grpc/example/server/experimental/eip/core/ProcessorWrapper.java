package electricsam.helidon.grpc.example.server.experimental.eip.core;

class ProcessorWrapper implements Processable {

    private final Processor processor;

    ProcessorWrapper(Processor processor) {
        this.processor = processor;
    }

    @Override
    public boolean process(Exchange exchange, ErrorHandler errorHandler) {
        processor.process(exchange);
        return true;
    }
}
