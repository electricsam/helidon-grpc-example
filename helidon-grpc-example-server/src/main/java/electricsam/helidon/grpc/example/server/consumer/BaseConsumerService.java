package electricsam.helidon.grpc.example.server.consumer;

import electricsam.helidon.grpc.example.proto.ExampleGrpc;
import electricsam.helidon.grpc.example.proto.ExampleGrpc.ConsumerRegistration;
import electricsam.helidon.grpc.example.proto.ExampleGrpc.ConsumerResponse;
import io.grpc.stub.StreamObserver;
import io.helidon.grpc.server.ServiceDescriptor.Rules;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

abstract class BaseConsumerService implements ConsumerService {

    private final AtomicBoolean acceptingConsumers = new AtomicBoolean(true);
    protected final Set<ConsumerServiceVisitor> visitors = Collections.synchronizedSet(new HashSet<>());

    protected void stopAcceptingConsumers() {
        acceptingConsumers.set(false);
    }

    @Override
    public String name() {
        return "ConsumerService";
    }

    @Override
    public void update(Rules rules) {
        rules.proto(ExampleGrpc.getDescriptor()).bidirectional("RegisterConsumer", this::register);
    }

    @Override
    public void addVisitor(ConsumerServiceVisitor visitor) {
        visitors.add(visitor);
    }

    @Override
    public void onServerShutdown() {
        stopAcceptingConsumers();
        onShutdown();
    }

    protected abstract void subscribe(StreamObserver<ConsumerResponse> consumerResponseStream);

    protected abstract void unsubscribe(StreamObserver<ConsumerResponse> consumerResponseStream);

    protected abstract void onShutdown();

    private StreamObserver<ConsumerRegistration> register(StreamObserver<ConsumerResponse> consumerResponseStream) {

        return new StreamObserver<>() {
            public void onNext(ConsumerRegistration registration) {
                if (registration.getStart()) {
                    if (acceptingConsumers.get()) {
                        subscribe(consumerResponseStream);
                    } else {
                        System.out.println("Consumer attempted to subscribe but service is not accepting new consumers");
                        //TODO return error?
                        consumerResponseStream.onCompleted();
                    }
                } else {
                    unsubscribe(consumerResponseStream);
                }
            }

            public void onError(Throwable t) {
                t.printStackTrace();
                //TODO return error?
                complete(consumerResponseStream);
            }

            public void onCompleted() {
                complete(consumerResponseStream);
            }
        };
    }

    protected void complete(StreamObserver<ConsumerResponse> consumerResponseStream) {
        consumerResponseStream.onCompleted();
        unsubscribe(consumerResponseStream);
    }

}
