package electricsam.helidon.grpc.example.server.consumer;

import electricsam.helidon.grpc.example.proto.ExampleGrpc;
import electricsam.helidon.grpc.example.proto.ExampleGrpc.ConsumerRegistration;
import electricsam.helidon.grpc.example.proto.ExampleGrpc.ConsumerResponse;
import io.grpc.stub.StreamObserver;
import io.helidon.grpc.server.ServiceDescriptor.Rules;

abstract class BaseConsumerService implements ConsumerService {


    @Override
    public String name() {
        return "ConsumerService";
    }

    @Override
    public void update(Rules rules) {
        rules.proto(ExampleGrpc.getDescriptor()).bidirectional("RegisterConsumer", this::register);
    }

    protected abstract void subscribe(StreamObserver<ConsumerResponse> observer);

    protected abstract void unsubscribe(StreamObserver<ConsumerResponse> observer);

    private StreamObserver<ConsumerRegistration> register(StreamObserver<ConsumerResponse> observer) {

        return new StreamObserver<>() {
            public void onNext(ConsumerRegistration registration) {
                if (registration.getStart()) {
                    subscribe(observer);
                } else {
                    unsubscribe(observer);
                }
            }

            public void onError(Throwable t) {
                t.printStackTrace();
                unsubscribe(observer);
                observer.onError(t);
            }

            public void onCompleted() {
                unsubscribe(observer);
                observer.onCompleted();
            }
        };
    }

}
