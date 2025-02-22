package electricsam.helidon.grpc.example.server.tubes.module.disruptor;

public class GenericEvent<T> {

    private T value;

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
