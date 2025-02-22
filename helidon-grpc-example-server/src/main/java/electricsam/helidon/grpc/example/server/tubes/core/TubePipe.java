package electricsam.helidon.grpc.example.server.tubes.core;

public interface TubePipe {

    void process(TubeExchange exchange);
}
