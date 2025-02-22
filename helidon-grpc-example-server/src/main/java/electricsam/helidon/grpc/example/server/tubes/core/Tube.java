package electricsam.helidon.grpc.example.server.tubes.core;

public interface Tube {

    void to(TubeSink sink);
    Tube process(TubeProcessor processor);
    Tube errorHandler(TubeProcessor processor);
}
