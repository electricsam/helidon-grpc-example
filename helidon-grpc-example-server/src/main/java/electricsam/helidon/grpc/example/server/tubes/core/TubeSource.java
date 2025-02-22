package electricsam.helidon.grpc.example.server.tubes.core;

public interface TubeSource {

    void acceptPipe(TubePipe pipe);
    void start();
    void stop();
}
