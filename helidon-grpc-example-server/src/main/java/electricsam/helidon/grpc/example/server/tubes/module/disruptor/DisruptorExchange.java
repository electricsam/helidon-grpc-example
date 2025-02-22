package electricsam.helidon.grpc.example.server.tubes.module.disruptor;

import electricsam.helidon.grpc.example.server.tubes.core.TubeExchange;

import java.util.Optional;

public class DisruptorExchange<T> implements TubeExchange<T> {

    private final T body;
    private final Throwable error;

    public DisruptorExchange(T body, Throwable error) {
        this.body = body;
        this.error = error;
    }

    @Override
    public Optional<Throwable> getError() {
        return Optional.empty();
    }

    @Override
    public T getBody() {
        return null;
    }
}
