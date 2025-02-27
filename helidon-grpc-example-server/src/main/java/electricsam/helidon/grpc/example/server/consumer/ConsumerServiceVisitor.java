package electricsam.helidon.grpc.example.server.consumer;

public interface ConsumerServiceVisitor {

    //TODO Maybe a code smell. There must be a more generic way to do this.
    void onProducerServiceStop();
}
