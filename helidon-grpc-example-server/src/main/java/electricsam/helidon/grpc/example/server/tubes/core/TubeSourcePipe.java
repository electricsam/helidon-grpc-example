package electricsam.helidon.grpc.example.server.tubes.core;

import java.util.List;

public class TubeSourcePipe {

    private final TubeSource source;
    private final List<TubeProcessor> processors;

    public TubeSourcePipe(TubeSource source, List<TubeProcessor> processors) {
        this.source = source;
        this.processors = processors;
    }

    public TubeSource getSource() {
        return source;
    }

    public List<TubeProcessor> getProcessors() {
        return processors;
    }
}
