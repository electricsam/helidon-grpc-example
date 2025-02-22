package electricsam.helidon.grpc.example.server.tubes.core;

import java.util.List;

public class TubePipeImpl implements TubePipe {

    private final List<TubeProcessor> processors;

    public TubePipeImpl(List<TubeProcessor> processors) {
        this.processors = processors;
    }

    @Override
    public void process(TubeExchange exchange) {
        processors.forEach(processor -> processor.process(exchange));
    }
}
